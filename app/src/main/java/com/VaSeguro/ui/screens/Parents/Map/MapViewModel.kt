package com.VaSeguro.ui.screens.Parents.Map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.LocationDriverAddress
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.map.repository.StopPassengerRepository
import com.VaSeguro.map.repository.StopRouteRepository
import com.VaSeguro.ui.screens.Driver.Route.RouteScreenViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val savedRoutesRepository: SavedRoutesRepository,
    private val stopPassengerRepository: StopPassengerRepository,
    private val stopRouteRepository: StopRouteRepository // NUEVO: Repositorio para StopRoute
) : ViewModel() {

    // Estado para la posición actual del conductor
    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()

    // Estado para los puntos de la ruta (decodificados del polyline)
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    // NUEVO: Estado para las paradas de los hijos del padre usando StopRoute como fuente de verdad
    private val _parentChildrenStopRoutes = MutableStateFlow<List<StopRoute>>(emptyList())
    val parentChildrenStopRoutes: StateFlow<List<StopRoute>> = _parentChildrenStopRoutes.asStateFlow()

    // Mantener también las paradas tradicionales para compatibilidad con el mapa
    private val _parentChildrenStops = MutableStateFlow<List<StopPassenger>>(emptyList())
    val parentChildrenStops: StateFlow<List<StopPassenger>> = _parentChildrenStops.asStateFlow()

    // NUEVO: Estado para la información completa de ubicación y ruta del conductor
    private val _driverLocationWithRoute = MutableStateFlow<LocationDriverAddress?>(null)
    val driverLocationWithRoute: StateFlow<LocationDriverAddress?> = _driverLocationWithRoute.asStateFlow()

    // Estado para controlar si se está cargando datos
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado para el ID del conductor que se está siguiendo (por defecto 1)
    private val _driverId = MutableStateFlow(1)
    val driverId: StateFlow<Int> = _driverId.asStateFlow()

    // NUEVO: Estado para el ID del padre actual
    private val _childId = MutableStateFlow(1) // Por defecto child con ID 1
    val child_id: StateFlow<Int> = _childId.asStateFlow()

    // Estado para controlar si la ruta está activa
    private val _isRouteActive = MutableStateFlow(false)
    val isRouteActive: StateFlow<Boolean> = _isRouteActive.asStateFlow()

    // NUEVO: Estado para el progreso de la ruta
    private val _routeProgress = MutableStateFlow(0.0f)
    val routeProgress: StateFlow<Float> = _routeProgress.asStateFlow()

    // NUEVO: Estado para el estado de la ruta
    private val _routeStatus = MutableStateFlow<String?>(null)
    val routeStatus: StateFlow<String?> = _routeStatus.asStateFlow()

    // NUEVO: Estados para notificaciones de proximidad y cambios de estado
    private val _proximityAlert = MutableStateFlow<String?>(null)
    val proximityAlert: StateFlow<String?> = _proximityAlert.asStateFlow()

    private val _stopStateChangeNotification = MutableStateFlow<String?>(null)
    val stopStateChangeNotification: StateFlow<String?> = _stopStateChangeNotification.asStateFlow()

    // NUEVO: Estado para rastrear el último segmento procesado (para detectar cambios)
    private val _lastProcessedSegment = MutableStateFlow(-1)
    val lastProcessedSegment: StateFlow<Int> = _lastProcessedSegment.asStateFlow()

    // NUEVO: Umbral de proximidad en metros
    private val proximityThreshold = 300.0

    // Variable para rastrear si las actualizaciones están pausadas
    private var locationUpdatesPaused = false

    init {
        // Al iniciar, cargamos la última ubicación del conductor
        loadDriverLocationWithRoute()

        // También cargamos las paradas de los hijos del padre
        loadParentChildrenStops()
    }

    /**
     * NUEVO: Configura el ID del padre cuyos hijos queremos seguir
     */
    fun setChildId(id: Int) {
        if (_childId.value != id) {
            _childId.value = id
            // Al cambiar de padre, recargamos las paradas de sus hijos
            loadParentChildrenStops()
        }
    }

    /**
     * Configura el ID del conductor a seguir
     */
    fun setDriverId(id: Int) {
        if (_driverId.value != id) {
            _driverId.value = id
            // Al cambiar de conductor, recargamos datos
            loadDriverLocationWithRoute()
        }
    }

    /**
     * MODIFICADO: Carga la última ubicación conocida del conductor con información de ruta
     */
    private fun loadDriverLocationWithRoute() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Obtener ubicación inicial completa (con información de ruta)
                val initialLocation = locationRepository.getDriverLocationWithRoute(_driverId.value)
                _driverLocationWithRoute.value = initialLocation
                _driverLocation.value = LatLng(initialLocation.latitude, initialLocation.longitude)

                // Actualizar estados de ruta
                _isRouteActive.value = initialLocation.route_active
                _routeProgress.value = initialLocation.route_progress
                _routeStatus.value = initialLocation.route_status

                // Decodificar y actualizar polyline si existe
                initialLocation.encoded_polyline?.let { polyline ->
                    val decodedPoints = decodePolyline(polyline)
                    _routePoints.value = decodedPoints
                }

                // Suscribirse a actualizaciones en tiempo real si no están pausadas
                if (!locationUpdatesPaused) {
                    subscribeToLocationAndRouteUpdates()
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
     * NUEVO: Carga las paradas usando StopRoute como fuente de verdad
     */
    private fun loadParentChildrenStops() {
        viewModelScope.launch {
            try {
                // Cargar StopRoutes primero (fuente de verdad)
                stopRouteRepository.getStopRoutesByChild(_childId.value).collectLatest { stopRoutes ->
                    _parentChildrenStopRoutes.value = stopRoutes

                    // Extraer StopPassengers de los StopRoutes para compatibilidad
                    val stopPassengers = stopRoutes.map { it.stopPassenger }
                    _parentChildrenStops.value = stopPassengers

                    println("StopRoutes cargados para child ${_childId.value}: ${stopRoutes.size} paradas")
                    stopRoutes.forEach { stopRoute ->
                        println("StopRoute ID: ${stopRoute.id}, Order: ${stopRoute.order}, State: ${stopRoute.state}, Child: ${stopRoute.stopPassenger.child.fullName}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar paradas: ${e.message}"
                println("Error al cargar StopRoutes del child: ${e.message}")
            }
        }
    }

    /**
     * NUEVO: Suscribe a las actualizaciones de ubicación y ruta en tiempo real
     */
    private fun subscribeToLocationAndRouteUpdates() {
        viewModelScope.launch {
            try {
                locationRepository.subscribeToDriverLocationAndRouteUpdates(_driverId.value).collectLatest { locationData ->
                    _driverLocationWithRoute.value = locationData
                    _driverLocation.value = LatLng(locationData.latitude, locationData.longitude)

                    // Actualizar estados de ruta
                    _isRouteActive.value = locationData.route_active
                    _routeProgress.value = locationData.route_progress
                    _routeStatus.value = locationData.route_status

                    // NUEVO: Verificar proximidad a paradas de mis hijos
                    checkProximityToMyChildrenStops(LatLng(locationData.latitude, locationData.longitude))

                    // NUEVO: Detectar cambio de segmento y verificar estado de paradas
                    if (locationData.current_segment != _lastProcessedSegment.value) {
                        checkStopStateChanges()
                        _lastProcessedSegment.value = locationData.current_segment
                    }

                    // Decodificar y actualizar polyline si cambió
                    locationData.encoded_polyline?.let { polyline ->
                        if (polyline != _driverLocationWithRoute.value?.encoded_polyline) {
                            val decodedPoints = decodePolyline(polyline)
                            _routePoints.value = decodedPoints
                            println("Polyline actualizada: ${decodedPoints.size} puntos")
                        }
                    } ?: run {
                        // Si no hay polyline, limpiar los puntos de ruta
                        _routePoints.value = emptyList()
                    }

                    print("Ubicación actualizada: ${locationData.latitude}, ${locationData.longitude}")
                    print("Ruta activa: ${locationData.route_active}, Progreso: ${locationData.route_progress}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error en las actualizaciones: ${e.message}"
            }
        }
    }

    /**
     * NUEVO: Verifica si el conductor está cerca de alguna parada de mis hijos
     */
    private fun checkProximityToMyChildrenStops(driverLocation: LatLng) {
        val childrenStops = _parentChildrenStops.value

        if (childrenStops.isEmpty() || !_isRouteActive.value) return

        childrenStops.forEach { stopPassenger ->
            val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
            val distance = calculateDistance(driverLocation, stopLocation)

            if (distance <= proximityThreshold) {
                val stopTypeName = if (stopPassenger.stopType.name == "HOME") "casa" else "escuela"
                val alertMessage = "🚌 El conductor está cerca de la parada de ${stopPassenger.child.fullName} (${stopTypeName}) - ${stopPassenger.stop.name}"

                // Solo mostrar la alerta si es diferente a la anterior
                if (_proximityAlert.value != alertMessage) {
                    _proximityAlert.value = alertMessage

                    // Limpiar la alerta después de 5 segundos
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(5000)
                        if (_proximityAlert.value == alertMessage) {
                            _proximityAlert.value = null
                        }
                    }
                }
            }
        }
    }

    /**
     * CORREGIDO: Verifica cambios de estado en las paradas cuando el conductor avanza de segmento
     * Usa StopRoute como fuente de verdad para los estados
     */
    private fun checkStopStateChanges() {
        viewModelScope.launch {
            try {
                // Guardar los estados originales de StopRoute que ya tenemos
                val originalStopRoutes = _parentChildrenStopRoutes.value
                if (originalStopRoutes.isEmpty()) return@launch

                // Obtener StopRoutes actualizados desde el repositorio
                val updatedStopRoutes = stopRouteRepository.getStopRoutesByChild(_childId.value).first()

                // Comparar estado por estado para detectar cambios
                originalStopRoutes.forEach { originalStopRoute ->
                    // Buscar el mismo StopRoute en la lista actualizada
                    val updatedStopRoute = updatedStopRoutes.find { it.id == originalStopRoute.id }

                    updatedStopRoute?.let { updated ->
                        // Verificar si el estado cambió en StopRoute
                        val originalState = originalStopRoute.state
                        val newState = updated.state

                        // Si detectamos un cambio de estado de false a true (completado)
                        if (originalState != newState && newState == true) {
                            val actionText = if (updated.stopPassenger.stopType.name == "HOME") {
                                "fue dejado en casa"
                            } else {
                                "fue recogido en la escuela"
                            }

                            val notification = "✅ ${updated.stopPassenger.child.fullName} $actionText - ${updated.stopPassenger.stop.name}"
                            _stopStateChangeNotification.value = notification

                            println("STOP_ROUTE_CHANGE: Detectado cambio de estado para ${updated.stopPassenger.child.fullName}")
                            println("STOP_ROUTE_CHANGE: Estado original: $originalState, nuevo estado: $newState")
                            println("STOP_ROUTE_CHANGE: StopRoute ID: ${updated.id}, Order: ${updated.order}")

                            // Limpiar la notificación después de 8 segundos
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(8000)
                                if (_stopStateChangeNotification.value == notification) {
                                    _stopStateChangeNotification.value = null
                                }
                            }
                        }
                    }
                }

                // Actualizar los estados locales con los nuevos datos
                _parentChildrenStopRoutes.value = updatedStopRoutes

                // Actualizar también los StopPassengers para compatibilidad
                val updatedStopPassengers = updatedStopRoutes.map { it.stopPassenger }
                _parentChildrenStops.value = updatedStopPassengers

            } catch (e: Exception) {
                println("Error al verificar cambios de estado de StopRoute: ${e.message}")
                _errorMessage.value = "Error al verificar cambios de estado: ${e.message}"
            }
        }
    }

    /**
     * NUEVO: Obtiene las paradas que están en la ruta actual del conductor usando StopRoute
     */
    fun getStopRoutesOnCurrentRoute(): List<StopRoute> {
        val routePoints = _routePoints.value
        val childrenStopRoutes = _parentChildrenStopRoutes.value

        if (routePoints.isEmpty() || childrenStopRoutes.isEmpty()) {
            return emptyList()
        }

        // Filtrar StopRoutes que están cerca de los puntos de la ruta
        return childrenStopRoutes.filter { stopRoute ->
            val stopLocation = LatLng(stopRoute.stopPassenger.stop.latitude, stopRoute.stopPassenger.stop.longitude)

            // Verificar si algún punto de la ruta está cerca de esta parada (dentro de 200 metros)
            routePoints.any { routePoint ->
                calculateDistance(stopLocation, routePoint) <= 200.0
            }
        }
    }

    /**
     * NUEVO: Limpia las alertas y notificaciones
     */
    fun clearProximityAlert() {
        _proximityAlert.value = null
    }

    fun clearStopStateNotification() {
        _stopStateChangeNotification.value = null
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
            subscribeToLocationAndRouteUpdates()
        }
    }

    /**
     * NUEVO: Obtiene las paradas que están en la ruta actual del conductor
     * Filtra las paradas de los hijos del padre que coinciden con la ruta activa
     */
    fun getStopsOnCurrentRoute(): List<StopPassenger> {
        val routePoints = _routePoints.value
        val childrenStops = _parentChildrenStops.value

        if (routePoints.isEmpty() || childrenStops.isEmpty()) {
            return emptyList()
        }

        // Filtrar paradas que están cerca de los puntos de la ruta
        return childrenStops.filter { stop ->
            val stopLocation = LatLng(stop.stop.latitude, stop.stop.longitude)

            // Verificar si algún punto de la ruta está cerca de esta parada (dentro de 200 metros)
            routePoints.any { routePoint ->
                calculateDistance(stopLocation, routePoint) <= 200.0
            }
        }
    }

    /**
     * NUEVO: Función auxiliar para calcular distancia entre dos puntos
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
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
                    savedRoutesRepository = application.appProvider.provideSavedRoutesRepository(),
                    stopPassengerRepository = application.appProvider.provideStopPassengerRepository(),
                    stopRouteRepository = application.appProvider.provideStopRouteRepository() // NUEVO
                )
            }
        }
    }
}
