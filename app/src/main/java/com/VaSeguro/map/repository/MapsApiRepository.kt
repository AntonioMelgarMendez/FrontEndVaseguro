package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route

interface MapsApiRepository {
    suspend fun searchPlaces(
        query: String
    ): List<PlaceResult>
}