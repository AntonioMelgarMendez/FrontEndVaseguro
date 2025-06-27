package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Responses.RouteResponse
import com.VaSeguro.helpers.Resource
import com.VaSeguro.map.data.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    suspend fun getRoutes(): List<RoutesData>
    suspend fun getRouteById(id: Int): RoutesData
    suspend fun createRoute(
        name: String,
        startDate: String,
        vehicleId: VehicleMap,
        statusId: RouteStatus,
        typeId: RouteType,
    ): RoutesData
    suspend fun updateRoute(
        id: Int,
        data: RoutesData
    ): RoutesData
    suspend fun deleteRoute(id: Int)
}