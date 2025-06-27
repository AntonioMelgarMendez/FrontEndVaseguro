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
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.map.repository.StopPassengerRepository
import com.VaSeguro.map.calculateDistance
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Polyline
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.data.RouteSegment
import com.VaSeguro.map.data.driver
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.map.isPointNearPolyline
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.MapsApiRepository
import com.VaSeguro.map.repository.RoutesApiRepository
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la pantalla de rutas.
 * Maneja la lógica relacionada con la planificación de rutas, búsqueda de lugares y
 * cálculo de rutas óptimas entre puntos.
 */
class RouteScreenViewModel(
    private val mapsApiRepository: MapsApiRepository,
    private val routesApiRepository: RoutesApiRepository,
    private val stopPassengerRepository: StopPassengerRepository,
    private val savedRoutesRepository: SavedRoutesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    // Estados para niños y paradas
    private val _children = MutableStateFlow<List<ChildMap>>(emptyList())
    val children: StateFlow<List<ChildMap>> = _children.asStateFlow()

    private val _stopPassengers = MutableStateFlow<List<StopPassenger>>(emptyList())
    val stopPassengers: StateFlow<List<StopPassenger>> = _stopPassengers.asStateFlow()

    // Estado para mantener los niños seleccionados
    private val _selectedChildIds = MutableStateFlow<List<Int>>(emptyList())
    val selectedChildIds: StateFlow<List<Int>> = _selectedChildIds.asStateFlow()

    // Estado para la búsqueda de niños
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Estado para niños filtrados por búsqueda
    val filteredChildren = MutableStateFlow<List<ChildMap>>(emptyList())

    // Estados para puntos de la ruta
    private val _routePoints = mutableStateListOf<RoutePoint>()
    val routePoints: List<RoutePoint> get() = _routePoints

    // Estados para la ruta seleccionada y puntos
    private val _selectedRoute = mutableStateOf<Route?>(null)
    val selectedRoute: Route? get() = _selectedRoute.value

    // Estado para el segmento actual de la ruta
    private val _currentSegmentIndex = MutableStateFlow(0)
    val currentSegmentIndex: StateFlow<Int> = _currentSegmentIndex.asStateFlow()

    // Estado para información sobre el próximo punto
    private val _nextPointName = MutableStateFlow("")
    val nextPointName: StateFlow<String> = _nextPointName.asStateFlow()

    // Estado para tiempo estimado al próximo punto
    private val _timeToNextPoint = MutableStateFlow("")
    val timeToNextPoint: StateFlow<String> = _timeToNextPoint.asStateFlow()

    // Estado de carga
    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    // Estado para mensajes de error
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value

    // Estado para la ubicación actual del conductor
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    // Estado para el progreso de la ruta (0.0 - 1.0)
    private val _routeProgress = MutableStateFlow(0.0f)
    val routeProgress: StateFlow<Float> = _routeProgress.asStateFlow()

    // Estados para alertas de proximidad
    private val _isProximityAlertVisible = MutableStateFlow(false)
    val isProximityAlertVisible: StateFlow<Boolean> = _isProximityAlertVisible.asStateFlow()

    private val _currentPointName = MutableStateFlow("")
    val currentPointName: StateFlow<String> = _currentPointName.asStateFlow()

    // NUEVOS ESTADOS PARA POPUP DE PARADA

    // Distancia en metros configurable para mostrar popup de parada
    private val _stopProximityThreshold = MutableStateFlow(200.0)
    val stopProximityThreshold: StateFlow<Double> = _stopProximityThreshold.asStateFlow()

    // Estado para controlar si el popup de parada está visible
    private val _isStopInfoDialogVisible = MutableStateFlow(false)
    val isStopInfoDialogVisible: StateFlow<Boolean> = _isStopInfoDialogVisible.asStateFlow()

    // Estado para la parada actual cerca de la que estamos
    private val _currentNearbyStop = MutableStateFlow<StopData?>(null)
    val currentNearbyStop: StateFlow<StopData?> = _currentNearbyStop.asStateFlow()

    // Estado para los StopPassengers asociados a la parada actual
    private val _currentStopPassengers = MutableStateFlow<List<StopPassenger>>(emptyList())
    val currentStopPassengers: StateFlow<List<StopPassenger>> = _currentStopPassengers.asStateFlow()

    // Mapa para mantener el estado de cada parada (completada o no)
    private val _stopCompletionStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val stopCompletionStates: StateFlow<Map<Int, Boolean>> = _stopCompletionStates.asStateFlow()

    // NUEVO: Set para rastrear las paradas que ya han sido procesadas (para evitar mostrar el diálogo múltiples veces)
    private val _processedStops = MutableStateFlow<Set<String>>(emptySet())
    val processedStops: StateFlow<Set<String>> = _processedStops.asStateFlow()

    // NUEVO: Estado para el diálogo de confirmación al cerrar StopInfoDialog
    private val _showStopCloseConfirmation = MutableStateFlow(false)
    val showStopCloseConfirmation: StateFlow<Boolean> = _showStopCloseConfirmation.asStateFlow()

    // Estado para la velocidad actual del conductor (km/h)
    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    // Estado para el tiempo estimado ajustado según la velocidad real
    private val _adjustedTimeToNextPoint = MutableStateFlow("")
    val adjustedTimeToNextPoint: StateFlow<String> = _adjustedTimeToNextPoint.asStateFlow()

    // Estado para controlar si la ruta actual está guardada en la base de datos
    private val _isCurrentRouteSaved = MutableStateFlow(false)
    val isCurrentRouteSaved: StateFlow<Boolean> = _isCurrentRouteSaved.asStateFlow()

    // Estado para el tipo de ruta actual (INBOUND/OUTBOUND)
    private val _currentRouteType = MutableStateFlow(RouteType.INBOUND)
    val currentRouteType: StateFlow<RouteType> = _currentRouteType.asStateFlow()

    // ID de la ruta cargada actualmente (si es una ruta guardada)
    private val _currentRouteId = MutableStateFlow<Int?>(null)
    val currentRouteId: StateFlow<Int?> = _currentRouteId.asStateFlow()

    // Nuevo: Estado para el objeto RoutesData sincronizado con la ruta actual
    private val _currentRoutesData = MutableStateFlow<RoutesData?>(null)
    val currentRoutesData: StateFlow<RoutesData?> = _currentRoutesData.asStateFlow()

    // Nuevo: Estado para el status actual de la ruta
    private val _currentRouteStatus = MutableStateFlow<RouteStatus>(RouteStatus.NO_INIT)
    val currentRouteStatus: StateFlow<RouteStatus> = _currentRouteStatus.asStateFlow()

    // Última ubicación para calcular velocidad
    private var lastLocation: LatLng? = null
    private var lastLocationTime: Long = 0

    // Distancia en metros para considerar que estamos cerca de un punto
    private val proximityThreshold = 300.0

    // Distancia en metros para considerar que hemos llegado a un punto y pasar al siguiente segmento
    private val waypointArrivalThreshold = 200.0

    // Job para control de alertas de proximidad
    private var proximityAlertJob: Job? = null

    // NUEVOS ESTADOS PARA MANEJO DE DESVIACIONES Y RECÁLCULO AUTOMÁTICO

    // Copia de seguridad de la ruta original completa para guardar al final
    private val _originalCompleteRoute = MutableStateFlow<Route?>(null)
    val originalCompleteRoute: StateFlow<Route?> = _originalCompleteRoute.asStateFlow()

    // Copia de seguridad de todos los puntos originales
    private val _originalRoutePoints = mutableStateListOf<RoutePoint>()
    val originalRoutePoints: List<RoutePoint> get() = _originalRoutePoints

    // Lista de segmentos ya completados (para no mostrarlos en el mapa)
    private val _completedSegments = mutableStateListOf<RouteSegment>()
    private val _completedSegmentsFlow = MutableStateFlow<List<RouteSegment>>(emptyList())
    val completedSegments: StateFlow<List<RouteSegment>> = _completedSegmentsFlow.asStateFlow()

    // Estado para controlar si estamos en una desviación
    private val _isDeviatedFromRoute = MutableStateFlow(false)
    val isDeviatedFromRoute: StateFlow<Boolean> = _isDeviatedFromRoute.asStateFlow()

    // Contador de desviaciones consecutivas para evitar recálculos excesivos
    private var consecutiveDeviations = 0
    private var lastDeviationTime = 0L

    // Umbral de tiempo para considerar una desviación persistente (en ms)
    private val deviationTimeThreshold = 30000L // 30 segundos

    // Distancia mínima para considerar que un segmento fue completado (en metros)
    private val segmentCompletionThreshold = 150.0

    init {
        // Cargar los datos de niños y sus paradas al iniciar
        loadChildrenData()

        // Iniciar observador para filtrado de niños
        viewModelScope.launch {
            // Combinar estado de niños y búsqueda para filtrar
            _children.collect { childrenList ->
                updateFilteredChildren()
            }
        }

        viewModelScope.launch {
            _searchQuery.collect {
                updateFilteredChildren()
            }
        }
    }

    /**
     * Carga los datos de los niños y sus paradas
     */
    private fun loadChildrenData() {
        viewModelScope.launch {
            try {
                // Obtener todas las paradas
                val stops = stopPassengerRepository.getAllStopPassengers().first()
                _stopPassengers.value = stops

                // Extraer los niños únicos
                val uniqueChildren = stops.map { it.child }.distinctBy { it.id }
                _children.value = uniqueChildren
            } catch (e: Exception) {
                showError("Error al cargar datos de niños: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Actualiza la lista de niños filtrados basado en la búsqueda
     */
    private fun updateFilteredChildren() {
        val query = _searchQuery.value.trim().lowercase()
        val allChildren = _children.value

        filteredChildren.value = if (query.isEmpty()) {
            allChildren
        } else {
            allChildren.filter { child ->
                child.fullName.lowercase().contains(query) ||
                        child.forenames.lowercase().contains(query) ||
                        child.surnames.lowercase().contains(query)
            }
        }
    }

    /**
     * Actualiza el texto de búsqueda
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Alterna la selección de un niño
     */
    fun toggleChildSelection(childId: Int) {
        val currentSelection = _selectedChildIds.value.toMutableList()

        if (currentSelection.contains(childId)) {
            currentSelection.remove(childId)
        } else {
            currentSelection.add(childId)
        }

        _selectedChildIds.value = currentSelection
    }

    /**
     * Obtiene las paradas asociadas a un niño específico
     */
    fun getStopsByChild(childId: Int): List<StopPassenger> {
        return _stopPassengers.value.filter { it.child.id == childId }
    }

    /**
     * Añade los puntos de parada de un niño a la ruta, con opción para calcular o no la ruta automáticamente
     */
    fun addChildStopsToRoute(childId: Int, calculateRouteAutomatically: Boolean = true) {
        val childStops = getStopsByChild(childId)

        if (childStops.isEmpty()) {
            showError("No hay paradas definidas para este niño")
            return
        }

        // Si hay paradas existentes, mantenemos el historial
        // Si no, limpiamos la ruta
        if (_routePoints.isEmpty()) {
            clearRoute()
        }

        // Convertir StopPassenger a RoutePoint y agregar a la ruta
        childStops.forEach { stop ->
            val latLng = LatLng(stop.stop.latitude, stop.stop.longitude)
            val name = "${stop.child.fullName} - ${stop.stop.name} (${stop.stopType.name})"
            addRoutePoint(latLng, name, stop.stopType)
        }

        // Calcular la ruta con los nuevos puntos solo si se solicita y hay suficientes puntos
        if (calculateRouteAutomatically && _routePoints.size >= 2) {
            calculateRoute()
        }
    }

    /**
     * Elimina los puntos de parada asociados a un niño específico
     */
    fun removeChildStops(childId: Int) {
        val childStops = getStopsByChild(childId)

        // Si no hay paradas para este niño, no hacemos nada
        if (childStops.isEmpty()) return

        // Creamos un conjunto con los nombres de las paradas del niño para búsqueda eficiente
        val childStopNames = childStops.map { stop ->
            "${stop.child.fullName} - ${stop.stop.name} (${stop.stopType.name})"
        }.toSet()

        // Eliminamos los puntos que corresponden a este niño
        val pointsToRemove = _routePoints.filter { point ->
            point.name in childStopNames
        }

        pointsToRemove.forEach { point ->
            removeRoutePoint(point.location)
        }
    }

    /**
     * Añade un punto a la ruta con información opcional
     */
    fun addRoutePoint(point: LatLng, name: String = "", stopType: StopType? = null) {
        _routePoints.add(RoutePoint(point, name, stopType))
    }

    /**
     * Limpia todos los puntos de la ruta y la ruta seleccionada
     */
    fun clearRoute() {
        _routePoints.clear()
        _selectedRoute.value = null
        _routeProgress.value = 0.0f
        _currentSegmentIndex.value = 0
        _nextPointName.value = ""
        _timeToNextPoint.value = ""
        _adjustedTimeToNextPoint.value = ""
        _isCurrentRouteSaved.value = false
        _currentRouteId.value = null
        _currentRoutesData.value = null // Limpiar objeto RoutesData sincronizado
        _currentRouteStatus.value = RouteStatus.NO_INIT // Restablecer estado a inicial
        _stopCompletionStates.value = emptyMap() // Limpiar estados de paradas

        // NUEVO: Limpiar estados de desviación y copias de seguridad
        _originalCompleteRoute.value = null
        _originalRoutePoints.clear()
        _completedSegments.clear()
        _isDeviatedFromRoute.value = false
        consecutiveDeviations = 0
        lastDeviationTime = 0L

        dismissProximityAlert()
        dismissStopInfoDialog() // Asegurar que el diálogo se cierra

        // Deseleccionar todos los niños cuando se limpia la ruta
        _selectedChildIds.value = emptyList()

        // CORREGIDO: Actualizar la base de datos para resetear todos los valores de ruta
        viewModelScope.launch {
            _currentLocation.value?.let { location ->
                locationRepository.updateLocation(
                    driverId = 1,
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
        }
    }

    /**
     * Busca lugares por nombre y devuelve los resultados
     */
    suspend fun searchPlaces(query: String): List<PlaceResult> {
        return try {
            mapsApiRepository.searchPlaces(query)
        } catch (e: Exception) {
            showError("Error en la búsqueda: ${e.localizedMessage}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Elimina un punto específico de la ruta
     */
    fun removeRoutePoint(point: LatLng) {
        val indexToRemove = _routePoints.indexOfFirst { it.location == point }
        if (indexToRemove >= 0) {
            _routePoints.removeAt(indexToRemove)

            // Si eliminamos un punto, recalculamos la ruta si ya existía una
            if (_selectedRoute.value != null && _routePoints.size >= 2) {
                calculateRoute()
            } else if (_routePoints.size < 2) {
                _selectedRoute.value = null
                _routeProgress.value = 0.0f
                _currentSegmentIndex.value = 0
                _nextPointName.value = ""
                _timeToNextPoint.value = ""
            }
        }
    }

    /**
     * Calcula la ruta óptima usando los puntos seleccionados y divide la ruta en segmentos
     */
    fun calculateRoute() {
        if (_routePoints.isEmpty()) {
            showError("Añade al menos un destino para calcular una ruta")
            return
        }

        val currentLoc = _currentLocation.value
        if (currentLoc == null) {
            showError("No se puede calcular la ruta sin conocer tu ubicación actual")
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // 1. Primero guardamos una copia separada de los puntos de usuario
                val userRoutePoints = _routePoints.toList()

                // 2. Limpiamos los puntos actuales
                _routePoints.clear()

                // 3. Siempre añadimos la ubicación actual como primer punto
                _routePoints.add(RoutePoint(currentLoc, "Mi ubicación", null))

                // 4. Optimizamos el orden de los puntos de usuario
                val optimizedPoints = if (userRoutePoints.size > 1) {
                    optimizeRouteOrder(currentLoc, userRoutePoints)
                } else {
                    userRoutePoints
                }

                // 5. Añadimos los puntos optimizados a la ruta
                _routePoints.addAll(optimizedPoints)

                // Logging para debug
                val pointNames = _routePoints.map { it.name }.joinToString(", ")
                println("DEBUG: Puntos ordenados: $pointNames")

                // 6. Calcular la ruta usando la API de direcciones
                val origin = _routePoints.first().toApiString()
                val destination = _routePoints.last().toApiString()

                // Waypoints intermedios (excluyendo origen y destino)
                val waypoints = if (_routePoints.size > 2) {
                    _routePoints.subList(1, _routePoints.size - 1)
                        .joinToString("|") { it.toApiString() }
                } else null

                // Log detallado para depuración
                println("DEBUG_ROUTES: Calculando ruta:")
                println("DEBUG_ROUTES: Origen: $origin")
                println("DEBUG_ROUTES: Destino: $destination")
                println("DEBUG_ROUTES: Waypoints: $waypoints")
                println("DEBUG_ROUTES: Total puntos: ${_routePoints.size}")

                val response = routesApiRepository.getDirections(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints
                )

                println("DEBUG_ROUTES: Rutas recibidas: ${response.size}")

                if (response.isNotEmpty()) {
                    // Crear segmentos de ruta
                    val originalRoute = response.first()
                    val routeWithSegments = createRouteSegments(originalRoute)

                    _selectedRoute.value = routeWithSegments
                    _currentSegmentIndex.value = 0
                    updateNextPointInfo()

                    // NUEVO: Crear copia de seguridad de la ruta y puntos originales
                    // Solo si no estamos recalculando (es decir, es la primera vez o es una ruta nueva)
                    if (_originalCompleteRoute.value == null) {
                        createRouteBackup(routeWithSegments, _routePoints.toList())
                    }

                    // Si estamos creando una ruta nueva (sin ID), asignar un ID temporal
                    if (_currentRouteId.value == null) {
                        // Generar un ID negativo temporal (para distinguirlo de los IDs de la base de datos)
                        _currentRouteId.value = -System.currentTimeMillis().toInt()
                    }
                } else {
                    println("DEBUG_ROUTES: La API devolvió una respuesta vacía")
                    showError("No se pudo calcular la ruta")
                }
            } catch (e: Exception) {
                println("DEBUG_ROUTES: Error al calcular ruta: ${e.message}")
                showError("Error al calcular la ruta: ${e.localizedMessage}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza la ubicación actual del conductor y maneja la lógica de proximidad y progreso
     */
    fun updateCurrentLocation(location: LatLng, routeStatus: RouteStatus) {
        val oldLocation = _currentLocation.value
        _currentLocation.value = location

        // Solo procesar lógica de ruta si tenemos una ruta activa
        val route = _selectedRoute.value
        if (route != null && routeStatus == RouteStatus.ON_PROGRESS) {
            // Calcular velocidad si tenemos una ubicación anterior
            if (oldLocation != null) {
                calculateSpeed(oldLocation, location)
            }

            // Verificar proximidad y progreso solo si la ruta está iniciada
            checkProximityToRoutePoints(location)
            updateRouteProgress(location)
        }

        // Actualizar la ubicación en el repositorio
        viewModelScope.launch {
            try {
                locationRepository.updateLocation(
                    driverId = 1,
                    lat = location.latitude,
                    lon = location.longitude
                )
            } catch (e: Exception) {
                println("Error al actualizar ubicación en repositorio: ${e.message}")
            }
        }
    }

    /**
     * Actualiza el estado de la ruta actual
     */
    fun updateRouteStatus(statusId: String) {
        viewModelScope.launch {
            try {
                val newStatus = RouteStatus.values().find { it.id == statusId }
                newStatus?.let { status ->
                    _currentRouteStatus.value = status

                    // Actualizar también en RoutesData si existe
                    _currentRoutesData.value?.let { routeData ->
                        _currentRoutesData.value = routeData.copy(status_id = status)
                    }
                } ?: showError("No hay una ruta activa para actualizar")
            } catch (e: Exception) {
                showError("Error al actualizar estado de ruta: ${e.message}")
            }
        }
    }

    /**
     * Actualiza el tipo de ruta (INBOUND/OUTBOUND)
     */
    fun updateRouteType(routeType: RouteType) {
        _currentRouteType.value = routeType

        // Si tenemos datos de ruta, actualizar también el objeto RoutesData
        _currentRoutesData.value?.let { routeData ->
            _currentRoutesData.value = routeData.copy(type_id = routeType)
        }
    }

    /**
     * Actualiza el progreso de la ruta (0.0 - 1.0)
     */
    fun updateRouteProgress(progress: Float) {
        _routeProgress.value = progress.coerceIn(0.0f, 1.0f)
    }

    /**
     * Configura el umbral de proximidad para detectar paradas (en metros)
     */
    fun setStopProximityThreshold(meters: Double) {
        _stopProximityThreshold.value = meters
    }

    /**
     * Verifica si el usuario está lo suficientemente cerca de un punto y muestra el diálogo si es así
     * @param markerPosition La posición del marcador en el que se ha hecho clic
     * @return true si se muestra el diálogo, false si no está lo suficientemente cerca
     */
    fun checkMarkerProximityAndShowDialog(markerPosition: LatLng): Boolean {
        // Si la ruta no está en progreso, no hacemos nada
        if (_currentRouteStatus.value != RouteStatus.ON_PROGRESS) {
            return false
        }

        // Obtener la ubicación actual
        val currentLocation = _currentLocation.value ?: return false

        // Calcular la distancia al marcador
        val distance = calculateDistance(currentLocation, markerPosition)

        // Verificar si estamos dentro del umbral de proximidad
        if (distance <= _stopProximityThreshold.value) {
            // Encontrar el RoutePoint correspondiente a este marcador
            val routePoint = _routePoints.find { isApproximatelySameLocation(markerPosition, it.location) }

            routePoint?.let {
                // Buscar la StopData asociada a este punto
                _stopPassengers.value.forEach { stopPassenger ->
                    val stopLocation = LatLng(
                        stopPassenger.stop.latitude,
                        stopPassenger.stop.longitude
                    )

                    if (isApproximatelySameLocation(markerPosition, stopLocation)) {
                        // Encontramos una parada cerca
                        val stopData = stopPassenger.stop

                        // NUEVO: Verificar si esta parada ya fue procesada
                        val stopKey = generateStopKey(stopData)
                        if (_processedStops.value.contains(stopKey)) {
                            showError("Esta parada ya ha sido procesada y completada")
                            return false
                        }

                        // Buscar todos los StopPassengers asociados a esta parada
                        val relatedPassengers = _stopPassengers.value.filter {
                            isApproximatelySameLocation(
                                LatLng(it.stop.latitude, it.stop.longitude),
                                stopLocation
                            )
                        }

                        if (relatedPassengers.isNotEmpty()) {
                            // Actualizar estados y mostrar diálogo
                            _currentNearbyStop.value = stopData
                            _currentStopPassengers.value = relatedPassengers
                            _isStopInfoDialogVisible.value = true
                            return true
                        }
                    }
                }
            }

            return false
        } else {
            // Si no estamos lo suficientemente cerca, mostrar mensaje de error
            showError("Necesitas acercarte más a la parada para poder interactuar con ella")
            return false
        }
    }

    /**
     * Actualiza el estado de un StopPassenger
     */
    fun updateStopPassengerState(stopPassengerId: Int, isCompleted: Boolean) {
        // Actualizar el mapa de estados de paradas
        val currentStates = _stopCompletionStates.value.toMutableMap()
        currentStates[stopPassengerId] = isCompleted
        _stopCompletionStates.value = currentStates

        // Encontrar el StopPassenger que se está actualizando
        val stopPassenger = _stopPassengers.value.find { it.id == stopPassengerId }

        // Notificar al backend sobre el cambio de estado
        stopPassenger?.let { sp ->
            notifyStopPassengerStateChange(sp, isCompleted)
        }
    }

    /**
     * Notifica al backend sobre el cambio de estado de un StopPassenger
     */
    private fun notifyStopPassengerStateChange(stopPassenger: StopPassenger, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val stopStateUpdate = mapOf(
                    "event" to "stop_passenger_state_changed",
                    "driver_id" to 1,
                    "stop_passenger_id" to stopPassenger.id,
                    "child_id" to stopPassenger.child.id,
                    "parent_id" to stopPassenger.child.id, // Asumiendo que child_id = parent_id por ahora
                    "stop_id" to stopPassenger.stop.id,
                    "stop_type" to stopPassenger.stopType.name,
                    "is_completed" to isCompleted,
                    "child_name" to stopPassenger.child.fullName,
                    "stop_name" to stopPassenger.stop.name,
                    "current_segment" to _currentSegmentIndex.value,
                    "route_id" to _currentRouteId.value,
                    "timestamp" to System.currentTimeMillis(),
                    "location" to mapOf(
                        "latitude" to _currentLocation.value?.latitude,
                        "longitude" to _currentLocation.value?.longitude
                    )
                )

                // Logging para debug
                println("DEBUG: Notificando cambio de StopPassenger al backend:")
                println("DEBUG: Child: ${stopPassenger.child.fullName}")
                println("DEBUG: Stop: ${stopPassenger.stop.name}")
                println("DEBUG: Completed: $isCompleted")

                // Aquí se implementaría la llamada real al backend/socket
                // stopPassengerRepository.notifyStateChange(stopStateUpdate)
                // locationRepository.notifyStopPassengerUpdate(stopStateUpdate)

            } catch (e: Exception) {
                println("ERROR: Error al notificar cambio de StopPassenger: ${e.message}")
                _errorMessage.value = "Error al notificar cambio de estado: ${e.message}"
            }
        }
    }

    /**
     * NUEVA FUNCIÓN: Genera una clave única para una parada basada en su ubicación
     */
    private fun generateStopKey(stopData: StopData): String {
        return "${stopData.latitude}_${stopData.longitude}_${stopData.id}"
    }

    /**
     * NUEVA FUNCIÓN: Muestra el diálogo de confirmación antes de cerrar StopInfoDialog
     */
    fun showStopCloseConfirmation() {
        _showStopCloseConfirmation.value = true
    }

    /**
     * NUEVA FUNCIÓN: Cancela el diálogo de confirmación de cierre
     */
    fun cancelStopCloseConfirmation() {
        _showStopCloseConfirmation.value = false
    }

    /**
     * NUEVA FUNCIÓN: Confirma el cierre del StopInfoDialog y procesa la parada
     */
    fun confirmStopClose() {
        _showStopCloseConfirmation.value = false

        // Verificar si la parada actual está completada
        val passengersForStop = _currentStopPassengers.value
        val nearbyStop = _currentNearbyStop.value

        if (passengersForStop.isNotEmpty() && nearbyStop != null) {
            val allPassengersHandled = passengersForStop.all { passenger ->
                _stopCompletionStates.value[passenger.id] == true
            }

            if (allPassengersHandled) {
                // Marcar esta parada como procesada para evitar que se vuelva a abrir
                val stopKey = generateStopKey(nearbyStop)
                val currentProcessedStops = _processedStops.value.toMutableSet()
                currentProcessedStops.add(stopKey)
                _processedStops.value = currentProcessedStops

                // Avanzar al siguiente segmento de la ruta solo si todos fueron atendidos
                moveToNextSegment()

                showError("Parada completada. Avanzando al siguiente punto.")
            } else {
                showError("Parada cerrada sin completar todos los pasajeros.")
            }
        }

        // Cerrar el diálogo
        _isStopInfoDialogVisible.value = false
        _currentNearbyStop.value = null
        _currentStopPassengers.value = emptyList()
    }

    /**
     * MODIFICADA: Nueva versión de dismissStopInfoDialog que maneja el flujo de confirmación
     */
    fun dismissStopInfoDialogWithConfirmation() {
        // Verificar si hay pasajeros pendientes
        val passengersForStop = _currentStopPassengers.value

        if (passengersForStop.isNotEmpty()) {
            val allPassengersHandled = passengersForStop.all { passenger ->
                _stopCompletionStates.value[passenger.id] == true
            }

            // Si no todos los pasajeros han sido atendidos, mostrar confirmación
            if (!allPassengersHandled) {
                showStopCloseConfirmation()
            } else {
                // Si todos fueron atendidos, proceder directamente
                confirmStopClose()
            }
        } else {
            // Si no hay pasajeros, cerrar directamente
            dismissStopInfoDialog()
        }
    }

    /**
     * Cierra el diálogo de información de parada
     */
    fun dismissStopInfoDialog() {
        // Verificar si la parada actual está completada
        val passengersForStop = _currentStopPassengers.value
        if (passengersForStop.isNotEmpty()) {
            val allPassengersHandled = passengersForStop.all { passenger ->
                _stopCompletionStates.value[passenger.id] == true
            }

            if (allPassengersHandled) {
                // Si todos los pasajeros de la parada fueron atendidos,
                // avanzamos al siguiente segmento de la ruta.
                moveToNextSegment()
            }
        }

        _isStopInfoDialogVisible.value = false
        _currentNearbyStop.value = null
        _currentStopPassengers.value = emptyList()
    }

    /**
     * Compara dos ubicaciones para ver si están aproximadamente en el mismo lugar
     * (distancia menor a 10 metros)
     */
    private fun isApproximatelySameLocation(loc1: LatLng, loc2: LatLng): Boolean {
        return calculateDistance(loc1, loc2) < 10.0 // 10 metros de tolerancia
    }

    /**
     * Calcula la velocidad actual basada en la diferencia de ubicación y tiempo
     */
    private fun calculateSpeed(oldLocation: LatLng, newLocation: LatLng) {
        val currentTime = System.currentTimeMillis()

        if (lastLocationTime > 0) {
            val timeDiffSeconds = (currentTime - lastLocationTime) / 1000.0
            val distance = calculateDistance(oldLocation, newLocation)

            if (timeDiffSeconds > 0 && distance > 0) {
                val speedMps = distance / timeDiffSeconds
                val speedKmh = speedMps * 3.6
                _currentSpeed.value = speedKmh
            }
        }

        lastLocation = newLocation
        lastLocationTime = currentTime
    }

    /**
     * Verifica proximidad a puntos de la ruta y maneja alertas
     */
    private fun checkProximityToRoutePoints(currentLocation: LatLng) {
        _routePoints.forEach { point ->
            val distance = calculateDistance(currentLocation, point.location)

            if (distance <= proximityThreshold) {
                // Verificar si estamos cerca de una parada específica
                _stopPassengers.value.forEach { stopPassenger ->
                    val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)

                    if (isApproximatelySameLocation(point.location, stopLocation)) {
                        val stopKey = generateStopKey(stopPassenger.stop)

                        // Solo mostrar diálogo si no se ha procesado esta parada
                        if (!_processedStops.value.contains(stopKey) && !_isStopInfoDialogVisible.value) {
                            _currentNearbyStop.value = stopPassenger.stop

                            // Buscar todos los StopPassengers asociados a esta parada
                            val relatedPassengers = _stopPassengers.value.filter {
                                isApproximatelySameLocation(
                                    LatLng(it.stop.latitude, it.stop.longitude),
                                    stopLocation
                                )
                            }

                            _currentStopPassengers.value = relatedPassengers
                            _isStopInfoDialogVisible.value = true
                            return
                        }
                    }
                }

                // Mostrar alerta general de proximidad
                if (!_isProximityAlertVisible.value) {
                    showProximityAlert("Cerca de: ${point.name}")
                }
            }
        }
    }

    /**
     * Actualiza el progreso de la ruta basado en la ubicación actual
     */
    private fun updateRouteProgress(currentLocation: LatLng) {
        val route = _selectedRoute.value ?: return
        val currentSegmentIndex = _currentSegmentIndex.value

        if (currentSegmentIndex < route.segments.size) {
            // Calcular progreso basado en el segmento actual y la posición
            val totalSegments = route.segments.size
            val segmentProgress = currentSegmentIndex.toFloat() / totalSegments.toFloat()
            val withinSegmentProgress = calculateWithinSegmentProgress(currentLocation, currentSegmentIndex)

            val totalProgress = segmentProgress + (withinSegmentProgress / totalSegments)
            _routeProgress.value = totalProgress.coerceIn(0.0f, 1.0f)
        }
    }

    /**
     * Calcula el progreso dentro del segmento actual
     */
    private fun calculateWithinSegmentProgress(currentLocation: LatLng, segmentIndex: Int): Float {
        val route = _selectedRoute.value ?: return 0.0f

        if (segmentIndex >= route.segments.size) return 1.0f

        val segment = route.segments[segmentIndex]
        val segmentStartPoint = if (segmentIndex == 0) {
            _routePoints.firstOrNull()?.location
        } else {
            _routePoints.getOrNull(segmentIndex)?.location
        } ?: return 0.0f

        val segmentEndPoint = _routePoints.getOrNull(segmentIndex + 1)?.location ?: return 0.0f

        val totalSegmentDistance = calculateDistance(segmentStartPoint, segmentEndPoint)
        val distanceFromStart = calculateDistance(segmentStartPoint, currentLocation)

        return if (totalSegmentDistance > 0) {
            (distanceFromStart / totalSegmentDistance).toFloat().coerceIn(0.0f, 1.0f)
        } else {
            0.0f
        }
    }

    /**
     * Muestra una alerta temporal de proximidad a un punto
     */
    private fun showProximityAlert(pointName: String) {
        // Cancelar cualquier alerta previa
        dismissProximityAlert()

        // Verificar si el mensaje comienza con "Llegaste a:" y ya tenemos otro mensaje similar
        if (pointName.startsWith("Llegaste a:") && _currentPointName.value.startsWith("Llegaste a:")) {
            // No mostrar otro mensaje de llegada si ya hay uno activo
            return
        }

        _currentPointName.value = pointName
        _isProximityAlertVisible.value = true

        proximityAlertJob = viewModelScope.launch {
            delay(3000) // Ocultar después de 3 segundos
            dismissProximityAlert()
        }
    }

    /**
     * Oculta la alerta de proximidad
     */
    private fun dismissProximityAlert() {
        proximityAlertJob?.cancel()
        proximityAlertJob = null
        _isProximityAlertVisible.value = false
        _currentPointName.value = ""
    }

    /**
     * Avanza al siguiente segmento de la ruta.
     */
    private fun moveToNextSegment() {
        val newIndex = _currentSegmentIndex.value + 1
        _selectedRoute.value?.let { route ->
            if (newIndex < route.segments.size) {
                _currentSegmentIndex.value = newIndex
                updateNextPointInfo()
            } else {
                // Se ha completado el último segmento
                _currentSegmentIndex.value = newIndex
                _nextPointName.value = ""
                _timeToNextPoint.value = ""
                _adjustedTimeToNextPoint.value = ""
                _routeProgress.value = 1.0f // Marcar la ruta como completada al 100%

                // NUEVO: Finalizar la ruta cuando se complete el último segmento
                finalizeRoute()
            }
        }
    }

    /**
     * NUEVA FUNCIÓN: Finaliza la ruta cuando se han completado todas las paradas
     */
    private fun finalizeRoute() {
        viewModelScope.launch {
            try {
                // Cambiar el estado de la ruta a FINISHED
                _currentRouteStatus.value = RouteStatus.FINISHED

                // Actualizar fecha de finalización si tenemos datos de ruta
                _currentRoutesData.value?.let { routeData ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val endDate = dateFormat.format(Date())

                    _currentRoutesData.value = routeData.copy(
                        status_id = RouteStatus.FINISHED,
                        end_date = endDate
                    )
                }

                // Limpiar estados de paradas procesadas para permitir nuevas rutas
                _processedStops.value = emptySet()

                // Mostrar mensaje de finalización
                showError("¡Felicidades! Has completado todas las paradas de la ruta.")

                // Log para debug
                println("DEBUG_ROUTE: Ruta finalizada correctamente")

                // Limpiar la ruta después de un breve delay
                delay(3000)
                clearRoute()

            } catch (e: Exception) {
                println("DEBUG_ROUTE: Error al finalizar ruta: ${e.message}")
                showError("Error al finalizar la ruta: ${e.message}")
            }
        }
    }

    /**
     * Optimiza el orden de los puntos de ruta usando un algoritmo simple de vecino más cercano
     */
    private fun optimizeRouteOrder(startLocation: LatLng, points: List<RoutePoint>): List<RoutePoint> {
        if (points.size <= 1) return points

        val optimized = mutableListOf<RoutePoint>()
        val remaining = points.toMutableList()
        var currentLocation = startLocation

        while (remaining.isNotEmpty()) {
            // Encontrar el punto más cercano a la ubicación actual
            val nearestPoint = remaining.minByOrNull { point ->
                calculateDistance(currentLocation, point.location)
            }

            nearestPoint?.let { point ->
                optimized.add(point)
                remaining.remove(point)
                currentLocation = point.location
            }
        }

        return optimized
    }

    /**
     * Crea segmentos de ruta a partir de una ruta de la API
     */
    private fun createRouteSegments(route: Route): Route {
        val segments = mutableListOf<RouteSegment>()

        // Si la ruta tiene legs (segmentos), crear un RouteSegment por cada leg
        route.legs.forEachIndexed { index, leg ->
            val startPointName = if (index == 0) "Origen" else _routePoints.getOrNull(index)?.name ?: "Punto $index"
            val endPointName = _routePoints.getOrNull(index + 1)?.name ?: "Punto ${index + 1}"

            segments.add(
                RouteSegment(
                    startPointName = startPointName,
                    endPointName = endPointName,
                    distance = leg.distanceMeters,
                    duration = leg.duration,
                    polyline = leg.polyline,
                    startPoint = leg.startLocation.latLng,
                    endPoint = leg.endLocation.latLng
                )
            )

        }

        return route.copy(segments = segments)
    }

    /**
     * Crea una copia de seguridad de la ruta y puntos originales
     */
    private fun createRouteBackup(route: Route, routePoints: List<RoutePoint>) {
        _originalCompleteRoute.value = route.copy()
        _originalRoutePoints.clear()
        _originalRoutePoints.addAll(routePoints)
    }

    /**
     * Fuerza el recálculo de la ruta desde la ubicación actual
     */
    fun forceRouteRecalculation() {
        val currentLoc = _currentLocation.value ?: return
        val remainingPoints = _routePoints.drop(1) // Omitir "Mi ubicación"

        if (remainingPoints.isNotEmpty()) {
            _routePoints.clear()
            _routePoints.add(RoutePoint(currentLoc, "Mi ubicación", null))
            _routePoints.addAll(remainingPoints)

            calculateRoute()
            _isDeviatedFromRoute.value = false
        }
    }

    /**
     * Actualiza la información sobre el próximo punto en la ruta
     */
    private fun updateNextPointInfo() {
        val route = _selectedRoute.value ?: return
        val currentIndex = _currentSegmentIndex.value

        // Actualizar el nombre del próximo punto
        _nextPointName.value = route.getNextPointName(currentIndex)

        // Usar directamente la duración de Google para el segmento actual
        if (currentIndex < route.segments.size) {
            val segment = route.segments[currentIndex]
            val originalDuration = segment.duration

            // Siempre usamos la duración original de Google para mostrar en la interfaz
            _timeToNextPoint.value = formatGoogleDuration(originalDuration)
        } else {
            // Si no hay un segmento actual, usar la duración total de la ruta
            _timeToNextPoint.value = formatGoogleDuration(route.duration)
        }
    }

    /**
     * Formatea una duración de Google Maps a formato legible
     */
    private fun formatGoogleDuration(durationString: String): String {
        val seconds = parseDuration(durationString)
        return formatDuration(seconds)
    }

    /**
     * Parsea una duración de Google Maps (formato: "123s")
     */
    private fun parseDuration(duration: String): Long {
        return try {
            duration.removeSuffix("s").toLong()
        } catch (e: NumberFormatException) {
            0L
        }
    }

    /**
     * Formatea segundos a formato "Xh Ym" o "Ym"
     */
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    /**
     * Cargar ruta guardada
     */
    fun loadSavedRoute(routeId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Guardamos la ubicación actual antes de limpiar
                val savedCurrentLocation = _currentLocation.value

                // Limpiamos la ruta pero conservamos datos críticos
                _routePoints.clear()
                _selectedRoute.value = null
                _routeProgress.value = 0.0f
                _currentSegmentIndex.value = 0
                _nextPointName.value = ""
                _timeToNextPoint.value = ""
                _isCurrentRouteSaved.value = false
                _currentRouteId.value = null
                _stopCompletionStates.value = emptyMap()
                dismissProximityAlert()

                // Restauramos la ubicación actual inmediatamente
                if (savedCurrentLocation != null) {
                    _currentLocation.value = savedCurrentLocation
                }

                // Obtener la ruta guardada
                savedRoutesRepository.getRoute(routeId).first()?.let { routeData ->
                    // Actualizar estados para indicar que esta es una ruta guardada
                    _currentRouteId.value = routeData.id
                    _isCurrentRouteSaved.value = true

                    // Convertir StopRoute a RoutePoint para usar las funciones de optimización
                    val routePoints = routeData.stopRoute.map { stopRoute ->
                        val stop = stopRoute.stopPassenger.stop
                        val latLng = LatLng(stop.latitude, stop.longitude)
                        val childName = stopRoute.stopPassenger.child.fullName
                        val stopName = stop.name
                        val stopType = stopRoute.stopPassenger.stopType
                        RoutePoint(latLng, "$childName - $stopName (${stopType.name})", stopType)
                    }

                    // Si tenemos ubicación actual, optimizar el orden de las paradas
                    _currentLocation.value?.let { currentLoc ->
                        // Usar el algoritmo de optimización existente
                        val optimizedPoints = optimizeRouteOrder(currentLoc, routePoints)

                        // Añadir los puntos optimizados a la ruta
                        optimizedPoints.forEach { point ->
                            addRoutePoint(point.location, point.name, point.stopType)

                            // Extraer el ID del niño del nombre del punto y añadirlo a seleccionados
                            val childName = point.name.substringBefore(" - ")
                            val childId = routeData.stopRoute.find {
                                it.stopPassenger.child.fullName == childName
                            }?.stopPassenger?.child?.id

                            childId?.let {
                                if (!_selectedChildIds.value.contains(it)) {
                                    _selectedChildIds.value = _selectedChildIds.value + it
                                }
                            }
                        }
                    } ?: run {
                        // Si no hay ubicación actual, mantener el orden original
                        routePoints.forEach { point ->
                            addRoutePoint(point.location, point.name, point.stopType)
                        }
                    }

                    // Si tenemos ubicación actual y al menos un punto, calcular la ruta
                    if (_currentLocation.value != null && _routePoints.isNotEmpty()) {
                        calculateRoute()
                    } else {
                        showError("No se puede calcular la ruta sin ubicación actual")
                    }
                } ?: showError("No se encontró la ruta con ID: $routeId")
            } catch (e: Exception) {
                showError("Error al cargar la ruta: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    fun showError(message: String) {
        _errorMessage.value = message
        viewModelScope.launch {
            delay(3000)
            _errorMessage.value = null
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MyApplication
                RouteScreenViewModel(
                    mapsApiRepository = application.appProvider.provideMapsApiRepository(),
                    routesApiRepository = application.appProvider.provideRoutesApiRepository(),
                    stopPassengerRepository = application.appProvider.provideStopPassengerRepository(),
                    savedRoutesRepository = application.appProvider.provideSavedRoutesRepository(),
                    locationRepository = application.appProvider.provideLocationRepository()
                )
            }
        }
    }
}
