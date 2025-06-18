package com.VaSeguro.map.request

import com.google.gson.annotations.SerializedName

data class RouteMatrixRequest(
    val origins: List<RouteMatrixOrigin>,
    val destinations: List<RouteMatrixDestination>,
    @SerializedName("travelMode")
    val travelMode: String = "DRIVE",
    @SerializedName("routingPreference")
    val routingPreference: String = "TRAFFIC_AWARE"
)

data class RouteMatrixOrigin(
    val waypoint: RouteMatrixWaypoint,
    val routeModifiers: RouteModifiers? = null
)

data class RouteMatrixDestination(
    val waypoint: RouteMatrixWaypoint
)

data class RouteMatrixWaypoint(
    val location: RouteMatrixLocation
)

data class RouteMatrixLocation(
    val latLng: LatLngPoint
)

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

data class RouteModifiers(
    @SerializedName("avoid_ferries")
    val avoidFerries: Boolean? = null,
    @SerializedName("avoid_highways")
    val avoidHighways: Boolean? = null,
    @SerializedName("avoid_tolls")
    val avoidTolls: Boolean? = null
)
