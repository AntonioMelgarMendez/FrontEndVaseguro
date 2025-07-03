package com.VaSeguro.data.remote.Responses

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.VehicleMap

data class RouteResponse (
    val name: String,
    val start_date: String,
    val vehicle_id: VehicleMap,
    val status_id: RouteStatus,
    val type_id: RouteType,
    val end_date: String,
)