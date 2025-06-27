package com.VaSeguro.data.remote.Route

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Responses.RouteResponse
import com.VaSeguro.map.data.Route
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface RouteService {
    @GET ("routes")
    suspend fun getRoutes(
        @Header("Authorization") authHeader: String
    ): List<RoutesData>

    @GET("routes/{id}")
    suspend fun getRouteById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): RoutesData

    @Multipart
    @POST("routes/")
    suspend fun createRoute(
        @Part("name") name: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("vehicle_id") vehicleId: RequestBody,
        @Part("status_id") statusId: RequestBody,
        @Part("type_id") typeId: RequestBody,
        @Header("Authorization") authHeader: String
    ): RoutesData

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
    ): RoutesData

    @DELETE("routes/{id}")
    suspend fun deleteRoute(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    )


}
