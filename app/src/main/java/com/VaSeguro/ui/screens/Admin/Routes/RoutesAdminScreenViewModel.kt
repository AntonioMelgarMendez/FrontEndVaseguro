package com.VaSeguro.ui.screens.Admin.Routes

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.map.data.driver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RoutesAdminScreenViewModel : ViewModel(){
    val burnedVehicle = Vehicle(
        id = "VEH-002",
        plate = "P987654",
        model = "Toyota Hiace 2020",
        driver_id = driver.id,
        year = "2020",
        color = "White",
        capacity = "20",
        updated_at = "2025-06-16T09:00:00",
        carPic = "https://example.com/toyota_hiace_2020.jpg",
        created_at = "2025-06-16T09:00:00",
        brand = "Toyota",
    )
    private val _routes = MutableStateFlow(
        listOf(
            RoutesData(
                id = 1,
                name = "Ruta A",
                start_date = "2025-06-01",
                vehicle_id = burnedVehicle,
                status_id = RouteStatus(id = "1", status = "Active"),
                type_id = RouteType(id = "A", type = "School"),
                end_date = "2025-12-15",
                stopRoute = emptyList()
            ),
            RoutesData(
                id = 2,
                name = "Ruta B",
                start_date = "2025-06-10",
                vehicle_id = burnedVehicle,
                status_id = RouteStatus(id = "2", status = "Inactive"),
                type_id = RouteType(id = "B", type = "Weekend"),
                end_date = "2025-12-20",
                stopRoute = emptyList()
            )
        )
    )
    val routes: StateFlow<List<RoutesData>> = _routes

    private val _expandedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<Int, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<Int, Boolean>> = _checkedMap

    fun toggleExpand(routeId: Int) {
        _expandedMap.update { map ->
            map.toMutableMap().apply {
                this[routeId] = !(this[routeId] ?: false)
            }
        }
    }

    fun setChecked(routeId: Int, checked: Boolean) {
        _checkedMap.update { map ->
            map.toMutableMap().apply {
                this[routeId] = checked
            }
        }
    }

    fun deleteRoute(routeId: Int) {
        _routes.update { it.filterNot { route -> route.id == routeId } }
        _expandedMap.update { it - routeId }
        _checkedMap.update { it - routeId }
    }

    fun addRoute(route: RoutesData) {
        _routes.update { it + route }
    }
}