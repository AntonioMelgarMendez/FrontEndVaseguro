package com.VaSeguro.map.services

import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RoutesDataToSave
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopRouteResponse
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

interface StopRouteService {

    @GET("stop-routes/active/{childId}")
    suspend fun getStopRoutesActiveByChild(
        @Path("childId") childId: Int,
        @Header("Authorization") authHeader: String
    ): Response<List<StopRouteResponse>>

}
