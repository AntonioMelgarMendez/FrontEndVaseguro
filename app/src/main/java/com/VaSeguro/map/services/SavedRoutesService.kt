package com.VaSeguro.map.services

import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RoutesDataResponse
import com.VaSeguro.data.model.Routes.RoutesDataToSave
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Routes.CreateFullRouteResponse
import com.VaSeguro.data.model.Routes.UpdateRouteRequest
import com.VaSeguro.data.model.Routes.RoutesApiResponse
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopRouteToSave
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SavedRoutesService {
    @POST("routes")
    suspend fun saveRoute(
        @Body route: RoutesDataToSave
    ): Response<RoutesData>


    @GET("routes/driver/{driverId}")
    suspend fun getAllRoutes(
        @Path("driverId") driverId: Int
    ): Response<List<RoutesApiResponse>>

    @GET("routes/{id}")
    suspend fun getRouteById(
        @Path("id") routeId: Int
    ): Response<RoutesData>

    @POST("routes/full")
    suspend fun createFullRoute(
        @Body request: CreateFullRouteRequest,
        @Header("Authorization") authHeader: String
    ): CreateFullRouteResponse

    @PUT("routes/{routeId}")
    suspend fun updateRoute(
        @Path("routeId") routeId: Int,
        @Body request: UpdateRouteRequest,
        @Header("Authorization") authHeader: String
    ): Response<RoutesDataResponse>

        @PUT("routes/close-all-except/{id}")
        suspend fun closeAllRoutesExcept(
            @Path("id") routeId: Int,
            @Query("driverId") driverId: Int,
            @Header("Authorization") authHeader: String
        ): Response<List<RoutesDataResponse>>

        @PUT("routes/close-all")
        suspend fun closeAllRoutes(
            @Query("driverId") driverId: Int,
            @Header("Authorization") authHeader: String
        ): Response<List<RoutesDataResponse>>
}
