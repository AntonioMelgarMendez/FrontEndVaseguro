package com.VaSeguro.map.data

import com.google.android.gms.maps.model.LatLng

/**
 * Clase para configurar puntos de ruta
 */
data class RoutePoint(
    val location: LatLng,
    val name: String = "",
    val isWaypoint: Boolean = false
) {
    /**
     * Convierte un punto de ruta a formato de string para API
     */
    fun toApiString(): String = "${location.latitude},${location.longitude}"

    /**
     * Convierte el punto a objeto Location
     */
    fun toLocation(): Location = Location.fromLatLng(location)

    companion object {
        /**
         * Crea un RoutePoint desde un objeto LatLng
         */
        fun fromLatLng(latLng: LatLng, name: String = ""): RoutePoint {
            return RoutePoint(latLng, name)
        }

        /**
         * Crea un RoutePoint desde un objeto Location
         */
        fun fromLocation(location: Location, name: String = ""): RoutePoint {
            return RoutePoint(location.toLatLng(), name)
        }
    }
}
