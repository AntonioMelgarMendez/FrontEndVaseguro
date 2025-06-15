package com.VaSeguro.data.remote.Vehicle

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface VehicleService {
    @GET("vehicles/")
    suspend fun getAllVehicles(): List<VehicleResponse>

    @GET("vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): VehicleResponse

    @Multipart
    @POST("vehicles/")
    suspend fun createVehicle(
        @Part("plate") plate: RequestBody,
        @Part("model") model: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("year") year: RequestBody,
        @Part("color") color: RequestBody,
        @Part("capacity") capacity: RequestBody,
        @Part("driver_id") driverId: RequestBody,
        @Part car_pic: MultipartBody.Part?
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
    suspend fun deleteVehicle(@Path("id") id: Int)
}