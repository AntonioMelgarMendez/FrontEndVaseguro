package com.VaSeguro.map

import com.VaSeguro.map.data.Location
import com.VaSeguro.map.data.RoutePoint
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Decodifica una cadena polyline en una lista de coordenadas LatLng.
 * Utilizado para convertir los polylines recibidos de la API de Google Maps.
 *
 * @param encoded La cadena polyline codificada
 * @return Lista de puntos LatLng
 */
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(latLng)
    }

    return poly
}

/**
 * Calcula la distancia entre dos puntos LatLng en metros usando la fórmula de Haversine
 *
 * @param start Punto inicial
 * @param end Punto final
 * @return Distancia en metros
 */
fun calculateDistance(start: LatLng, end: LatLng): Double {
    val earthRadius = 6371000.0 // Radio de la Tierra en metros

    val latDistance = Math.toRadians(end.latitude - start.latitude)
    val lonDistance = Math.toRadians(end.longitude - start.longitude)

    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

/**
 * Convierte una lista de puntos RoutePoint a formato de cadena para API de direcciones
 *
 * @param points Lista de puntos de ruta
 * @return Cadena formateada para la API de direcciones
 */
fun formatWaypointsForApi(points: List<RoutePoint>): String? {
    if (points.size <= 2) return null

    return points.subList(1, points.size - 1)
        .joinToString("|") { it.toApiString() }
}

/**
 * Formatea un tiempo en segundos a una cadena legible
 *
 * @param seconds Tiempo en segundos
 * @return Cadena formateada (ejemplo: "2 h 30 min" o "45 min")
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        hours > 0 -> "$hours h $minutes min"
        minutes > 0 -> "$minutes min"
        else -> "${seconds % 60} seg"
    }
}

/**
 * Calcula si un punto está cerca de una ruta (polilínea)
 *
 * @param point Punto a verificar
 * @param polyline Lista de puntos que forman la polilínea
 * @param toleranceMeters Distancia máxima en metros para considerar que está cerca
 * @return true si el punto está cerca de la ruta
 */
fun isPointNearPolyline(point: LatLng, polyline: List<LatLng>, toleranceMeters: Double = 50.0): Boolean {
    if (polyline.isEmpty()) return false

    for (i in 0 until polyline.size - 1) {
        val distance = distanceToLineSegment(point, polyline[i], polyline[i + 1])
        if (distance <= toleranceMeters) {
            return true
        }
    }

    return false
}

/**
 * Calcula la distancia desde un punto a un segmento de línea
 */
private fun distanceToLineSegment(point: LatLng, lineStart: LatLng, lineEnd: LatLng): Double {
    val lineLength = calculateDistance(lineStart, lineEnd)

    if (lineLength == 0.0) {
        return calculateDistance(point, lineStart)
    }

    // Cálculo de la proyección del punto en la línea
    val t = ((point.longitude - lineStart.longitude) * (lineEnd.longitude - lineStart.longitude) +
            (point.latitude - lineStart.latitude) * (lineEnd.latitude - lineStart.latitude)) /
            (lineLength * lineLength)

    when {
        t < 0 -> return calculateDistance(point, lineStart)
        t > 1 -> return calculateDistance(point, lineEnd)
        else -> {
            val projection = LatLng(
                lineStart.latitude + t * (lineEnd.latitude - lineStart.latitude),
                lineStart.longitude + t * (lineEnd.longitude - lineStart.longitude)
            )
            return calculateDistance(point, projection)
        }
    }
}

