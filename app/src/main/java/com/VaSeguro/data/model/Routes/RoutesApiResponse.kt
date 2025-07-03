package com.VaSeguro.data.model.Routes

import com.google.gson.annotations.SerializedName
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Child.ChildMap

// Response models that match the API structure
data class RoutesApiResponse(
    val id: Int,
    val name: String,
    val start_date: String,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int,
    val end_date: String?,
    val vehicles: VehicleApiResponse,
    val route_status: RouteStatusApiResponse,
    val route_types: RouteTypeApiResponse,
    val stops_route: List<StopRouteApiResponse>
)

data class VehicleApiResponse(
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

data class RouteStatusApiResponse(
    val id: Int,
    val status: String
)

data class RouteTypeApiResponse(
    val id: Int,
    val type: String
)

data class StopRouteApiResponse(
    val id: Int,
    val order: Int?,
    val state: Boolean,
    val route_id: Int,
    val created_at: String,
    val stops_passengers: StopPassengerApiResponse,
    val stops_passengers_id: Int
)

data class StopPassengerApiResponse(
    val id: Int,
    val stops: StopApiResponse,
    val stop_id: Int,
    val type_id: Int,
    val child_id: Int,
    val children: ChildApiResponse
)

data class StopApiResponse(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class ChildApiResponse(
    val id: Int,
    val gender: String,
    val surnames: String,
    val driver_id: Int,
    val forenames: String,
    val parent_id: Int,
    val birth_date: String,
    val created_at: String,
    val profile_pic: String?,
    val medical_info: String
)

// Extension function to convert API response to RoutesData
fun RoutesApiResponse.toRoutesData(): RoutesData {
    return RoutesData(
        id = this.id,
        name = this.name,
        start_date = this.start_date,
        vehicle_id = this.vehicles.toVehicleMap(),
        status_id = RouteStatus.fromId(this.status_id),
        type_id = RouteType.fromId(this.type_id),
        end_date = this.end_date ?: "",
        encodedPolyline = "",
        stopRoute = this.stops_route.map { it.toStopRoute() }
    )
}

// Extension function to convert VehicleApiResponse to VehicleMap
fun VehicleApiResponse.toVehicleMap(): VehicleMap {
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

// Extension function to convert StopRouteApiResponse to StopRoute
fun StopRouteApiResponse.toStopRoute(): StopRoute {
    return StopRoute(
        id = this.id,
        stopPassenger = this.stops_passengers.toStopPassenger(),
        order = this.order ?: 0,
        state = this.state
    )
}

// Extension function to convert StopPassengerApiResponse to StopPassenger
fun StopPassengerApiResponse.toStopPassenger(): StopPassenger {
    return StopPassenger(
        id = this.id,
        stop = this.stops.toStopData(),
        child = this.children.toChildMap(),
        stop_id = this.stop_id,
        type_id = this.type_id,
        child_id = this.child_id
    )
}

// Extension function to convert StopApiResponse to StopData
fun StopApiResponse.toStopData(): StopData {
    return StopData(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

// Extension function to convert ChildApiResponse to ChildMap
fun ChildApiResponse.toChildMap(): ChildMap {
    return ChildMap(
        id = this.id,
        forenames = this.forenames,
        surnames = this.surnames,
        birthDate = this.birth_date,
        driverId = this.driver_id,
        parentId = this.parent_id,
        medicalInfo = this.medical_info,
        createdAt = this.created_at,
        profilePic = this.profile_pic,
        gender = this.gender
    )
}
