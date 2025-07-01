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

    fun addRoute(route: RoutesData)

    fun updateRoute(route: RoutesData)

    fun deleteRoute(routeId: Int)

    fun getRoute(routeId: Int): Flow<RoutesData?>



    // NUEVOS MÉTODOS PARA GUARDAR RUTAS COMPLETADAS
    suspend fun saveCompletedRoute(route: RoutesData): RoutesData?


    // NUEVO: Método para crear ruta completa usando /routes/full
    suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData

    // NUEVO: Método para actualizar el estado de una ruta existente
    suspend fun updateRouteStatus(routeId: Int, statusId: Int, endDate: String? = null): RoutesData?
}