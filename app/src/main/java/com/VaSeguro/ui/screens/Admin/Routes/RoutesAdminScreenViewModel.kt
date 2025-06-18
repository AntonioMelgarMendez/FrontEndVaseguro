package com.VaSeguro.ui.screens.Admin.Routes

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RoutesAdminScreenViewModel : ViewModel(){

    val driverRole = UserRole(
        id = 1,
        role_name = "Driver"
    )

    val driver = UserData(
        id = "USR-001",
        forename = "Carlos",
        surname = "Ramírez",
        email = "carlos.ramirez@example.com",
        phoneNumber = "+50312345678",
        profilePic = null,
        role_id = driverRole,
        gender = "Male"
    )

    val burnedVehicle = Vehicle(
        id = "VEH-002",
        plate = "P987654",
        model = "Toyota Hiace 2020",
        driver_id = driver,
        created_at = "2025-06-16T09:00:00"
    )

    private val _routes = MutableStateFlow(
        listOf(
            RoutesData(
                id = "R01", name = "Route A", start_date = "01/01/2023", vehicle_id = burnedVehicle,
                status_id = RouteStatus.ON_PROGRESS,
                type_id = RouteType.INBOUND,
                end_date = "01/12/2023",
                stopRoute = emptyList()
            ),
            RoutesData(
                id = "R02", name = "Route B", start_date = "01/03/2023", vehicle_id = burnedVehicle,
                status_id = RouteStatus.FINISHED,
                type_id = RouteType.OUTBOUND,
                end_date = "01/09/2023",
                stopRoute = emptyList()
            )
        )
    )
    val routes: StateFlow<List<RoutesData>> = _routes

    fun addRoute(
        name: String,
        startDate: String,
        vehiculeId: String,
        status: RouteStatus,
        type: RouteType,
        endDate: String
    ) {
        val newRoute = RoutesData(
            id = System.currentTimeMillis().toString().takeLast(5),
            name = name,
            start_date = startDate,
            vehicle_id = burnedVehicle,
            status_id = status,
            type_id = type,
            end_date = endDate,
            stopRoute = emptyList()
        )
        _routes.value = _routes.value + newRoute
    }

    fun deleteRoute(id: String) {
        _routes.value = _routes.value.filterNot { it.id == id }
    }
}