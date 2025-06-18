package com.VaSeguro.data.model.Routes

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle

data class RoutesData (
    val id: String,
    val name: String,
    val start_date: String,
    val vehicle_id: Vehicle,
    val status_id: RouteStatus,
    val type_id: RouteType,
    val end_date: String,
    val stopRoute: List<StopRoute>
)