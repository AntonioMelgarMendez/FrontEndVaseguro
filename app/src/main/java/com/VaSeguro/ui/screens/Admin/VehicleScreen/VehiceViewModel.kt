package com.VaSeguro.ui.screens.Admin.VehicleScreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.VaSeguro.data.model.Vehicle
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole

class VehicleViewModel : ViewModel() {
  private val _vehicles = MutableStateFlow(
    listOf(
      Vehicle(
        id = "12451",
        plate = "PABC123",
        model = "Toyota Yaris",
        driver_id = UserData(
          id = "u1",
          forename = "Daniel",
          surname = "Hawkins",
          email = "daniel@example.com",
          phoneNumber = "1234567890",
          profilePic = null,
          role_id = UserRole(1, "Driver"),
          gender = "Male"
        ),
        created_at = "17/04/2025 14:01"
      ),
      Vehicle(
        id = "12452",
        plate = "XDEF456",
        model = "Honda Civic",
        driver_id = UserData(
          id = "u2",
          forename = "Maria",
          surname = "Lopez",
          email = "maria@example.com",
          phoneNumber = "0987654321",
          profilePic = null,
          role_id = UserRole(1, "Driver"),
          gender = "Female"
        ),
        created_at = "17/04/2025 15:00"
      ),
      Vehicle(
        id = "12453",
        plate = "JKL789",
        model = "Ford Focus",
        driver_id = UserData(
          id = "u3",
          forename = "Carlos",
          surname = "Martinez",
          email = "carlos@example.com",
          phoneNumber = "1112223333",
          profilePic = null,
          role_id = UserRole(1, "Driver"),
          gender = "Male"
        ),
        created_at = "18/04/2025 09:30"
      ),
      Vehicle(
        id = "12454",
        plate = "XYZ321",
        model = "Chevrolet Spark",
        driver_id = UserData(
          id = "u4",
          forename = "Ana",
          surname = "Gonzalez",
          email = "ana@example.com",
          phoneNumber = "4445556666",
          profilePic = null,
          role_id = UserRole(1, "Driver"),
          gender = "Female"
        ),
        created_at = "18/04/2025 10:15"
      )
    )
  )
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
    _vehicles.update { list ->
      list.filterNot { it.id == vehicleId }
    }
    _expandedMap.update { it - vehicleId }
    _checkedMap.update { it - vehicleId }
  }

  fun addVehicle(plate: String, model: String, driverName: String) {
    val randomId = (10000..99999).random().toString()
    val now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    val (forename, surname) = driverName.split(" ").let {
      if (it.size >= 2) it[0] to it[1] else it[0] to "Lastname"
    }

    val vehicle = Vehicle(
      id = randomId,
      plate = plate,
      model = model,
      driver_id = UserData(
        id = "u${randomId}",
        forename = forename,
        surname = surname,
        email = "$forename@example.com",
        phoneNumber = "1234567890",
        profilePic = null,
        role_id = UserRole(1, "Driver"),
        gender = "Male"
      ),
      created_at = now
    )

    _vehicles.update { it + vehicle }
  }
}
