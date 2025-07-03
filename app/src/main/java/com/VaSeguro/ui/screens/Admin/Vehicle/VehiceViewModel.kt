package com.VaSeguro.ui.screens.Admin.Vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.Dao.Vehicle.VehicleDao
import com.VaSeguro.data.Entitys.Vehicle.VehicleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.remote.Vehicle.toDomain
import com.VaSeguro.data.remote.Vehicle.toEntity
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class VehicleViewModel(
  private val vehicleRepository: VehicleRepository,
  private val userPreferencesRepository: UserPreferencesRepository,
  private val vehicleDao: VehicleDao
) : ViewModel() {

  private val _drivers = MutableStateFlow<List<UserData>>(emptyList())
  val drivers: StateFlow<List<UserData>> = _drivers

  private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  val vehicles: StateFlow<List<Vehicle>> = _vehicles

  private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

  private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val CACHE_EXPIRATION_MS = 1 * 60 * 1000L
  init {
    loadVehicles()
  }

  private fun loadVehicles() {
    viewModelScope.launch {
      _isLoading.value = true
      // 1. Read from Room
      val cached = vehicleDao.getAllVehicles()
      if (cached.isNotEmpty()) {
        _vehicles.value = cached.map { it.toDomain() }
      }
      // 2. Check cache expiration
      val lastFetch = userPreferencesRepository.getLastVehiclesFetchTime()
      val now = System.currentTimeMillis()
      if (lastFetch != null && now - lastFetch < CACHE_EXPIRATION_MS) {
        _isLoading.value = false
        return@launch
      }
      // 3. Fetch from API and update Room
      try {
        val token = userPreferencesRepository.getAuthToken().orEmpty()
        val apiVehicles = vehicleRepository.getAllVehicles(token)
        apiVehicles.collect { resource ->
          when (resource) {
            is com.VaSeguro.helpers.Resource.Success -> {
              val vehicles = resource.data.map { it.toDomain() }
              _vehicles.value = vehicles
              vehicleDao.clearVehicles()
              vehicleDao.insertVehicles(resource.data.map { it.toEntity() })
              userPreferencesRepository.setLastVehiclesFetchTime(now)
            }
            is com.VaSeguro.helpers.Resource.Error -> {
              // Optionally handle error
            }
            com.VaSeguro.helpers.Resource.Loading -> {
              _isLoading.value = true
            }
          }
        }
      } catch (_: Exception) { }
      _isLoading.value = false
    }
  }
  private fun Vehicle.toEntity() = VehicleEntity(
    id = id.toInt(),
    plate = plate,
    driver_id = driver_id.toInt(),
    model = model,
    brand = brand,
    year = year,
    color = color,
    capacity = capacity,
    updated_at = updated_at,
    carPic = carPic,
    created_at = created_at
  )

  fun toggleExpand(vehicleId: String) {
    _expandedMap.update { current ->
      current.toMutableMap().apply {
        this[vehicleId] = !(this[vehicleId] ?: false)
      }
    }
  }

  fun setChecked(vehicleId: String, checked: Boolean) {
    _checkedMap.update { current ->
      current.toMutableMap().apply {
        this[vehicleId] = checked
      }
    }
  }


  fun deleteVehicle(vehicleId: String) {
    viewModelScope.launch {
      vehicleRepository.deleteVehicle(vehicleId.toInt(), userPreferencesRepository.getAuthToken().toString()).collect { resource ->
        when (resource) {
          is Resource.Success -> {
            _vehicles.update { list ->
              list.filterNot { it.id == vehicleId }
            }
            _expandedMap.update { it - vehicleId }
            _checkedMap.update { it - vehicleId }
            loadVehicles()
          }
          is Resource.Error -> {
            println("Error al eliminar vehículo: ${resource.message}")
          }
          Resource.Loading -> { }
        }
      }
    }
  }
  fun updateVehicle(
    id: Int,
    plate: String,
    model: String,
    brand: String,
    year: String,
    color: String,
    capacity: String,
    carPic: MultipartBody.Part? = null,
    onResult: (Boolean, String?) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      vehicleRepository.updateVehicle(
        userPreferencesRepository.getAuthToken().toString(),
        id = id,
        plate = plate,
        model = model,
        brand = brand,
        year = year,
        color = color,
        capacity = capacity,
        carPic = carPic
      ).collect { resource ->
        when (resource) {
          is Resource.Success -> {
            loadVehicles()
            onResult(true, null)
          }
          is Resource.Error -> {
            onResult(false, resource.message)
          }
          Resource.Loading -> { }
        }
      }
    }
  }

}