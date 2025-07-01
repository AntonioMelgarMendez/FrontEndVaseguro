package com.VaSeguro.ui.screens.Parents.Bus

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import com.VaSeguro.data.remote.Vehicle.toEntity
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class BusViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _resolvedImageUrl = MutableStateFlow<String?>(null)
    val resolvedImageUrl: StateFlow<String?> = _resolvedImageUrl

    private val _vehicle = MutableStateFlow<Resource<VehicleResponse>>(Resource.Loading)
    val vehicle: StateFlow<Resource<VehicleResponse>> = _vehicle

    private val _driverFullName = MutableStateFlow<String?>(null)
    val driverFullName: StateFlow<String?> = _driverFullName

    private val _driverPhoneNumber = MutableStateFlow<String?>(null)
    val driverPhoneNumber: StateFlow<String?> = _driverPhoneNumber

    private val _isDriverLoading = MutableStateFlow(false)
    val isDriverLoading: StateFlow<Boolean> = _isDriverLoading

    private val _userRole = MutableStateFlow<Int?>(null)
    val userRole: StateFlow<Int?> = _userRole

    init {
        viewModelScope.launch {
            _userRole.value = userPreferencesRepository.getUserData()?.role_id
            Log.d("data", "User role: ${_userRole.value.toString()}")
        }
    }

    fun resolveVehicleImage(rawUrl: String?) {
        viewModelScope.launch {
            Log.d("Resolving vehicle image URL", rawUrl.toString())
            _resolvedImageUrl.value = if (rawUrl.isNullOrBlank()) null else rawUrl
        }
    }

//    fun loadVehicle() {
//        viewModelScope.launch {
//            val user = userPreferencesRepository.getUserData()
//            val token = userPreferencesRepository.getAuthToken()
//            val idToUse = when (user?.role_id) {
//                3 -> DriverPrefs.getDriverId(context)
//                4 -> user.id
//                else -> null
//            }
//            Log.d("BusViewModel", "Driver ID: ${DriverPrefs.getDriverId(context).toString()}")
//            if (idToUse != null && !token.isNullOrEmpty()) {
//                vehicleRepository.getVehicleById(idToUse, token).collectLatest { vehicleResource ->
//                    _vehicle.value = vehicleResource
//                    if (vehicleResource is Resource.Success) {
//                        val driverId = vehicleResource.data.driverId
//                        fetchDriverData(driverId, token)
//                    }
//                }
//            } else {
//                _vehicle.value = Resource.Error("No valid user/driver ID or token found")
//            }
//        }
//    }

    fun loadVehicle() {
        viewModelScope.launch {
            val user = userPreferencesRepository.getUserData()
            val token = userPreferencesRepository.getAuthToken()
            val idToUse = when (user?.role_id) {
                3 -> DriverPrefs.getDriverId(context)
                4 -> user.id
                else -> null
            }

            if (idToUse != null && !token.isNullOrEmpty()) {
                // Obtener el vehículo de la base de datos local primero (Room)
                vehicleRepository.getVehicleById(idToUse, token).collectLatest { vehicleResource ->
                    _vehicle.value = vehicleResource
                    if (vehicleResource is Resource.Success) {
                        // Si se obtiene con éxito, obtener información adicional del conductor
                        val driverId = vehicleResource.data.driverId
                        fetchDriverData(driverId, token)
                    }
                }
            } else {
                _vehicle.value = Resource.Error("No valid user/driver ID or token found")
            }
        }
    }

    private fun fetchDriverData(driverId: Int, token: String) {
        viewModelScope.launch {
            _isDriverLoading.value = true
            try {
                val user: UserResponse = authRepository.getUserById(driverId, token)
                _driverFullName.value = "${user.forenames} ${user.surnames}"
                _driverPhoneNumber.value = user.phone_number
            } catch (e: Exception) {
                _driverFullName.value = null
                _driverPhoneNumber.value = null
            } finally {
                _isDriverLoading.value = false
            }
        }
    }

    fun editVehicle(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        carPic: MultipartBody.Part? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken()
            val vehicleId = (_vehicle.value as? Resource.Success)?.data?.id
            if (token != null && vehicleId != null) {
                // Llamamos a la API para actualizar el vehículo
                vehicleRepository.updateVehicle(
                    token = token,
                    id = vehicleId,
                    plate = plate,
                    model = model,
                    brand = brand,
                    year = year,
                    color = color,
                    capacity = capacity,
                    carPic = carPic
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Si la actualización es exitosa, actualizamos en Room también
                            val updatedVehicle = result.data.toEntity()  // Convertimos el VehicleResponse a VehicleEntity
                            vehicleRepository.updateVehicleInRoom(updatedVehicle)  // Actualizamos en Room

                            _vehicle.value = result
                            onResult(true, null)
                        }
                        is Resource.Error -> {
                            onResult(false, result.message)
                        }
                        else -> {}
                    }
                }
            } else {
                onResult(false, "Missing token or vehicle ID")
            }
        }
    }
}