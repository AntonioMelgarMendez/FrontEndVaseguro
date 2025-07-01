package com.VaSeguro.map.repository

import androidx.compose.runtime.mutableStateOf
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.toRoutesData
import com.VaSeguro.data.model.Routes.toSave
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Routes.UpdateRouteRequest
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.toSave
import com.VaSeguro.map.services.SavedRoutesService
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class SavedRoutesRepositoryImpl(
    private val savedRoutesService: SavedRoutesService,
    private val userPreferencesRepository: UserPreferencesRepository
): SavedRoutesRepository {

    private val _savedRoutes = MutableStateFlow<List<RoutesData>>(emptyList())
    val savedRoutes: Flow<List<RoutesData>> = _savedRoutes
    private val _driverId = mutableStateOf<Int?>(null)
    val driverId: Int? get() = _driverId.value

    init {
        runBlocking {
            try {
                val userData = userPreferencesRepository.getUserData()
                val actualDriverId = userData?.id
                if (actualDriverId != null) {
                    setDriverId(actualDriverId)
                } else {
                    println("DEBUG_INIT: No se pudo obtener el driverId del usuario")
                }
            } catch (e: Exception) {
                println("DEBUG_INIT: Error al inicializar repositorio: ${e.message}")
            }
        }
    }

    /**
     * Establece el ID del conductor y carga los datos iniciales
     */
    fun setDriverId(driverId: Int) {
        _driverId.value = driverId
        runBlocking {
            loadInitialData(driverId)
        }
    }

    /**
     * Carga los datos iniciales para un conductor específico
     */
    private suspend fun loadInitialData(driverId: Int) {
        try {
            val response = savedRoutesService.getAllRoutes(driverId)
            if (response.isSuccessful) {
                val apiRoutes = response.body() ?: emptyList()
                // Convert API response to RoutesData
                val routesData = apiRoutes.map { it.toRoutesData() }
                _savedRoutes.value = routesData
                println("DEBUG_LOAD_INITIAL: ${routesData.size} rutas cargadas para driver $driverId")
            } else {
                println("DEBUG_LOAD_INITIAL: Error al cargar rutas del servidor: ${response.errorBody()?.string()}")
                _savedRoutes.value = emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG_LOAD_INITIAL: Error al cargar datos para driver $driverId: ${e.message}")
            _savedRoutes.value = emptyList()
        }
    }

    override fun getRoute(routeId: Int): Flow<RoutesData?> {
        return _savedRoutes.map { routes ->
            routes.find { it.id == routeId }
        }
    }

    // Keep only essential methods for getting routes
    override fun addRoute(route: RoutesData) {
        _savedRoutes.update { currentList ->
            currentList + route
        }
    }

    override fun updateRoute(route: RoutesData) {
        _savedRoutes.update { currentList ->
            currentList.map {
                if (it.id == route.id) route else it
            }
        }
    }

    override fun deleteRoute(routeId: Int) {
        _savedRoutes.update { currentList ->
            currentList.filter { it.id != routeId }
        }
    }

    override suspend fun saveCompletedRoute(route: RoutesData): RoutesData? {
        return try {
            val response = savedRoutesService.saveRoute(route.toSave())
            if (response.isSuccessful) {
                val savedRoute = response.body()
                savedRoute?.let { addRoute(it) }
                savedRoute
            } else {
                println("DEBUG_SAVE_COMPLETED: Error al guardar ruta completada: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_SAVE_COMPLETED: Error al guardar ruta completada: ${e.message}")
            null
        }
    }


    // NUEVO: Método para crear ruta completa usando /routes/full
    override suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData {
        return try {
            val response = savedRoutesService.createFullRoute(request, "Bearer token")
            // Convert CreateFullRouteResponse to RoutesData
            // Note: This is a simplified conversion, you might need to adjust based on your needs
            RoutesData(
                id = response.id,
                name = response.name,
                start_date = response.start_date,
                vehicle_id = VehicleMap(
                    id = response.vehicle_id,
                    plate = "",
                    driver_id = 0,
                    model = "",
                    brand = "",
                    year = "",
                    color = "",
                    capacity = "",
                    updated_at = "",
                    carPic = "",
                    created_at = ""
                ),
                status_id = RouteStatus.fromId(response.status_id),
                type_id = RouteType.fromId(response.type_id),
                end_date = response.end_date,
                encodedPolyline = "",
                stopRoute = emptyList()
            )
        } catch (e: Exception) {
            println("DEBUG_CREATE_FULL: Error al crear ruta completa: ${e.message}")
            throw e
        }
    }

    // NUEVO: Método para actualizar el estado de una ruta existente
    override suspend fun updateRouteStatus(routeId: Int, statusId: Int, endDate: String?): RoutesData? {
        return try {
            val updateRequest = UpdateRouteRequest(
                status_id = statusId,
                end_date = endDate
            )
            val response = savedRoutesService.updateRoute(routeId, updateRequest, "Bearer token")
            if (response.isSuccessful) {
                val updatedRoute = response.body()
                updatedRoute?.let { updateRoute(it) }
                updatedRoute
            } else {
                println("DEBUG_UPDATE_STATUS: Error al actualizar estado: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_UPDATE_STATUS: Error al actualizar estado: ${e.message}")
            null
        }
    }
}
