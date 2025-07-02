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

    // NUEVO: Método para actualizar ubicación con información de ruta
    suspend fun updateLocationWithRoute(
        driverId: Int,
        lat: Double,
        lon: Double,
        encodedPolyline: String?,
        routeActive: Boolean = false,
        routeProgress: Float = 0f,
        currentSegment: Int = 0,
        routeStatus: Int? = 1
    )

    // Método para obtener la última ubicación conocida de un conductor
    suspend fun getDriverLocation(driverId: Int): LocationDriverAddress

    // NUEVO: Método para obtener ubicación completa con información de ruta
    suspend fun getDriverLocationWithRoute(driverId: Int): LocationDriverAddress

    // Método para suscribirse a los cambios de ubicación de un conductor
    fun subscribeToDriverLocationUpdates(driverId: Int): Flow<LatLng>

    // NUEVO: Método para suscribirse a cambios completos (ubicación + ruta)
    fun subscribeToDriverLocationAndRouteUpdates(driverId: Int): Flow<LocationDriverAddress>

    // Método para cancelar la suscripción
    fun unsubscribeFromLocationUpdates()
}


