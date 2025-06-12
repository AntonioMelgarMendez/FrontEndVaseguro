package com.VaSeguro.data.remote.Vehicle

import com.VaSeguro.data.remote.Responses.VehicleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface VehicleService {
  @GET("vehicles")
  suspend fun getVehicles(): List<VehicleResponse>

  @GET("vehicles/{id}")
  suspend fun getVehicleById(
    @Path("id") id: String
  ): VehicleResponse

  @Multipart
  @POST("vehicles")
  suspend fun createVehicle(
    @Part("plate") plate: RequestBody,
    @Part("driver_id") driverId: RequestBody,
    @Part("model") model: RequestBody,
    @Part("brand") brand: RequestBody,
    @Part("year") year: RequestBody,
    @Part("color") color: RequestBody,
    @Part("capacity") capacity: RequestBody,
    @Part carPic: MultipartBody.Part?
  ): VehicleResponse

  @Multipart
  @PUT("vehicles/{id}")
  suspend fun updateVehicle(
    @Path("id") id: String,
    @Part("plate") plate: RequestBody,
    @Part("driver_id") driverId: RequestBody,
    @Part("model") model: RequestBody,
    @Part("brand") brand: RequestBody,
    @Part("year") year: RequestBody,
    @Part("color") color: RequestBody,
    @Part("capacity") capacity: RequestBody,
    @Part carPic: MultipartBody.Part?
  ): VehicleResponse

  @DELETE("vehicles/{id}")
  suspend fun deleteVehicle(
    @Path("id") id: String
  ): Response<Unit>
}