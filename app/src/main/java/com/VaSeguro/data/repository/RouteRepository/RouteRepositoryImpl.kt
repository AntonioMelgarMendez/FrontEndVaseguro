
package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Routes.RouteResponse
import com.VaSeguro.data.remote.Route.RouteService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class RouteRepositoryImpl(
    private val routeService: RouteService
) : RouteRepository {

    private fun String.toPart(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    override suspend fun getRoutes(token: String): List<RouteResponse> {
        return routeService.getRoutes("Bearer $token")
    }

    override suspend fun getRouteById(token: String, id: Int): RouteResponse {
        return routeService.getRouteById("Bearer $token", id.toString())
    }

    override suspend fun createRoute(
        token: String,
        name: String,
        startDate: String,
        vehicleId: Int,
        statusId: Int,
        typeId: Int,
    ): RouteResponse {
        return routeService.createRoute(
            name = name.toPart(),
            startDate = startDate.toPart(),
            vehicleId = vehicleId.toString().toPart(),
            statusId = statusId.toString().toPart(),
            typeId = typeId.toString().toPart(),
            authHeader = "Bearer $token"
        )
    }

    override suspend fun updateRoute(
        token: String,
        id: Int,
        data: RouteResponse
    ): RouteResponse {
        return routeService.updateRoute(
            id = id.toString(),
            name = data.name.toPart(),
            startDate = data.start_date.toPart(),
            vehicleId = data.vehicle_id.toString().toPart(),
            statusId = data.status_id.toString().toPart(),
            typeId = data.type_id.toString().toPart(),
            authHeader = "Bearer $token"
        )
    }

    override suspend fun deleteRoute(token: String, id: Int) {
        routeService.deleteRoute(id.toString(), "Bearer $token")
    }
}