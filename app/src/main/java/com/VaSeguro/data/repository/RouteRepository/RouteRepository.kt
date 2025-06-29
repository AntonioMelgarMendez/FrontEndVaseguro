package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Routes.RouteResponse

interface RouteRepository {
    suspend fun getRoutes(token: String): List<RouteResponse>
    suspend fun getRouteById(token: String, id: Int): RouteResponse
    suspend fun createRoute(
        token: String,
        name: String,
        startDate: String,
        vehicleId: Int,
        statusId: Int,
        typeId: Int,
    ): RouteResponse
    suspend fun updateRoute(
        token: String,
        id: Int,
        data: RouteResponse
    ): RouteResponse
    suspend fun deleteRoute(token: String, id: Int)
}