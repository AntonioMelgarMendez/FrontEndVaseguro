package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route

interface MapsApiRepository {
    suspend fun getDirections(
        origin: String,
        destination: String,
        waypoints: String? = null
    ): List<Route>
    suspend fun searchPlaces(
        query: String
    ): List<PlaceResult>
}