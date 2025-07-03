package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.services.RoutesApiService

interface RoutesApiRepository {
    suspend fun getDirections(
        origin: String,
        destination: String,
        waypoints: String? = null
    ): List<Route>
}