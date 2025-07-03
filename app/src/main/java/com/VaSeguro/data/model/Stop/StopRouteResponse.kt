package com.VaSeguro.data.model.Stop

data class StopRouteResponse(
    val id: Int,
    val created_at: String,
    val stops_passengers_id: Int,
    val route_id: Int,
    val order: Int?,
    val state: Boolean,
    val stops_passengers: StopPassengerResponse,
    val routes: RouteResponse
)

data class StopPassengerResponse(
    val id: Int,
    val stops: StopInfoResponse,
    val stop_id: Int,
    val type_id: Int,
    val child_id: Int
)

data class StopInfoResponse(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class RouteResponse(
    val id: Int,
    val name: String,
    val type_id: Int,
    val end_date: String?,
    val status_id: Int,
    val start_date: String,
    val vehicle_id: Int
)
