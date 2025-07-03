package com.VaSeguro.ui.screens.Parents.Map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.LocationDriverAddress
import com.VaSeguro.map.repository.StopRouteRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val stopRouteRepository: StopRouteRepository,
    private val context: Context

) : ViewModel() {

    // Estado para la posición actual del conductor
    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()

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
    private val _driverId = MutableStateFlow(0)
    val driverId: StateFlow<Int> = _driverId.asStateFlow()

    // NUEVO: Estado para el ID del padre actual
    private val _childId = MutableStateFlow(48) // Por defecto child con ID 1
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

    // NUEVO: Job para manejar la limpieza de notificaciones de forma secuencial
    private var notificationCleanupJob: kotlinx.coroutines.Job? = null

    // Variable para rastrear si las actualizaciones están pausadas
    private var locationUpdatesPaused = false

    // NUEVO: Variable para rastrear el estado anterior de la ruta
    private var previousRouteActive = false

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
                val savedDriverId = DriverPrefs.getDriverId(context)

                // Si hay un ID guardado, usarlo; si no, usar el ID por defecto
                val driverIdToUse = savedDriverId ?: _driverId.value

                println("ID del conductor obtenido: $driverIdToUse (guardado: $savedDriverId, por defecto: ${_driverId.value})")

                if (driverIdToUse != _driverId.value) {
                    _driverId.value = driverIdToUse
                }

                val initialLocation = locationRepository.getDriverLocationWithRoute(driverIdToUse)
                _driverLocationWithRoute.value = initialLocation
                _driverLocation.value = LatLng(initialLocation.latitude, initialLocation.longitude)

                // Actualizar estados de ruta
                _isRouteActive.value = initialLocation.route_active
                _routeProgress.value = initialLocation.route_progress
                _routeStatus.value = initialLocation.route_status // Ya es String, no necesita .toString()

                // Suscribirse a actualizaciones en tiempo real si no están pausadas
                if (!locationUpdatesPaused) {
                    subscribeToLocationAndRouteUpdates()
                }

                println("Ubicación del conductor cargada exitosamente: lat=${initialLocation.latitude}, lng=${initialLocation.longitude}")
                println("Ruta activa: ${initialLocation.route_active}, Estado: ${initialLocation.route_status}")

            } catch (e: Exception) {
                println("Error al cargar ubicación del conductor: ${e.message}")
                _errorMessage.value = "Error al cargar ubicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las paradas usando StopRoute como fuente de verdad
     */
    private fun loadParentChildrenStops() {
        viewModelScope.launch {
            try {
                stopRouteRepository.getStopRoutesByChild(_childId.value)
                    .catch { e ->
                        // Manejar errores del Flow usando catch operator
                        println("Error en el Flow de StopRoutes del child: ${e.message}")
                        _errorMessage.value = "Error al cargar paradas: ${e.message}"
                        // Emitir lista vacía como fallback
                        emit(emptyList())
                    }
                    .collectLatest { stopRoutes ->
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
                // Este catch solo maneja errores fuera del Flow
                println("Error general al configurar StopRoutes del child: ${e.message}")
                _errorMessage.value = "Error al configurar paradas: ${e.message}"
            }
        }
    }

    /**
     * MODIFICADO: Suscribe a las actualizaciones de ubicación y ruta en tiempo real
     */
    private fun subscribeToLocationAndRouteUpdates() {
        viewModelScope.launch {
            try {
                locationRepository.subscribeToDriverLocationAndRouteUpdates(_driverId.value).collectLatest { locationData ->
                    _driverLocationWithRoute.value = locationData
                    _driverLocation.value = LatLng(locationData.latitude, locationData.longitude)

                    // NUEVO: Detectar si la ruta se volvió activa (cambio de false a true)
                    val wasRouteInactive = !previousRouteActive
                    val isNowRouteActive = locationData.route_active

                    println("🔍 ANÁLISIS CAMBIO RUTA:")
                    println("  - Estado anterior: $previousRouteActive")
                    println("  - Estado actual: $isNowRouteActive")
                    println("  - ¿Era inactiva antes?: $wasRouteInactive")
                    println("  - ¿Es activa ahora?: $isNowRouteActive")

                    if (wasRouteInactive && isNowRouteActive) {
                        println("🚨 ¡NUEVA RUTA DETECTADA! La ruta se volvió activa, recargando StopRoutes...")

                        // Limpiar notificaciones anteriores ya que es una nueva ruta
                        _proximityAlert.value = null
                        _stopStateChangeNotification.value = null

                        // Resetear el último segmento procesado
                        _lastProcessedSegment.value = -1

                        // Recargar los StopRoutes porque puede ser una nueva ruta
                        loadParentChildrenStops()
                    }

                    // Actualizar estados de ruta
                    previousRouteActive = _isRouteActive.value // Guardar el estado anterior antes de actualizarlo
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

                    println("📍 Ubicación actualizada: ${locationData.latitude}, ${locationData.longitude}")
                    println("🚌 Ruta activa: ${locationData.route_active}, Progreso: ${locationData.route_progress}")
                }
            } catch (e: Exception) {
                println("❌ Error en las actualizaciones: ${e.message}")
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
                    // Ya no limpiamos automáticamente - el mensaje se mantiene hasta que se actualice
                }
            }
        }
    }

    /**
     * CORREGIDO: Verifica cambios en el estado de las paradas (StopRoute) y actualiza los estados
     */
    private fun checkStopStateChanges() {
        viewModelScope.launch {
            try {
                println("=== INICIANDO checkStopStateChanges ===")

                // Guardar los estados originales de StopRoute que ya tenemos
                val originalStopRoutes = _parentChildrenStopRoutes.value
                if (originalStopRoutes.isEmpty()) {
                    println("❌ No hay StopRoutes originales para comparar")
                    return@launch
                }

                println("📋 Estados originales:")
                originalStopRoutes.forEach { original ->
                    println("   StopRoute ID: ${original.id}, State: ${original.state}, Child: ${original.stopPassenger.child.fullName}")
                }

                // Hacer una consulta fresca desde la API para obtener los datos actualizados
                val updatedStopRoutes = try {
                    println("🔄 Consultando API para obtener estados actualizados...")
                    stopRouteRepository.getStopRoutesByChildDirect(_childId.value)
                } catch (e: Exception) {
                    println("❌ Error al obtener StopRoutes actualizados: ${e.message}")
                    return@launch
                }

                println("📋 Estados actualizados desde API:")
                updatedStopRoutes.forEach { updated ->
                    println("   StopRoute ID: ${updated.id}, State: ${updated.state}, Child: ${updated.stopPassenger.child.fullName}")
                }

                // Actualizar INMEDIATAMENTE los estados del ViewModel con los datos frescos
                _parentChildrenStopRoutes.value = updatedStopRoutes
                _parentChildrenStops.value = updatedStopRoutes.map { it.stopPassenger }

                println("🔍 Comparando estados...")

                // Comparar cada parada actualizada con su estado original
                var changeDetected = false
                updatedStopRoutes.forEach { updatedStopRoute ->
                    val originalStopRoute = originalStopRoutes.find { it.id == updatedStopRoute.id }

                    if (originalStopRoute != null) {
                        println("   Comparando StopRoute ID ${updatedStopRoute.id}:")
                        println("     Estado original: ${originalStopRoute.state}")
                        println("     Estado actualizado: ${updatedStopRoute.state}")
                        println("     ¿Cambió de false a true?: ${!originalStopRoute.state && updatedStopRoute.state}")

                        // Si encontramos la parada original y su estado ha cambiado de false a true
                        if (!originalStopRoute.state && updatedStopRoute.state) {
                            changeDetected = true
                            val actionText = if (updatedStopRoute.stopPassenger.stopType.name == "HOME") {
                                "fue recogido/dejado en casa"
                            } else {
                                "fue dejado/recogido en la escuela"
                            }

                            val notificationMessage = "✅ ${updatedStopRoute.stopPassenger.child.fullName} $actionText - ${updatedStopRoute.stopPassenger.stop.name}"

                            println("🎉 ¡CAMBIO DE ESTADO DETECTADO! -> $notificationMessage")

                            // Gestionar la notificación (se mostrará la última detectada si hay varias)
                            _stopStateChangeNotification.value = notificationMessage
                            notificationCleanupJob?.cancel()
                            notificationCleanupJob = viewModelScope.launch {
                                kotlinx.coroutines.delay(8000)
                                if (_stopStateChangeNotification.value == notificationMessage) {
                                    _stopStateChangeNotification.value = null
                                }
                                notificationCleanupJob = null
                            }
                        }
                    } else {
                        println("   ⚠️ No se encontró StopRoute original con ID ${updatedStopRoute.id}")
                    }
                }

                if (!changeDetected) {
                    println("ℹ️ No se detectaron cambios de estado")
                }

                println("=== FIN checkStopStateChanges ===")

            } catch (e: Exception) {
                println("❌ Error al verificar cambios de estado de StopRoute: ${e.message}")
                _errorMessage.value = "Error al verificar cambios de estado: ${e.message}"
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
                    stopRouteRepository = application.appProvider.provideStopRouteRepository(),
                    context = application.applicationContext
                )
            }
        }
    }
}
