package com.VaSeguro.data.model.Routes

data class RoutesData (
    val id: String,
    val name: String,
    val start_date: String,
    val vehicule_id: String,
    val status_id: RouteStatus,
    val type_id: RouteType,
    val end_date: String,
)