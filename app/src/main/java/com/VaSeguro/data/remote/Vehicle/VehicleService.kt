package com.VaSeguro.data.remote.Vehicle

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface VehicleService {
    @GET("vehicles/")
    suspend fun getAllVehicles(
        @Header("Authorization") authHeader: String
    ): List<VehicleResponse>
    @GET("vehicles/driver/{driver_id}")
    suspend fun getVehicleById(
        @Path("driver_id") id: Int,
        @Header("Authorization") authHeader: String
    ): VehicleResponse
    @FormUrlEncoded
    @POST("vehicles/")
    suspend fun createVehicle(
        @Field("plate") plate: String,
        @Field("model") model: String,
        @Field("brand") brand: String,
        @Field("year") year: String,
        @Field("color") color: String,
        @Field("capacity") capacity: String,
        @Field("driver_id") driverId: Int,
        @Field("car_pic") carPic: String?
    ): VehicleResponse

    @Multipart
    @PUT("vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: Int,
        @Part("plate") plate: RequestBody,
        @Part("model") model: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("year") year: RequestBody,
        @Part("color") color: RequestBody,
        @Part("capacity") capacity: RequestBody,
        @Part("driverId") driverId: RequestBody,
        @Part car_pic: MultipartBody.Part?
    ): VehicleResponse

    @DELETE("vehicles/{id}")
    suspend fun deleteVehicle( @Header("Authorization") authHeader: String ,@Path("id") id: Int)
}