package com.VaSeguro.map.data

data class Route(
    val overview_polyline: Polyline,
    val legs: List<Leg>
)
