package com.VaSeguro.ui.screens.Admin.Routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.Dao.Route.RouteDao
import com.VaSeguro.data.Entitys.Routes.RouteEntity
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.remote.Vehicle.toDomain
import com.VaSeguro.data.model.Routes.RouteResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.RouteRepository.RouteRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutesAdminScreenViewModel(
    private val routeRepository: RouteRepository,
    private val vehicleRepository: VehicleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val routeDao: RouteDao
) : ViewModel() {
    private val _routes = MutableStateFlow<List<RouteResponse>>(emptyList())
    val routes: StateFlow<List<RouteResponse>> = _routes

    private val _expandedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<Int, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<Int, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val CACHE_EXPIRATION_MS = 1 * 60 * 1000L

    var name: String = ""
    var start_date: String = ""
    var vehicleId: Int? = null
    var statusId: Int? = null
    var typeId: Int? = null

    var plate: String = ""
    var routeName: String = ""
    var routeStatus: String = ""

    fun updateRouteName(routeName: String) { name = routeName }
    fun updateRouteStartDate(startDate: String) { start_date = startDate }
    fun updateRouteVehicleId(plate: String) {
        val vehicle = vehicles.value.find { it.plate == plate }
        vehicle?.let { vehicleId = it.id.toIntOrNull() }
        this.plate = plate
    }
    fun updateStatusId(value: RouteStatus) {
        statusId = value.id.toInt()
        routeStatus = value.name
    }
    fun updateTypeId(value: RouteType) {
        typeId = value.id.toInt()
        routeName = value.name
    }
    fun resetForm() {
        name = ""
        start_date = ""
        plate = ""
        routeName = ""
        routeStatus = ""
        vehicleId = null
        statusId = null
        typeId = null
    }
    fun areFieldsValid(): Boolean {
        return name.isNotBlank() &&
                start_date.isNotBlank() &&
                vehicleId != null &&
                statusId != null &&
                typeId != null
    }

    init { loadVehicles() }

    private fun loadVehicles() {
        viewModelScope.launch {
            _loading.value = true
            vehicleRepository.getAllVehicles(userPreferencesRepository.getAuthToken().toString()).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _vehicles.value = resource.data.map { it.toDomain() }
                        _loading.value = false
                    }
                    is Resource.Error -> {
                        println("Error al cargar vehículos: ${resource.message}")
                        _loading.value = false
                    }
                    Resource.Loading -> { _loading.value = true }
                }
            }
        }
    }

    fun fetchAllRoutes() {
        viewModelScope.launch {
            _loading.value = true
            val cached = routeDao.getAllRoutes()
            if (cached.isNotEmpty()) {
                _routes.value = cached.map { it.toRouteResponse() }
            }
            val lastFetch = userPreferencesRepository.getLastRoutesFetchTime()
            val now = System.currentTimeMillis()
            if (lastFetch != null && now - lastFetch < CACHE_EXPIRATION_MS) {
                _loading.value = false
                return@launch
            }
            // 3. Fetch from API and update Room
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                val routeResponses = routeRepository.getRoutes(token)
                _routes.value = routeResponses
                routeDao.clearRoutes()
                routeDao.insertRoutes(routeResponses.map { it.toEntity() })
                userPreferencesRepository.setLastRoutesFetchTime(now)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    // Conversion helpers
    private fun RouteResponse.toEntity() = RouteEntity(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        end_date = this.end_date,
        vehicle_id = this.vehicle_id,
        status_id = this.status_id,
        type_id = this.type_id
    )
    private fun RouteEntity.toRouteResponse() = RouteResponse(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        end_date = this.end_date,
        vehicle_id = this.vehicle_id,
        status_id = this.status_id,
        type_id = this.type_id
    )

    fun deleteRoute(routeId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                routeRepository.deleteRoute(token, routeId)
                fetchAllRoutes()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
        _expandedMap.update { it - routeId }
        _checkedMap.update { it - routeId }
    }

    fun updateRoute(routeId: Int, data: RouteResponse) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                routeRepository.updateRoute(token, routeId, data)
                fetchAllRoutes()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun addRoute() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                routeRepository.createRoute(
                    token = token,
                    name = name,
                    startDate = start_date,
                    vehicleId = vehicleId!!,
                    statusId = statusId!!,
                    typeId = typeId!!
                )
                fetchAllRoutes()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleExpand(routeId: Int) {
        _expandedMap.update { current ->
            current.toMutableMap().apply {
                this[routeId] = !(this[routeId] ?: false)
            }
        }
    }

    fun setChecked(routeId: Int, checked: Boolean) {
        _checkedMap.update { current ->
            current.toMutableMap().apply {
                this[routeId] = checked
            }
        }
    }

    fun getVehicleById(vehicleId: Int): Vehicle? {
        return vehicles.value.find { it.id == vehicleId.toString() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                try {
                    val application = this[APPLICATION_KEY] as MyApplication
                    RoutesAdminScreenViewModel(
                        application.appProvider.provideRoutesRepository(),
                        application.appProvider.provideVehicleRepository(),
                        application.appProvider.provideUserPreferences(),
                        application.appProvider.provideAuthRepository(),
                        application.appProvider.provideRouteDao()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}