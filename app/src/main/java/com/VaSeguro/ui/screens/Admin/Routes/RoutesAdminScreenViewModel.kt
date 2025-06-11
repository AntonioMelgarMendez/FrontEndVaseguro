package com.VaSeguro.ui.screens.Admin.Routes

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Routes.RouteStatus
import com.VaSeguro.data.model.Routes.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RoutesAdminScreenViewModel : ViewModel(){

    private val _routes = MutableStateFlow(
        listOf(
            RoutesData(
                id = "R01", name = "Route A", start_date = "01/01/2023", vehicule_id = "V001",
                status_id = RouteStatus("1", "Active"),
                type_id = RouteType("1", "Long Distance"),
                end_date = "01/12/2023"
            ),
            RoutesData(
                id = "R02", name = "Route B", start_date = "01/03/2023", vehicule_id = "V002",
                status_id = RouteStatus("2", "Inactive"),
                type_id = RouteType("2", "Short Distance"),
                end_date = "01/09/2023"
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
            vehicule_id = vehiculeId,
            status_id = status,
            type_id = type,
            end_date = endDate
        )
        _routes.value = _routes.value + newRoute
    }

    fun deleteRoute(id: String) {
        _routes.value = _routes.value.filterNot { it.id == id }
    }
}