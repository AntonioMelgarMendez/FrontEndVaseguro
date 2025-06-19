package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
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

    fun addRoute(route: RoutesData)

    fun updateRoute(route: RoutesData)

    fun deleteRoute(routeId: String)

    fun getRoute(routeId: String): Flow<RoutesData?>

    suspend fun createMockRoutes(): List<RoutesData>

    // Función helper para encontrar un StopPassenger por tipo y lista de stops de un niño
    fun findStopByTypeAndChild(type: String, childStops: List<StopPassenger>): StopPassenger
}
