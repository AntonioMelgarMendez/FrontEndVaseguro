package com.VaSeguro.ui.screens.Admin.Vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.remote.Vehicle.toDomain
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VehicleViewModel(
  private val vehicleRepository: VehicleRepository,
) : ViewModel() {

  private val _drivers = MutableStateFlow(
    listOf(
      UserData(
        id = "19",
        forename = "Ana",
        surname = "García",
        email = "ana.garcia@example.com",
        phoneNumber = "1234567890",
        profilePic = null,
        role_id = UserRole(1, "Conductor"),
        gender = "F"
      ),
      UserData(
        id = "20",
        forename = "Carlos",
        surname = "Mendoza",
        email = "carlos.mendoza@example.com",
        phoneNumber = "9876543210",
        profilePic = null,
        role_id = UserRole(1, "Conductor"),
        gender = "M"
      ),
      UserData(
        id = "21",
        forename = "Lucía",
        surname = "Pérez",
        email = "lucia.perez@example.com",
        phoneNumber = "5551234567",
        profilePic = null,
        role_id = UserRole(1, "Conductor"),
        gender = "F"
      )
    )
  )

  fun getDriverForVehicle(driverId: String): UserData? {
    return _drivers.value.firstOrNull { it.id == driverId }
  }

  private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())

  init {
    loadVehicles()
  }

  private fun loadVehicles() {
    viewModelScope.launch {
      vehicleRepository.getAllVehicles().collect { resource ->
        when (resource) {
          is Resource.Success -> {
            _vehicles.value = resource.data.map { it.toDomain() }
          }
          is Resource.Error -> {
            println("Error al cargar vehículos: ${resource.message}")
          }
          Resource.Loading -> {

          }
        }
      }
    }
  }


  val drivers: StateFlow<List<UserData>> = _drivers
  val vehicles: StateFlow<List<Vehicle>> = _vehicles

  private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

  private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

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
      vehicleRepository.deleteVehicle(vehicleId.toInt()).collect { resource ->
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
          Resource.Loading -> {

          }
        }
      }
    }
  }

  fun addVehicle(plate: String, model: String, driverId: String) {
    viewModelScope.launch {
      vehicleRepository.createVehicle(
        plate,
        model,
        "Toyota",
        "2023",
        "Gris",
        "5",
        driverId.toInt(),
        null
      ).collectLatest { resource ->
        when (resource) {
          is Resource.Success -> {
            resource.data?.let { newVehicleResponse ->
              val newVehicle = newVehicleResponse.toDomain()
              _vehicles.update { current -> current + newVehicle }
              Log.d("VehicleViewModel", "Vehicle added: ${newVehicle.plate}, Model: ${newVehicle.model}, Driver ID: ${newVehicle.driver_id}")
            }

            loadVehicles()
          }
          is Resource.Error -> {
            println("Error al crear vehículo: ${resource.message}")
          }
          Resource.Loading -> {
            // Mostrar loading si querés
          }
        }
      }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        try {
          val application = this[APPLICATION_KEY] as MyApplication
          VehicleViewModel(
            application.appProvider.provideVehicleRepository()
          )
        } catch (e: Exception) {
          e.printStackTrace()
          throw e
        }
      }
    }
  }

}