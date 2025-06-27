package com.VaSeguro.ui.screens.Parents.Map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.ui.screens.Driver.Route.RouteScreenViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val savedRoutesRepository: SavedRoutesRepository
) : ViewModel() {

    // Estado para la posición actual del conductor
    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()

    // Estado para la ruta del conductor
    private val _currentRoute = MutableStateFlow<Route?>(null)
    val currentRoute: StateFlow<Route?> = _currentRoute.asStateFlow()

    // Estado para los puntos de la ruta (decodificados del polyline)
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    // Estado para controlar si se está cargando datos
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado para el ID del conductor que se está siguiendo (por defecto 1)
    private val _driverId = MutableStateFlow(1)
    val driverId: StateFlow<Int> = _driverId.asStateFlow()

    // Estado para controlar si la ruta está activa
    private val _isRouteActive = MutableStateFlow(true)
    val isRouteActive: StateFlow<Boolean> = _isRouteActive.asStateFlow()

    // Variable para rastrear si las actualizaciones están pausadas
    private var locationUpdatesPaused = false

    init {
        // Al iniciar, cargamos la última ubicación del conductor
        loadDriverLocation()

        // También cargamos la ruta activa
        //loadActiveRoute()
    }

    /**
     * Configura el ID del conductor a seguir
     */
    fun setDriverId(id: Int) {
        if (_driverId.value != id) {
            _driverId.value = id
            // Al cambiar de conductor, recargamos datos
            loadDriverLocation()
            //loadActiveRoute()
        }
    }

    /**
     * Carga la última ubicación conocida del conductor y se suscribe a actualizaciones
     */
    private fun loadDriverLocation() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Obtener ubicación inicial
                val initialLocation = locationRepository.getDriverLocation(_driverId.value)
                _driverLocation.value = LatLng(initialLocation.latitude, initialLocation.longitude)

                // Suscribirse a actualizaciones en tiempo real si no están pausadas
                if (!locationUpdatesPaused) {
                    subscribeToLocationUpdates()
                }
            } catch (e: Exception) {
                println(e.message)

                _errorMessage.value = "Error al cargar ubicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Suscribe a las actualizaciones de ubicación en tiempo real
     */
    private fun subscribeToLocationUpdates() {
        viewModelScope.launch {
            try {
                locationRepository.subscribeToDriverLocationUpdates(_driverId.value).collectLatest { location ->
                    _driverLocation.value = location
                    print("Ubicación actualizada: $location")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error en las actualizaciones: ${e.message}"
            }
        }
    }

    /**
     * Pausa las actualizaciones de ubicación para optimizar recursos
     * cuando la app está en segundo plano
     */
    fun pauseLocationUpdates() {
        locationUpdatesPaused = true
        locationRepository.unsubscribeFromLocationUpdates()
    }

    /**
     * Reanuda las actualizaciones de ubicación cuando la app vuelve
     * a primer plano
     */
    fun resumeLocationUpdates() {
        if (locationUpdatesPaused) {
            locationUpdatesPaused = false
            subscribeToLocationUpdates()
        }
    }

    /**
     * Carga la ruta activa del conductor desde el repositorio
     */
    private fun loadActiveRoute() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Obtener todas las rutas
                savedRoutesRepository.getRoute(1).collectLatest { route ->
                    // Buscar una ruta activa para el conductor
                    val activeRoute = route

                    if (activeRoute != null) {
                        // Si encontramos una ruta activa, marcamos la ruta como activa
                        _isRouteActive.value = true

                        // Solicitamos la ruta al repositorio
                        if (route != null && route.encodedPolyline != null) {
                            val decodedPoints = decodePolyline(route.encodedPolyline)
                            _routePoints.value = decodedPoints
                            _currentRoute.value = null
                        }
                    } else {
                        // Si no hay ruta activa
                        _isRouteActive.value = false
                        _currentRoute.value = null
                        _routePoints.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                println(e.message)

                _errorMessage.value = "Error al cargar ruta: ${e.message}"
                _isRouteActive.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia los recursos cuando se destruye el ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        // Cancelar suscripción de ubicación
        locationRepository.unsubscribeFromLocationUpdates()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MyApplication
                MapViewModel(
                    locationRepository = application.appProvider.provideLocationRepository(),
                    savedRoutesRepository = application.appProvider.provideSavedRoutesRepository()
                )
            }
        }
    }
}
