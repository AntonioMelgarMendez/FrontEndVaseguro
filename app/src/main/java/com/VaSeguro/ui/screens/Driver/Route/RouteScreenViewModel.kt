package com.VaSeguro.ui.screens.Driver.Route

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.repository.MapsApiRepository
import com.VaSeguro.map.services.MapsApiService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RouteScreenViewModel(private val mapsApiRepository: MapsApiRepository) : ViewModel() {
    private val _routePoints = mutableStateListOf<LatLng>()
    val routePoints: List<LatLng> get() = _routePoints

    private val _selectedRoute = mutableStateOf<Route?>(null)
    val selectedRoute: Route? get() = _selectedRoute.value

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value



    fun addRoutePoint(point: LatLng) {
        _routePoints.add(point)
    }

    fun clearRoute() {
        _routePoints.clear()
        _selectedRoute.value = null
    }
    suspend fun searchPlaces(query: String): List<PlaceResult> {
        return try {
            mapsApiRepository.searchPlaces(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun removeRoutePoint(point: LatLng) {
        _routePoints.remove(point)
    }

    fun calculateRoute() {
        if (_routePoints.size < 2) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val origin = "${_routePoints.first().latitude},${_routePoints.first().longitude}"
                val destination = "${_routePoints.last().latitude},${_routePoints.last().longitude}"
                val waypoints = if (_routePoints.size > 2) {
                    _routePoints.subList(1, _routePoints.size - 1)
                        .joinToString("|") { "${it.latitude},${it.longitude}" }
                } else null

                val response = mapsApiRepository.getDirections(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints
                )

                if (response.isNotEmpty()) {
                    _selectedRoute.value = response.firstOrNull()
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplication = this[APPLICATION_KEY] as MyApplication
                RouteScreenViewModel(
                    aplication.appProvider.provideMapsApiRepository()
                )
            }
        }
    }
}