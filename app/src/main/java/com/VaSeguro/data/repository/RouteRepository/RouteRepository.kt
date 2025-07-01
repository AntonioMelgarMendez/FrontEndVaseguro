package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Vehicle.VehicleMap

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
    suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData
}