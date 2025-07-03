package com.VaSeguro.ui.screens.Parents.History

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.R
import com.VaSeguro.data.model.HistoryInfo.TripInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryScreenViewModel : ViewModel() {
    private val _trips = MutableStateFlow<List<TripInfo>>(emptyList())
    val trips: StateFlow<List<TripInfo>> = _trips

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            _trips.value = listOf(
                TripInfo(
                    date = "April 20, 2025",
                    duration = "1h 20min",
                    pickupTime = "7:10 AM",
                    arrivalTime = "8:30 AM",
                    driver = "Juan Melgar",
                    bus = "Hiace 2025",
                    distance = "30,4 km",
                    mapImageRes = null
                ),
                TripInfo(
                    date = "March 11, 2025",
                    duration = "2h 20min",
                    pickupTime = "6:10 AM",
                    arrivalTime = "8:30 AM",
                    driver = "Manuel Perez",
                    bus = "Hiace 2025",
                    distance = "50,4 km",
                    mapImageRes = null
                ),
                TripInfo(
                    date = "January 20, 2025",
                    duration = "1h 20min",
                    pickupTime = "7:10 AM",
                    arrivalTime = "9:30 AM",
                    driver = "Juan Perez",
                    bus = "Sentra 2015",
                    distance = "31,4 km",
                    mapImageRes = null
                )
            )
        }
    }
}
