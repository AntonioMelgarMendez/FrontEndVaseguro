package com.VaSeguro.data.model.Routes

data class RouteResponse(
    val id: Int,
    val name: String,
    val start_date: String,
    val end_date: String?,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
)