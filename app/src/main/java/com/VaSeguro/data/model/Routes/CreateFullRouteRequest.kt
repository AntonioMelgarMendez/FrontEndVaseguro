package com.VaSeguro.data.model.Routes

data class CreateFullRouteRequest(
    val name: String,
    val start_date: String,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
    val stopRoute: List<CreateStopRouteRequest>
)