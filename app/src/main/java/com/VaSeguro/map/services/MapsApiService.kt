package com.VaSeguro.map.services

import com.VaSeguro.map.request.RouteRequest
import com.VaSeguro.map.response.PlacesResponse
import com.VaSeguro.map.response.RoutesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface MapsApiService {
    @GET("place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String
    ): Response<PlacesResponse>
}