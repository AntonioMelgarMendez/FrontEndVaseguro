package com.VaSeguro.data.model.Routes

import com.google.gson.annotations.SerializedName
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Stop.StopType

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
    val stops_route: List<StopRouteByIdResponse>,
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
    val id: Int,
    val status: String
)

data class RouteTypeByIdResponse(
    val id: Int,
    val type: String
)

// NUEVO: Modelo para StopRoute desde la API
data class StopRouteByIdResponse(
    val id: Int,
    val order: Int,
    val state: Boolean,
    val route_id: Int,
    val created_at: String,
    val stops_passengers: StopPassengerByIdResponse,
    val stops_passengers_id: Int
)

// NUEVO: Modelo para StopPassenger desde la API
data class StopPassengerByIdResponse(
    val id: Int,
    val stops: StopByIdResponse,
    val stop_id: Int,
    val type_id: Int,
    val child_id: Int,
    val children: ChildByIdResponse
)

// NUEVO: Modelo para Stop desde la API
data class StopByIdResponse(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

// NUEVO: Modelo para Child desde la API
data class ChildByIdResponse(
    val id: Int,
    val gender: String,
    val surnames: String,
    val driver_id: Int,
    val forenames: String,
    val parent_id: Int,
    val birth_date: String,
    val created_at: String,
    val profile_pic: String?,
    val medical_info: String?
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
        stopRoute = this.stops_route.map { it.toStopRoute() } // CORREGIDO: Mapear stopRoutes correctamente
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

// NUEVA: Extension function to convert StopRouteByIdResponse to StopRoute
fun StopRouteByIdResponse.toStopRoute(): StopRoute {
    return StopRoute(
        id = this.id,
        stopPassenger = this.stops_passengers.toStopPassenger(),
        order = this.order,
        state = this.state
    )
}

// NUEVA: Extension function to convert StopPassengerByIdResponse to StopPassenger
fun StopPassengerByIdResponse.toStopPassenger(): StopPassenger {
    return StopPassenger(
        id = this.id,
        stop = this.stops.toStopData(),
        type_id = this.type_id,
        child_id = this.children.id,
        child = this.children.toChildMap(),
        stop_id = this.stop_id
    )
}

// NUEVA: Extension function to convert StopByIdResponse to StopData
fun StopByIdResponse.toStopData(): StopData {
    return StopData(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

// NUEVA: Extension function to convert ChildByIdResponse to ChildMap
fun ChildByIdResponse.toChildMap(): ChildMap {
    return ChildMap(
        id = this.id,
        forenames = this.forenames,
        surnames = this.surnames,
        fullName = "${this.forenames} ${this.surnames}",
        gender = this.gender,
        birthDate = this.birth_date,
        medicalInfo = this.medical_info,
        profilePic = this.profile_pic,
        parentId = this.parent_id,
        driverId = this.driver_id,
        createdAt = this.created_at
    )
}
