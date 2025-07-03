package com.VaSeguro.map.services

import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RoutesDataToSave
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopRouteToSave
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StopPassengerService {

    @GET("stops-passenger/driver/{id}")
    suspend fun getStopPassengersByDriver(
        @Path("id") driverId: Int,
        @Header("Authorization") authHeader: String

    ): Response<List<StopPassenger>>

    @PUT("stop-routes/{stopPassengerId}/{stopRouteId}")
    suspend fun updateStopRouteState(
        @Path("stopPassengerId") stopPassengerId: Int,
        @Path("stopRouteId") stopRouteId: Int,
        @Header("Authorization") authHeader: String,
        @Body stateUpdate: Map<String, Boolean>
    ): Response<StopRoute>

}
