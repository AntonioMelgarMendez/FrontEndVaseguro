package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route

interface LocationRepository {
    suspend fun updateLocation(
        driverId: Int, lat: Double, lon: Double
    )
}