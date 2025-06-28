package com.VaSeguro.data.remote.Stops


import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopDto
import com.VaSeguro.data.model.Stop.Stops
import retrofit2.http.*

interface StopsService {
    @GET("stops")
    suspend fun getAllStops(
        @Header("Authorization") token: String
    ): List<StopDto>

    @GET("stops/{id}")
    suspend fun getStopById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Stops

    @POST("stops")
    suspend fun createStop(
        @Body stop: Stops,
        @Header("Authorization") token: String
    ): Stops

    @PUT("stops/{id}")
    suspend fun updateStop(
        @Path("id") id: String,
        @Body stop: Stops,
        @Header("Authorization") token: String
    ): Stops

    @DELETE("stops/{id}")
    suspend fun deleteStop(
        @Path("id") id: String,
        @Header("Authorization") token: String
    )
}