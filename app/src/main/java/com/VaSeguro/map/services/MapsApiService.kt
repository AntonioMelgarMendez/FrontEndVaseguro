package com.VaSeguro.map.services

import com.VaSeguro.map.response.DirectionsResponse
import com.VaSeguro.map.response.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApiService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String? = null
    ): Response<DirectionsResponse>
    @GET("place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String
    ): Response<PlacesResponse>
}