package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.LocationAddress
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun updateLocation(
        driverId: Int, lat: Double, lon: Double
    )

    // Método para obtener la última ubicación conocida de un conductor
    suspend fun getDriverLocation(driverId: Int): LocationDriverAddress

    // Método para suscribirse a los cambios de ubicación de un conductor
    fun subscribeToDriverLocationUpdates(driverId: Int): Flow<LatLng>

    // Método para cancelar la suscripción
    fun unsubscribeFromLocationUpdates()
}

