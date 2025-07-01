package com.VaSeguro.data.remote.Route

import com.VaSeguro.data.model.Routes.RouteResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface RouteService {
    @GET("routes")
    suspend fun getRoutes(
        @Header("Authorization") authHeader: String
    ): List<RouteResponse>

    @GET("routes/{id}")
    suspend fun getRouteById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): RouteResponse

    @Multipart
    @POST("routes/")
    suspend fun createRoute(
        @Part("name") name: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("vehicle_id") vehicleId: RequestBody,
        @Part("status_id") statusId: RequestBody,
        @Part("type_id") typeId: RequestBody,
        @Header("Authorization") authHeader: String
    ): RouteResponse

    @Multipart
    @PUT("routes/{id}")
    suspend fun updateRoute(
        @Path("id") id: String,
        @Part("name") name: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("vehicle_id") vehicleId: RequestBody,
        @Part("status_id") statusId: RequestBody,
        @Part("type_id") typeId: RequestBody,
        @Header("Authorization") authHeader: String
    ): RouteResponse

    @DELETE("routes/{id}")
    suspend fun deleteRoute(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    )
}