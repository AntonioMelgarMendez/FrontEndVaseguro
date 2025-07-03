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
    val end_date: String?, // Cambiado a nullable
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
    val end_date: String?, // Cambiado a nullable
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
    val end_date: String? // Cambiado a nullable
)

// Nuevo modelo específico para actualizar rutas (PUT /routes/{routeId})
data class UpdateRouteRequest(
    val status_id: Int,
    val end_date: String? = null
)

// Nuevo modelo para manejar la respuesta del backend donde vehicle_id es un número
data class RoutesDataResponse(
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: Int, // Aquí es número, no objeto
    val status_id: Int,
    val type_id: Int,
    val end_date: String?
)

// Extension function para convertir RoutesDataResponse a RoutesData
fun RoutesDataResponse.toRoutesData(vehicleMap: VehicleMap): RoutesData {
    return RoutesData(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        vehicle_id = vehicleMap,
        status_id = RouteStatus.entries.find { it.id == this.status_id } ?: RouteStatus.NO_INIT,
        type_id = RouteType.entries.find { it.id == this.type_id } ?: RouteType.INBOUND,
        end_date = this.end_date,
        encodedPolyline = "",
        stopRoute = emptyList()
    )
}
