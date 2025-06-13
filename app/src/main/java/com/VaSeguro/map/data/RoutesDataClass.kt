package com.VaSeguro.map.data

import com.google.android.gms.maps.model.LatLng

/**
 * Datos relacionados con la información de rutas
 */

/**
 * Representa un polyline codificado para mostrar en el mapa
 */
data class Polyline(
    val encodedPolyline: String
)

/**
 * Representa una ruta completa con información de distancia, duración y polyline
 */
data class Route(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline,
    val segments: List<RouteSegment> = emptyList()
) {
    /**
     * Nombre predeterminado de la ruta basado en la distancia
     */
    fun getRouteName(): String = "Ruta (${(distanceMeters / 1000.0).toInt()} km)"

    /**
     * Obtiene la duración estimada al próximo punto desde la posición actual
     * @param currentSegmentIndex índice del segmento actual de la ruta
     * @return duración estimada al próximo punto
     */
    fun getTimeToNextPoint(currentSegmentIndex: Int): String {
        return if (segments.isNotEmpty() && currentSegmentIndex >= 0 && currentSegmentIndex < segments.size) {
            segments[currentSegmentIndex].duration
        } else {
            duration
        }
    }

    /**
     * Obtiene el nombre del próximo punto en la ruta
     * @param currentSegmentIndex índice del segmento actual de la ruta
     * @return nombre del próximo punto o destino final
     */
    fun getNextPointName(currentSegmentIndex: Int): String {
        return if (segments.isNotEmpty() && currentSegmentIndex >= 0 && currentSegmentIndex < segments.size) {
            segments[currentSegmentIndex].endPointName
        } else {
            "destino final"
        }
    }
}

/**
 * Representa un segmento de ruta entre dos puntos
 */
data class RouteSegment(
    val startPoint: LatLng,
    val endPoint: LatLng,
    val startPointName: String = "",
    val endPointName: String = "",
    val distance: Int, // en metros
    val duration: String,
    val polyline: Polyline
)

/**
 * Punto de origen para una solicitud de ruta
 */
data class Origin(
    val location: LocationAddress
)

/**
 * Punto de destino para una solicitud de ruta
 */
data class Destination(
    val location: LocationAddress
)

/**
 * Punto intermedio para una solicitud de ruta
 */
data class Waypoint(
    val location: LocationAddress
)

/**
 * Dirección de ubicación para API de Google
 */
data class LocationAddress(
    val latLng: LatLng
) {
    companion object {
        fun fromLatLng(latLng: LatLng): LocationAddress {
            return LocationAddress(latLng)
        }
    }
}

/**
 * Representa la duración de un segmento de ruta
 */
data class Duration(
    val seconds: Long,
    val nanos: Int = 0
) {
    /**
     * Convierte la duración a un formato legible
     */
    fun toReadableFormat(): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "$hours h $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "${seconds % 60} seg"
        }
    }
}
