package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StopsAdminScreenViewModel : ViewModel() {

    private val _stops = MutableStateFlow(
        listOf(
            StopData("S01", "Stop A", "13.700", "-89.210", StopType("1", "School"), "Juan Mendoza"),
            StopData("S02", "Stop B", "13.701", "-89.211",StopType("2", "House"), "Pedro Torres"),
        )
    )
    val stops: StateFlow<List<StopData>> = _stops

    fun addStop(
        name: String,
        latitude: String,
        longitude: String,
        stopType: StopType,
        driver: String
    ) {
        val newStop = StopData(
            id = System.currentTimeMillis().toString().takeLast(5),
            name = name,
            latitude = latitude,
            longitude = longitude,
            stopType = stopType,
            driver = driver
        )
        _stops.value = _stops.value + newStop
    }

    fun deleteStop(id: String) {
        _stops.value = _stops.value.filterNot { it.id == id }
    }
}