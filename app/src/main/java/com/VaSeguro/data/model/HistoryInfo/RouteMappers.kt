package com.VaSeguro.data.model.HistoryInfo

import com.VaSeguro.data.model.Routes.RoutesData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration


fun RoutesData.toTripInfo(
    driverName: String,
    mapImage: Int,
    distanceKm: String
): TripInfo {
    return TripInfo(
        date = start_date.substringBefore(" "),
        duration = calcularDuracion(start_date, end_date),
        pickupTime = start_date.substringAfter(" "),
        arrivalTime = end_date.substringAfter(" "),
        driver = driverName,
        bus = vehicule_id,
        distance = distanceKm,
        mapImageRes = mapImage
    )
}

// Función de utilidad para calcular duración
fun calcularDuracion(start: String, end: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val startDateTime = LocalDateTime.parse(start, formatter)
    val endDateTime = LocalDateTime.parse(end, formatter)
    val duration = Duration.between(startDateTime, endDateTime)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return "${hours}h ${minutes}min"
}