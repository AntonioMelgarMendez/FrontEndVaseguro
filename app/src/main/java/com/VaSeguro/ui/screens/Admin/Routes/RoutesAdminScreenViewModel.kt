package com.VaSeguro.ui.screens.Admin.Routes

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Routes.RouteStatus
import com.VaSeguro.data.model.Routes.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RoutesAdminScreenViewModel : ViewModel(){

    private val _routes = MutableStateFlow(
        listOf(
            RoutesData(
                id = "R001",
                name = "Ruta A",
                start_date = "2025-06-01",
                vehicule_id = "12451",
                status_id = RouteStatus(id = "1", status = "Active"),
                type_id = RouteType(id = "A", type = "School"),
                end_date = "2025-12-15"
            ),
            RoutesData(
                id = "R002",
                name = "Ruta B",
                start_date = "2025-06-10",
                vehicule_id = "12452",
                status_id = RouteStatus(id = "2", status = "Inactive"),
                type_id = RouteType(id = "B", type = "Weekend"),
                end_date = "2025-12-20"
            )
        )
    )
    val routes: StateFlow<List<RoutesData>> = _routes

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    fun toggleExpand(routeId: String) {
        _expandedMap.update { map ->
            map.toMutableMap().apply {
                this[routeId] = !(this[routeId] ?: false)
            }
        }
    }

    fun setChecked(routeId: String, checked: Boolean) {
        _checkedMap.update { map ->
            map.toMutableMap().apply {
                this[routeId] = checked
            }
        }
    }

    fun deleteRoute(routeId: String) {
        _routes.update { it.filterNot { route -> route.id == routeId } }
        _expandedMap.update { it - routeId }
        _checkedMap.update { it - routeId }
    }

    fun addRoute(route: RoutesData) {
        _routes.update { it + route }
    }
}