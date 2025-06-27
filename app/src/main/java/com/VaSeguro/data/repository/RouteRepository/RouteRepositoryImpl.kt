package com.VaSeguro.data.repository.RouteRepository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Responses.RouteResponse
import com.VaSeguro.data.remote.Route.RouteService
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.map.data.Route

class RouteRepositoryImpl(
    private val routeService: RouteService,
    private val userPreferencesRepository: UserPreferencesRepository
) : RouteRepository {

    private suspend fun getAuthHeader(): String =
        "Bearer ${userPreferencesRepository.getAuthToken().orEmpty()}"

    override suspend fun getRoutes(): List<RoutesData> {
        return routeService.getRoutes(getAuthHeader())
    }

    override suspend fun getRouteById(id: Int): RoutesData {
        return routeService.getRouteById(getAuthHeader(), id)
    }

    override suspend fun createRoute(
        name: String,
        startDate: String,
        vehicleId: VehicleMap,
        statusId: RouteStatus,
        typeId: RouteType,
    ): Route {
        return routeService.createRoute(
            name, startDate, vehicleId, statusId, typeId, getAuthHeader()
        )
    }

    override suspend fun updateRoute(
        id: Int,
        data: Route
    ): Route {
        return routeService.updateRoute(id, data, getAuthHeader())
    }

    override suspend fun deleteRoute(id: Int) {
        return routeService.deleteRoute(id, getAuthHeader())
    }
}