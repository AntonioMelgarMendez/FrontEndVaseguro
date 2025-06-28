package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class   StopsAdminScreenViewModel : ViewModel() {

    private val _stops = MutableStateFlow(
        listOf(
            StopData(
                id = 1,
                name = "Parada El Centro",
                latitude = 13.700,
                longitude = -89.210,
            ),
            StopData(
                id = 2,
                name = "Parada Zona Norte",
                latitude = 13.710,
                longitude = -89.220,
            )
        )
    )
    val stops: StateFlow<List<StopData>> = _stops

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    fun toggleExpand(stopId: String) {
        _expandedMap.update { map ->
            map.toMutableMap().apply {
                this[stopId] = !(this[stopId] ?: false)
            }
        }
    }

    fun setChecked(stopId: String, checked: Boolean) {
        _checkedMap.update { map ->
            map.toMutableMap().apply {
                this[stopId] = checked
            }
        }
    }

    fun deleteStop(stopId: String) {
        _stops.update { it.filterNot { stop -> stop.id.toString() == stopId } }
        _expandedMap.update { it - stopId }
        _checkedMap.update { it - stopId }
    }

    fun addStop(stop: StopData) {
        _stops.update { it + stop }
    }
}