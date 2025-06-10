package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Stop.StopData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StopsAdminScreenViewModel : ViewModel() {

    private val _stops = MutableStateFlow(
        listOf(
            StopData("S01", "Stop A", "13.700", "-89.210", "School", "Juan Mendoza"),
            StopData("S02", "Stop B", "13.701", "-89.211", "House", "Pedro Torres"),
        )
    )
    val stops: StateFlow<List<StopData>> = _stops

    fun addStop(
        name: String,
        latitude: String,
        longitude: String,
        stopType: String,
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