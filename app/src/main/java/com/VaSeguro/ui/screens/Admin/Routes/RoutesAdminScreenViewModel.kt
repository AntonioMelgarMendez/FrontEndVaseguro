package com.VaSeguro.ui.screens.Admin.Routes

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.model.Vehicle.toVehicleMap
import com.VaSeguro.data.remote.Vehicle.toDomain
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.RouteRepository.RouteRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import com.VaSeguro.map.data.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutesAdminScreenViewModel(
    private val routeRepository: RouteRepository,
    private val vehicleRepository: VehicleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel(){
    private val _routes = MutableStateFlow<List<RoutesData>>(emptyList())
    val routes: StateFlow<List<RoutesData>> = _routes

    private val _expandedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<Int, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<Int, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    var name: String = ""
    var start_date: String = ""
    lateinit var vehicle_id: VehicleMap
    lateinit var status_id: RouteStatus
    lateinit var type_id: RouteType

    var plate: String = ""
    var routeName: String = ""
    var routeStatus: String = ""

    fun updateRouteName(routeName: String){
        name += routeName
    }
    fun updateRouteStartDate(startDate: String){
        start_date = startDate
    }
    fun updateRouteVehicleId(plate: String){
        val vehicle = vehicles.value.find { it.plate == plate }
        vehicle?.let{
            vehicle_id = it.toVehicleMap()
        }
        this.plate = plate
    }
    fun updateStatusId(value: RouteStatus){
        status_id = RouteStatus.valueOf(value.name)
        routeStatus = value.name
    }
    fun updateTypeId(value: RouteType){
        type_id = RouteType.valueOf(value.name)
        routeName = value.name
    }


    fun resetForm() {
        name = ""
        start_date = ""
        plate = ""
        routeName = ""
        routeStatus = ""
    }

    fun areFieldsValid(): Boolean {
        return  name.isNotBlank() &&
                start_date.isNotBlank() &&
                ::vehicle_id.isInitialized &&
                ::status_id.isInitialized &&
                ::type_id.isInitialized
    }


    init {
        loadVehicles()
    }

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
                    Resource.Loading -> {
                        _loading.value = true
                    }
                }
            }
        }
    }

    fun fetchAllRoutes() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val routeResponses = routeRepository.getRoutes()
                val routesDataList = routeResponses.map {
                    RoutesData(
                        id = it.id,
                        name = it.name,
                        start_date = it.start_date,
                        vehicle_id = it.vehicle_id,
                        status_id = it.status_id,
                        type_id = it.type_id,
                        end_date = it.end_date,
                        stopRoute = it.stopRoute
                    )
                }
                _routes.value = routesDataList
            } catch (e: Exception) {
                // Handle error
                _routes.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRoute(routeId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {

                routeRepository.deleteRoute(routeId)
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

    fun updateRoute(
        routeId: Int,
        data: Route
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                routeRepository.updateRoute(routeId, data)
                fetchAllRoutes()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun addRoute(

    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                routeRepository.createRoute(
                    name = name,
                    startDate = start_date,
                    vehicleId = vehicle_id,
                    statusId = status_id,
                    typeId = type_id
                )
                fetchAllRoutes()
            } catch (e: Exception) {
                // Handle errorendDate
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

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                try {
                    val application = this[APPLICATION_KEY] as MyApplication
                    RoutesAdminScreenViewModel(
                        application.appProvider.provideRoutes(),
                        application.appProvider.provideVehicleRepository(),
                        application.appProvider.provideUserPreferences(),
                        application.appProvider.provideAuthRepository()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}
