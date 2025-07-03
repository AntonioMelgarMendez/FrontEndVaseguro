package com.VaSeguro.ui.screens.Parents.History

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.HistoryInfo.TripInfo
import com.google.android.gms.maps.model.LatLng
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
                    mapImageRes = null,
                    routePoints = listOf(
                        LatLng(13.692940, -89.218191), // Centro San Salvador
                        LatLng(13.698493, -89.191424), // Colonia Escalón
                        LatLng(13.703222, -89.224444), // Zona Rosa
                        LatLng(13.710000, -89.200000), // Colonia San Benito
                        LatLng(13.715000, -89.230000)  // Universidad de El Salvador
                    )
                ),
                TripInfo(
                    date = "March 11, 2025",
                    duration = "2h 20min",
                    pickupTime = "6:10 AM",
                    arrivalTime = "8:30 AM",
                    driver = "Manuel Perez",
                    bus = "Hiace 2025",
                    distance = "50,4 km",
                    mapImageRes = null,
                    routePoints = listOf(
                        LatLng(13.692940, -89.218191), // Centro San Salvador
                        LatLng(13.690000, -89.210000), // Barrio San Jacinto
                        LatLng(13.700000, -89.220000), // Colonia Médica
                        LatLng(13.705000, -89.215000), // Colonia Miramonte
                        LatLng(13.710000, -89.225000)  // Colonia San Francisco
                    )
                ),
                TripInfo(
                    date = "January 20, 2025",
                    duration = "1h 20min",
                    pickupTime = "7:10 AM",
                    arrivalTime = "9:30 AM",
                    driver = "Juan Perez",
                    bus = "Sentra 2015",
                    distance = "31,4 km",
                    mapImageRes = null,
                    routePoints = listOf(
                        LatLng(13.692940, -89.218191), // Centro San Salvador
                        LatLng(13.695000, -89.225000), // Colonia Layco
                        LatLng(13.698000, -89.230000), // Colonia Atlacatl
                        LatLng(13.700000, -89.235000), // Colonia Dolores
                        LatLng(13.705000, -89.240000)  // Colonia Santa Lucía
                    )
                )
            )
        }
    }
}