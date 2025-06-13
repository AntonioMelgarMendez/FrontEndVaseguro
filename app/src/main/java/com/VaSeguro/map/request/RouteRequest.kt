package com.VaSeguro.map.request

import com.VaSeguro.map.data.Waypoint

data class RouteRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val intermediates: List<Waypoint> = emptyList(),
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE"
)
