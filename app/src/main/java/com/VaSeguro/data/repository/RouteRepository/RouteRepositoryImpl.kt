package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Route.RouteService
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class RouteRepositoryImpl(
    private val routeService: RouteService,
    private val userPreferencesRepository: UserPreferencesRepository
) : RouteRepository {

    private suspend fun getAuthHeader(): String =
        "Bearer ${userPreferencesRepository.getAuthToken().orEmpty()}"

    private fun String.toPart(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    override suspend fun getRoutes(): List<RoutesData> {
        return routeService.getRoutes(getAuthHeader())
    }

    override suspend fun getRouteById(id: Int): RoutesData {
        return routeService.getRouteById(getAuthHeader(), id.toString())
    }

    override suspend fun createRoute(
        name: String,
        startDate: String,
        vehicleId: VehicleMap,
        statusId: RouteStatus,
        typeId: RouteType,
    ): RoutesData {
        return routeService.createRoute(
            name = name.toPart(),
            startDate = startDate.toPart(),
            vehicleId = vehicleId.id.toString().toPart(),
            statusId = statusId.id.toString().toPart(),
            typeId = typeId.id.toString().toPart(),
            authHeader = getAuthHeader()
        )
    }

    override suspend fun updateRoute(
        id: Int,
        data: RoutesData
    ): RoutesData {
        return routeService.updateRoute(
            id = id.toString(),
            name = data.name.toRequestBody("text/plain".toMediaTypeOrNull()),
            startDate = data.start_date.toRequestBody("text/plain".toMediaTypeOrNull()),
            vehicleId = data.vehicle_id.id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            statusId = data.status_id.id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            typeId = data.type_id.id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            authHeader = getAuthHeader()
        )
    }
    override suspend fun deleteRoute(id: Int) {
        routeService.deleteRoute(id.toString(), getAuthHeader())
    }

    override suspend fun createFullRoute(request: CreateFullRouteRequest): RoutesData {
        return routeService.createFullRoute(
            request = request,
            authHeader = getAuthHeader()
        )
    }
}