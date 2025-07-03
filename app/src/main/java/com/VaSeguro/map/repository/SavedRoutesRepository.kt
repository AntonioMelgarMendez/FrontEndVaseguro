package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RoutesDataToSave
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.model.Vehicle.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface SavedRoutesRepository {

    // MÉTODOS BÁSICOS CRUD
    suspend fun getAllRoutes(driverId: Int): List<RoutesData>

    suspend fun getRouteById(routeId: Int): RoutesData?

    suspend fun saveCompletedRoute(route: RoutesData): RoutesData?

    suspend fun deleteRoute(routeId: Int): Boolean

    // MÉTODO para crear ruta completa usando /routes/full
    suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData

    // MÉTODO para actualizar el estado de una ruta existente
    suspend fun updateRouteStatus(routeId: Int, statusId: Int, endDate: String? = null): RoutesData?

    // MÉTODOS para cerrar rutas múltiples y prevenir conflictos
    suspend fun closeAllRoutesExcept(routeId: Int, driverId: Int): List<RoutesData>?

    suspend fun closeAllRoutes(driverId: Int): List<RoutesData>?
}