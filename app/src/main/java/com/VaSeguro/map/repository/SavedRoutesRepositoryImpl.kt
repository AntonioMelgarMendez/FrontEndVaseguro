package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RoutesDataResponse
import com.VaSeguro.data.model.Routes.toRoutesData
import com.VaSeguro.data.model.Routes.toSave
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Routes.UpdateRouteRequest
import com.VaSeguro.data.model.Routes.RouteByIdResponse
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

    private suspend fun getAuthHeader(): String =
        "Bearer ${userPreferencesRepository.getAuthToken().orEmpty()}"

    override suspend fun getAllRoutes(driverId: Int): List<RoutesData> {
        return try {
            val response = savedRoutesService.getAllRoutes(driverId)
            if (response.isSuccessful) {
                val apiRoutes = response.body() ?: emptyList()
                apiRoutes.map { it.toRoutesData() }
            } else {
                println("DEBUG_GET_ALL_ROUTES: Error del servidor: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG_GET_ALL_ROUTES: Error al obtener rutas: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getRouteById(routeId: Int): RoutesData? {
        return try {
            val response = savedRoutesService.getRouteById(routeId)
            if (response.isSuccessful) {
                val routeResponse = response.body()
                routeResponse?.toRoutesData()
            } else {
                println("DEBUG_GET_ROUTE_BY_ID: Error del servidor: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_GET_ROUTE_BY_ID: Error al obtener ruta: ${e.message}")
            null
        }
    }

    override suspend fun saveCompletedRoute(route: RoutesData): RoutesData? {
        return try {
            val response = savedRoutesService.saveRoute(route.toSave())
            if (response.isSuccessful) {
                response.body()
            } else {
                println("DEBUG_SAVE_COMPLETED: Error al guardar ruta: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_SAVE_COMPLETED: Error al guardar ruta: ${e.message}")
            null
        }
    }

    override suspend fun deleteRoute(routeId: Int): Boolean {
        return try {
            // Aquí puedes implementar el endpoint de eliminación cuando esté disponible
            // Por ahora retornamos true como placeholder
            println("DEBUG_DELETE_ROUTE: Eliminando ruta ID: $routeId")
            true
        } catch (e: Exception) {
            println("DEBUG_DELETE_ROUTE: Error al eliminar ruta: ${e.message}")
            false
        }
    }

    override suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData {
        return try {
            val response = savedRoutesService.createFullRoute(request, getAuthHeader())
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

    override suspend fun updateRouteStatus(routeId: Int, statusId: Int, endDate: String?): RoutesData? {
        return try {
            val updateRequest = UpdateRouteRequest(
                status_id = statusId,
                end_date = endDate
            )
            val response = savedRoutesService.updateRoute(routeId, updateRequest, getAuthHeader())
            if (response.isSuccessful) {
                val routeResponse = response.body()
                routeResponse?.let { routesDataResponse ->
                    val vehicleMap = VehicleMap(
                        id = routesDataResponse.vehicle_id,
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
                    )
                    routesDataResponse.toRoutesData(vehicleMap)
                }
            } else {
                println("DEBUG_UPDATE_STATUS: Error al actualizar estado: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_UPDATE_STATUS: Error al actualizar estado: ${e.message}")
            null
        }
    }

    override suspend fun closeAllRoutesExcept(routeId: Int, driverId: Int): List<RoutesData>? {
        return try {
            println("DEBUG_CLOSE_EXCEPT: Cerrando todas las rutas excepto ID: $routeId para driver: $driverId")
            val response = savedRoutesService.closeAllRoutesExcept(routeId, driverId, getAuthHeader())

            if (response.isSuccessful) {
                val closedRoutes = response.body()?.mapNotNull { routeResponse ->
                    try {
                        val vehicleMap = VehicleMap(
                            id = routeResponse.vehicle_id,
                            plate = "",
                            driver_id = driverId,
                            model = "",
                            brand = "",
                            year = "",
                            color = "",
                            capacity = "",
                            updated_at = "",
                            carPic = "",
                            created_at = ""
                        )
                        routeResponse.toRoutesData(vehicleMap)
                    } catch (e: Exception) {
                        println("DEBUG_CLOSE_EXCEPT: Error al convertir ruta: ${e.message}")
                        null
                    }
                } ?: emptyList()

                println("DEBUG_CLOSE_EXCEPT: ${closedRoutes.size} rutas cerradas exitosamente para driver $driverId")
                closedRoutes
            } else {
                println("DEBUG_CLOSE_EXCEPT: Error del servidor: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_CLOSE_EXCEPT: Error al cerrar rutas: ${e.message}")
            null
        }
    }

    override suspend fun closeAllRoutes(driverId: Int): List<RoutesData>? {
        return try {
            println("DEBUG_CLOSE_ALL: Cerrando todas las rutas para driver: $driverId")
            val response = savedRoutesService.closeAllRoutes(driverId, getAuthHeader())

            if (response.isSuccessful) {
                val closedRoutes = response.body()?.mapNotNull { routeResponse ->
                    try {
                        val vehicleMap = VehicleMap(
                            id = routeResponse.vehicle_id,
                            plate = "",
                            driver_id = driverId,
                            model = "",
                            brand = "",
                            year = "",
                            color = "",
                            capacity = "",
                            updated_at = "",
                            carPic = "",
                            created_at = ""
                        )
                        routeResponse.toRoutesData(vehicleMap)
                    } catch (e: Exception) {
                        println("DEBUG_CLOSE_ALL: Error al convertir ruta: ${e.message}")
                        null
                    }
                } ?: emptyList()

                println("DEBUG_CLOSE_ALL: ${closedRoutes.size} rutas cerradas exitosamente para driver $driverId")
                closedRoutes
            } else {
                println("DEBUG_CLOSE_ALL: Error del servidor: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG_CLOSE_ALL: Error al cerrar rutas: ${e.message}")
            null
        }
    }
}
