package com.VaSeguro.data.model.HistoryInfo

import com.google.android.gms.maps.model.LatLng

data class TripInfo(
    val date: String,
    val duration: String,
    val pickupTime: String,
    val arrivalTime: String,
    val driver: String,
    val bus: String,
    val distance: String,
    val mapImageRes: Int? = null,
    val routePoints: List<LatLng> = emptyList()
// ej: R.drawable.route_map
)