package com.VaSeguro.ui.screens.Admin.Vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.remote.Vehicle.toDomain
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.launch

class VehicleViewModel(
  private val vehicleRepository: VehicleRepository,
  private val userPreferencesRepository: UserPreferencesRepository
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


  init {
    loadVehicles()
  }

  private fun loadVehicles() {
    viewModelScope.launch {
      _isLoading.value = true
      vehicleRepository.getAllVehicles(userPreferencesRepository.getAuthToken().toString()).collect { resource ->
        when (resource) {
          is Resource.Success -> {
            _vehicles.value = resource.data.map { it.toDomain() }
            _isLoading.value = false
          }
          is Resource.Error -> {
            println("Error al cargar vehículos: ${resource.message}")
            _isLoading.value = false
          }
          Resource.Loading -> {
            _isLoading.value = true
          }
        }
      }
    }
  }


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

}