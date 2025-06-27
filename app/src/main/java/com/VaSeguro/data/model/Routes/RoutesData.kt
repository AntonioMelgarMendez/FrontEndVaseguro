package com.VaSeguro.data.model.Routes

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleMap

data class RoutesData (
    var id: Int,
    var name: String,
    var start_date: String,
    var vehicle_id: VehicleMap,
    var status_id: RouteStatus,
    var type_id: RouteType,
    var end_date: String,
    var stopRoute: List<StopRoute>
)