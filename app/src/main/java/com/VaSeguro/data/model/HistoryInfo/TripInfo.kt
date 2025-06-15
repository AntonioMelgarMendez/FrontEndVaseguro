package com.VaSeguro.data.model.HistoryInfo

data class TripInfo(
    val date: String,
    val duration: String,
    val pickupTime: String,
    val arrivalTime: String,
    val driver: String,
    val bus: String,
    val distance: String,
    val mapImageRes: Int? = null // ej: R.drawable.route_map
)