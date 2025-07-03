package com.VaSeguro.map.data

import com.google.android.gms.maps.model.LatLng

/**
 * Estructura de datos para la API de matriz de rutas según la documentación oficial
 */
data class MatrixWaypoint(
    val location: LatLngWrapper
)

data class LatLngWrapper(
    val latLng: LatLng2D
)

data class LatLng2D(
    val latitude: Double,
    val longitude: Double
) {
    constructor(latLng: LatLng) : this(latLng.latitude, latLng.longitude)
}

data class RouteModifiers(
    val avoidFerries: Boolean? = null,
    val avoidHighways: Boolean? = null,
    val avoidTolls: Boolean? = null
) {
    // Función para convertir a formato de API (snake_case)
    fun toApiMap(): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        avoidFerries?.let { map["avoid_ferries"] = it }
        avoidHighways?.let { map["avoid_highways"] = it }
        avoidTolls?.let { map["avoid_tolls"] = it }
        return map
    }
}

