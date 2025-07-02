package com.VaSeguro.data.model.Routes

import com.google.gson.annotations.SerializedName
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Vehicle.VehicleMap

// Specific response model for getRouteById endpoint
data class RouteByIdResponse(
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
    val end_date: String?,
    val vehicles: VehicleByIdResponse,
    val route_status: RouteStatusByIdResponse,
    val route_types: RouteTypeByIdResponse
)

data class VehicleByIdResponse(
    val id: Int,
    val year: Int,
    val brand: String,
    val color: String,
    val model: String,
    val plate: String,
    val car_pic: String?,
    val capacity: Int,
    val driver_id: Int,
    @SerializedName("update_at")
    val update_at: String,
    val created_at: String
)

data class RouteStatusByIdResponse(
    val status: String
)

data class RouteTypeByIdResponse(
    val type: String
)

// Extension function to convert RouteByIdResponse to RoutesData
fun RouteByIdResponse.toRoutesData(): RoutesData {
    return RoutesData(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        vehicle_id = this.vehicles.toVehicleMap(),
        status_id = RouteStatus.fromId(this.status_id),
        type_id = RouteType.fromId(this.type_id),
        end_date = this.end_date ?: "",
        encodedPolyline = "",
        stopRoute = emptyList() // No stops included in this response
    )
}

// Extension function to convert VehicleByIdResponse to VehicleMap
fun VehicleByIdResponse.toVehicleMap(): VehicleMap {
    return VehicleMap(
        id = this.id,
        plate = this.plate,
        driver_id = this.driver_id,
        model = this.model,
        brand = this.brand,
        year = this.year.toString(),
        color = this.color,
        capacity = this.capacity.toString(),
        updated_at = this.update_at,
        carPic = this.car_pic ?: "",
        created_at = this.created_at
    )
}
