package com.VaSeguro.map.data

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

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
    @SerializedName("legs") val legs: List<RouteLegs> = emptyList(),
    val segments: List<RouteSegment> = emptyList()
) {
    /**
     * Nombre predeterminado de la ruta basado en la distancia
     */
    fun getRouteName(): String = "Ruta (${(distanceMeters / 1000.0).toInt()} km)"

    /**
     * Obtiene la duración estimada al próximo punto desde la posición actual
     * @param currentSegmentIndex índice del segmento actual de la ruta
     * @return duración estimada al próximo punto en formato legible
     */
    fun getTimeToNextPoint(currentSegmentIndex: Int): String {
        return if (segments.isNotEmpty() && currentSegmentIndex >= 0 && currentSegmentIndex < segments.size) {
            // Usar la duración formateada del segmento
            parseDurationToReadableFormat(segments[currentSegmentIndex].duration)
        } else {
            // Usar la duración formateada del total de la ruta
            parseDurationToReadableFormat(duration)
        }
    }

    /**
     * Convierte una duración en formato de Google (por ejemplo "64s", "2m30s" o "1h20m30s")
     * a un formato legible
     */
    private fun parseDurationToReadableFormat(durationString: String): String {
        var seconds = 0L
        var number = 0L
        var i = 0

        while (i < durationString.length) {
            val c = durationString[i]
            if (c.isDigit()) {
                number = number * 10 + (c - '0')
            } else {
                when (c) {
                    'h' -> seconds += number * 3600
                    'm' -> seconds += number * 60
                    's' -> seconds += number
                }
                number = 0
            }
            i++
        }

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "$hours h $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "$secs seg"
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

data class RouteLegs(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline,
    val startLocation: LocationAddress,
    val endLocation: LocationAddress,
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
