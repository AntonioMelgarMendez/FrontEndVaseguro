package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StopsAdminScreenViewModel : ViewModel() {

    private val _stops = MutableStateFlow(
        listOf(
            StopData(1, "Stop A", 13.700, -89.210),
            StopData(2, "Stop B", 13.701, -89.211),
        )
    )
    val stops: StateFlow<List<StopData>> = _stops

    fun addStop(
        name: String,
        latitude: Double,
        longitude: Double,
        stopType: StopType,
        driver: String
    ) {
        val newId = (_stops.value.maxOfOrNull { it.id } ?: 0) + 1
        val newStop = StopData(
            id = newId,
            name = name,
            latitude = latitude,
            longitude = longitude,
        )
        _stops.value = _stops.value + newStop
    }

    fun deleteStop(id: Int) {
        _stops.value = _stops.value.filterNot { it.id == id }
    }
}

