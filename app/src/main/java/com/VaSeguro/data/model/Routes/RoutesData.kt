package com.VaSeguro.data.model.Routes

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleMap

data class RoutesData (
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: VehicleMap,
    val status_id: RouteStatus,
    val type_id: RouteType,
    val end_date: String,
    val encodedPolyline: String = "",
    val stopRoute: List<StopRoute>
)
fun RoutesData.toSave(): RoutesDataToSave {
    return RoutesDataToSave(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        vehicle_id = this.vehicle_id.id,
        status_id = this.status_id.id.toInt(),
        type_id = this.type_id.id.toInt(),
        end_date = this.end_date,
        stopRoute = this.stopRoute.map { it.id }
    )
}
data class RoutesDataToSave (
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
    val end_date: String,
    val stopRoute: List<Int>
)

data class CreateStopRouteRequest(
    val stopPassengerId: Int,
    val order: Int,
    val state: Boolean
)

// Response del endpoint /routes/full
data class CreateFullRouteResponse(
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
    val end_date: String
)

// Nuevo modelo específico para actualizar rutas (PUT /routes/{routeId})
data class UpdateRouteRequest(
    val status_id: Int,
    val end_date: String? = null
)

