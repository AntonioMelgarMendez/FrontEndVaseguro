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
import com.VaSeguro.data.model.Stop.StopRoute
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
        dismissProximityAlert()
        dismissStopInfoDialog() // Asegurar que el diálogo se cierra

        // Deseleccionar todos los niños cuando se limpia la ruta
        _selectedChildIds.value = emptyList()
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
     * Actualiza la ubicación actual del conductor y verifica proximidad a puntos de la ruta
     */
    fun updateCurrentLocation(location: LatLng, routeStatus: RouteStatus) {
        // Calcular velocidad actual
        updateCurrentSpeed(location)

        _currentLocation.value = location
        updateRouteProgress(location)
        checkProximityToRoutePoints(location)
        updateCurrentSegmentIndex(location)
        updateAdjustedTimeEstimation()

        // Actualizar la ubicación en la base de datos, sin pasar el objeto RouteStatus
        viewModelScope.launch {
            locationRepository.updateLocation(1, location.latitude, location.longitude)
        }

        // Lógica condicional basada en RouteStatus si es necesaria
        if(!(routeStatus.equals(RouteStatus.NO_INIT) || routeStatus.equals(RouteStatus.FINISHED))){
            // Lógica adicional para rutas activas si se necesita
        }
    }

    /**
     * Calcula y actualiza la velocidad actual basada en la distancia recorrida
     */
    private fun updateCurrentSpeed(newLocation: LatLng) {
        val currentTime = System.currentTimeMillis()

        lastLocation?.let { prevLocation ->
            // Solo actualizar si han pasado al menos 2 segundos desde la última actualización
            if (currentTime - lastLocationTime >= 2000) {
                // Distancia recorrida en metros
                val distance = calculateDistance(prevLocation, newLocation)

                // Tiempo transcurrido en horas
                val timeElapsed = (currentTime - lastLocationTime) / (1000.0 * 60 * 60)

                if (timeElapsed > 0) {
                    // Velocidad en km/h
                    val speed = (distance / 1000.0) / timeElapsed

                    // Si la velocidad es menor a 3 km/h consideramos que está casi detenido
                    _currentSpeed.value = if (speed < 3.0) 0.0 else speed

                    // Actualizar el tiempo estimado ajustado según la velocidad real
                    updateAdjustedTimeEstimation()
                }
            }
        }

        // Actualizar los datos para el siguiente cálculo
        lastLocation = newLocation
        lastLocationTime = currentTime
    }

    /**
     * Actualiza la estimación de tiempo al próximo punto basado en la velocidad actual
     */
    private fun updateAdjustedTimeEstimation() {
        val route = _selectedRoute.value ?: return
        val segmentIndex = _currentSegmentIndex.value

        if (segmentIndex >= route.segments.size) return

        val segment = route.segments[segmentIndex]
        val currentLoc = _currentLocation.value ?: return

        // Distancia restante al próximo punto en km
        val distanceToNextPoint = calculateDistance(currentLoc, segment.endPoint) / 1000.0

        // Determinamos la velocidad a usar para la estimación
        val speedToUse = when {
            // Si está detenido o muy lento, usar una velocidad promedio urbana
            _currentSpeed.value < 5.0 -> 30.0

            // Si está en movimiento, usar la velocidad actual
            else -> _currentSpeed.value
        }

        if (speedToUse > 0) {
            // Tiempo estimado en horas
            val timeEstimatedHours = distanceToNextPoint / speedToUse

            // Convertir a segundos para formatear
            val timeEstimatedSeconds = (timeEstimatedHours * 3600).toLong()

            _adjustedTimeToNextPoint.value = formatDuration(timeEstimatedSeconds)
        } else {
            // Si no tenemos velocidad, usar el tiempo original estimado
            _adjustedTimeToNextPoint.value = route.getTimeToNextPoint(segmentIndex)
        }
    }

    /**
     * Actualiza el índice del segmento actual basado en la ubicación
     */
    private fun updateCurrentSegmentIndex(location: LatLng) {
        val route = _selectedRoute.value ?: return

        // Si no hay puntos de ruta o segmentos, no hacemos nada
        if (_routePoints.isEmpty() || route.segments.isEmpty()) {
            _nextPointName.value = ""
            _timeToNextPoint.value = ""
            return
        }

        // Solo actualizar segmentos y mostrar alertas si la ruta está iniciada
        val isRouteStarted = _currentRouteStatus.value == RouteStatus.ON_PROGRESS

        // Siempre actualizar la información del próximo punto (para mostrar en la UI)
        updateNextPointInfo()

        // Si la ruta no está iniciada, no actualizamos segmentos ni mostramos alertas
        if (!isRouteStarted) {
            return
        }

        // Verificar si estamos cerca del siguiente waypoint
        val currentIndex = _currentSegmentIndex.value

        // Si estamos en un segmento válido
        if (currentIndex < route.segments.size) {
            val currentSegment = route.segments[currentIndex]
            val distanceToNextPoint = calculateDistance(location, currentSegment.endPoint)

            // Si llegamos al punto final del segmento actual
            if (distanceToNextPoint <= waypointArrivalThreshold) {
                // Verificar si es el punto final
                if (currentIndex >= route.segments.size - 1) {
                    // Si es el último segmento, mostramos alerta de llegada a destino
                    showProximityAlert("¡Has llegado a tu destino!")
                    _nextPointName.value = ""
                    _timeToNextPoint.value = ""
                    return
                }

                // Obtener el punto final del segmento actual (parada actual)
                val currentStopPoint = _routePoints.getOrNull(currentIndex + 1) ?: return

                // Buscar todos los StopPassengers asociados a esta parada
                val stopPassengersForCurrentStop = findStopPassengersForLocation(currentStopPoint.location)

                // Si no hay niños asociados a esta parada, avanzamos al siguiente punto
                if (stopPassengersForCurrentStop.isEmpty()) {
                    advanceToNextSegment()
                    return
                }

                // Verificar si todos los niños de esta parada han sido marcados como recogidos/dejados
                val allCompleted = stopPassengersForCurrentStop.all { stopPassenger ->
                    _stopCompletionStates.value[stopPassenger.id] == true
                }

                // Solo avanzar al siguiente segmento cuando todos los niños han sido marcados
                if (allCompleted) {
                    advanceToNextSegment()
                } else {
                    // Si no todos están marcados, solo mostrar un mensaje de proximidad
                    // pero no avanzar al siguiente segmento
                    if (!_isStopInfoDialogVisible.value) {
                        showProximityAlert("Llegaste a: ${currentSegment.endPointName}. Marca todos los niños para continuar.")
                    }
                }
            }
        }
    }

    /**
     * Encuentra todos los StopPassenger asociados a una ubicación específica
     */
    private fun findStopPassengersForLocation(location: LatLng): List<StopPassenger> {
        return _stopPassengers.value.filter { stopPassenger ->
            val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
            isApproximatelySameLocation(location, stopLocation)
        }
    }

    /**
     * Avanza al siguiente segmento de la ruta y actualiza la información
     */
    private fun advanceToNextSegment() {
        val route = _selectedRoute.value ?: return
        val currentIndex = _currentSegmentIndex.value

        if (currentIndex < route.segments.size - 1) {
            val currentSegment = route.segments[currentIndex]

            // Incrementar el índice de segmento
            _currentSegmentIndex.value = currentIndex + 1

            // Actualizar información del próximo punto
            updateNextPointInfo()

            // Mostrar alerta de llegada al punto
            showProximityAlert("Llegaste a: ${currentSegment.endPointName}")
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

        // Actualizar tiempo estimado considerando la ubicación actual
        if (currentIndex < route.segments.size && _currentLocation.value != null) {
            val segment = route.segments[currentIndex]
            val currentLoc = _currentLocation.value!!

            // Calcular distancia desde la ubicación actual hasta el final del segmento
            val distanceToEnd = calculateDistance(currentLoc, segment.endPoint)

            // Estimar el tiempo basado en una velocidad promedio urbana (20 km/h)
            val averageSpeedKmh = 20.0
            val timeEstimatedHours = (distanceToEnd / 1000.0) / averageSpeedKmh
            val timeEstimatedSeconds = (timeEstimatedHours * 3600).toLong()

            _timeToNextPoint.value = formatDuration(timeEstimatedSeconds)
        } else {
            // Si no podemos calcular un tiempo ajustado, usamos el tiempo original del segmento
            _timeToNextPoint.value = route.getTimeToNextPoint(currentIndex)
        }
    }

    /**
     * Verifica si estamos cerca de algún punto de la ruta
     */
    private fun checkProximityToRoutePoints(currentLocation: LatLng) {
        // Solo verificar si hay ruta activa y puntos de ruta
        if (_selectedRoute.value == null || _routePoints.isEmpty()) {
            return
        }

        // Solo mostrar alertas de proximidad si la ruta está iniciada (en progreso)
        val isRouteStarted = _currentRouteStatus.value == RouteStatus.ON_PROGRESS

        // Verificar si estamos cerca de la ruta para mostrar notificaciones de desviación
        // (esto debe verificarse independientemente del estado de la ruta)
        val routePath = _selectedRoute.value?.polyline?.encodedPolyline?.let {
            decodePolyline(it)
        } ?: return

        if (!isPointNearPolyline(currentLocation, routePath, 50.0)) {
            // Solo mostrar advertencia de desviación si la ruta está iniciada
            if (isRouteStarted) {
                showError("¡Te has desviado de la ruta!")
            }
            return
        }

        // Si la ruta no está iniciada, no mostramos alertas de proximidad
        if (!isRouteStarted) {
            return
        }

        // Verificar proximidad a cada punto de la ruta (excepto el punto de partida y los que ya pasamos)
        // Comenzamos desde el índice 1 para saltar el punto de partida ("Mi ubicación")
        for (i in maxOf(1, _currentSegmentIndex.value) until _routePoints.size) {
            val point = _routePoints[i]
            val distance = calculateDistance(currentLocation, point.location)

            // Si estamos dentro del umbral de proximidad para mostrar alertas
            if (distance <= proximityThreshold && distance > waypointArrivalThreshold) {
                showProximityAlert("Próximamente: ${point.name.ifEmpty { "Punto de ruta" }}")
                break
            }
        }
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
            val routePoint = _routePoints.find { isApproximatelySameLocation(it.location, markerPosition) }

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
     * Configura el umbral de proximidad para detectar paradas (en metros)
     */
    fun setStopProximityThreshold(meters: Double) {
        _stopProximityThreshold.value = meters
    }

    /**
     * Actualiza el estado de un StopPassenger (recogido/dejado)
     */
    fun updateStopPassengerState(stopPassengerId: Int, isCompleted: Boolean) {
        // Actualizar el mapa de estados de paradas
        val currentStates = _stopCompletionStates.value.toMutableMap()
        currentStates[stopPassengerId] = isCompleted
        _stopCompletionStates.value = currentStates

        // Si estamos trabajando con una ruta que tiene datos sincronizados
        _currentRoutesData.value?.let { routeData ->
            // Encontrar el StopRoute correspondiente a este StopPassenger
            val updatedStopRoutes = routeData.stopRoute.map { stopRoute ->
                if (stopRoute.stopPassenger.id == stopPassengerId) {
                    // Actualizar el estado de esta parada
                    stopRoute.copy(state = isCompleted)
                } else {
                    stopRoute
                }
            }

            // Actualizar el objeto RoutesData
            _currentRoutesData.value = routeData.copy(
                stopRoute = updatedStopRoutes
            )

            // Aquí se podría implementar la sincronización con el backend
            // Por ahora solo actualizamos el estado local
        }
    }

    /**
     * Cierra el diálogo de información de parada
     */
    fun dismissStopInfoDialog() {
        _isStopInfoDialogVisible.value = false
        _currentNearbyStop.value = null
        _currentStopPassengers.value = emptyList()
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
     * Actualiza el progreso en la ruta basado en la ubicación actual
     */
    private fun updateRouteProgress(currentLocation: LatLng) {
        val route = _selectedRoute.value ?: return
        val decodedPoints = decodePolyline(route.polyline.encodedPolyline)

        if (decodedPoints.isEmpty()) return

        // Encuentra el punto más cercano en la ruta
        val (closestPointIndex, _) = findClosestPointOnRoute(currentLocation, decodedPoints)

        // Calcula el progreso basado en la distancia recorrida
        _routeProgress.value = closestPointIndex.toFloat() / decodedPoints.size
    }

    /**
     * Actualiza manualmente el progreso de la ruta (útil para simular el inicio de la ruta)
     */
    fun updateRouteProgress(progress: Float) {
        if (progress in 0.0f..1.0f) {
            _routeProgress.value = progress
        }
    }

    /**
     * Carga una ruta guardada desde su ID
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
     * Encuentra el punto más cercano en la ruta y devuelve su índice y distancia
     */
    private fun findClosestPointOnRoute(
        location: LatLng,
        routePoints: List<LatLng>
    ): Pair<Int, Double> {
        if (routePoints.isEmpty()) return Pair(0, 0.0)

        var closestPointIndex = 0
        var closestDistance = Double.MAX_VALUE

        routePoints.forEachIndexed { index, point ->
            val distance = calculateDistance(location, point)
            if (distance < closestDistance) {
                closestDistance = distance
                closestPointIndex = index
            }
        }

        return Pair(closestPointIndex, closestDistance)
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
                    println("DEBUG_ROUTES: Distancia de ruta: ${originalRoute.distanceMeters} metros")
                    val routeWithSegments = createRouteSegments(originalRoute)

                    _selectedRoute.value = routeWithSegments
                    _currentSegmentIndex.value = 0
                    updateNextPointInfo()

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
     * Optimiza el orden de los puntos para crear una ruta más eficiente
     * Utiliza una combinación del algoritmo del vecino más cercano y optimización 2-opt
     */
    private fun optimizeRouteOrder(startLocation: LatLng, points: List<RoutePoint>): List<RoutePoint> {
        if (points.size < 2) return points

        // Paso 1: Obtener un orden inicial basado en el vecino más cercano
        val initialOrder = nearestNeighborOrder(startLocation, points)

        // Paso 2: Optimizar la ruta usando el algoritmo 2-opt
        return twoOptOptimization(startLocation, initialOrder)
    }

    /**
     * Algoritmo del vecino más cercano - genera un orden inicial
     * Comienza desde una ubicación y siempre selecciona el siguiente punto más cercano
     */
    private fun nearestNeighborOrder(startLocation: LatLng, points: List<RoutePoint>): List<RoutePoint> {
        val result = mutableListOf<RoutePoint>()
        val remaining = points.toMutableList()
        var currentPosition = startLocation

        while (remaining.isNotEmpty()) {
            val (nextPointIndex, _) = findClosestPointIndex(currentPosition, remaining)
            val nextPoint = remaining.removeAt(nextPointIndex)
            result.add(nextPoint)
            currentPosition = nextPoint.location
        }

        return result
    }

    /**
     * Algoritmo de optimización 2-opt
     * Mejora una ruta existente intercambiando pares de aristas cuando mejora la distancia total
     */
    private fun twoOptOptimization(startLocation: LatLng, initialOrder: List<RoutePoint>): List<RoutePoint> {
        // Si tenemos pocos puntos, no es necesario optimizar
        if (initialOrder.size < 3) return initialOrder.toList()

        var bestRoute = initialOrder.toList()
        var improved = true
        var iteration = 0
        val maxIterations = 100 // Limitar para evitar bucles infinitos en rutas complejas

        while (improved && iteration < maxIterations) {
            improved = false
            iteration++

            // Evaluar todas las combinaciones posibles de intercambio de aristas
            outer@ for (i in 0 until bestRoute.size - 1) {
                for (j in i + 1 until bestRoute.size) {
                    if (j - i == 1) continue // Ignorar aristas adyacentes

                    // Crear una nueva ruta con los segmentos invertidos
                    val newRoute = bestRoute.toMutableList()
                    newRoute.subList(i + 1, j + 1).reverse()

                    // Comprobar si la nueva ruta es mejor (más corta)
                    if (calculateTotalDistance(startLocation, newRoute) < calculateTotalDistance(startLocation, bestRoute)) {
                        bestRoute = newRoute.toList()
                        improved = true
                        break@outer // Reiniciar con la nueva mejor ruta
                    }
                }
            }
        }

        return bestRoute
    }

    /**
     * Calcula la distancia total de una ruta (suma de las distancias entre puntos consecutivos)
     */
    private fun calculateTotalDistance(startLocation: LatLng, route: List<RoutePoint>): Double {
        if (route.isEmpty()) return 0.0

        var totalDistance = calculateDistance(startLocation, route.first().location)

        for (i in 0 until route.size - 1) {
            totalDistance += calculateDistance(route[i].location, route[i + 1].location)
        }

        return totalDistance
    }

    /**
     * Encuentra el índice del punto más cercano en la lista de puntos
     */
    private fun findClosestPointIndex(currentLocation: LatLng, points: List<RoutePoint>): Pair<Int, Double> {
        if (points.isEmpty()) return Pair(-1, Double.MAX_VALUE)

        var closestIndex = 0
        var shortestDistance = Double.MAX_VALUE

        points.forEachIndexed { index, point ->
            val distance = calculateDistance(currentLocation, point.location)
            if (distance < shortestDistance) {
                shortestDistance = distance
                closestIndex = index
            }
        }

        return Pair(closestIndex, shortestDistance)
    }

    /**
     * Crea segmentos de ruta basados en los puntos de la ruta
     */
    private fun createRouteSegments(route: Route): Route {
        // Si tenemos menos de 2 puntos, no podemos crear segmentos
        if (_routePoints.size < 2) {
            return route
        }

        val segments = mutableListOf<RouteSegment>()
        val allPoints = _routePoints.toList()

        // Crear un segmento para cada par de puntos consecutivos
        for (i in 0 until allPoints.size - 1) {
            val startPoint = allPoints[i]
            val endPoint = allPoints[i + 1]

            // Calcular un polyline para este segmento
            val segmentPolyline = Polyline(route.polyline.encodedPolyline)

            // Calcular distancia aproximada
            val distance = calculateDistance(startPoint.location, endPoint.location).toInt()

            // Calcular duración estimada usando velocidad promedio más realista
            // Velocidad promedio de 30 km/h para ciudad (más realista con tráfico)
            val averageSpeedKmh = 30.0
            val durationSeconds = (distance / (averageSpeedKmh * 1000.0 / 3600)).toLong()
            val duration = formatDuration(durationSeconds)

            segments.add(
                RouteSegment(
                    startPoint = startPoint.location,
                    endPoint = endPoint.location,
                    startPointName = startPoint.name.ifEmpty { "Punto de inicio" },
                    endPointName = endPoint.name.ifEmpty { "Punto de ruta" },
                    distance = distance,
                    duration = duration,
                    polyline = segmentPolyline
                )
            )
        }

        return route.copy(segments = segments)
    }

    /**
     * Formatea la duración en segundos a un formato legible
     */
    public fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "$hours h $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "${seconds % 60} seg"
        }
    }

    /**
     * Actualiza el estado de la ruta actual
     * @param statusId el ID del nuevo estado de la ruta
     */
    fun updateRouteStatus(statusId: String) {
        viewModelScope.launch {
            try {
                // Si tenemos una ruta cargada con un ID
                _currentRouteId.value?.let { routeId ->
                    // Actualizar el estado interno
                    val newStatus = RouteStatus.fromId(statusId)
                    _currentRouteStatus.value = newStatus

                    // Si tenemos datos de ruta, actualizar también el objeto RoutesData
                    _currentRoutesData.value?.let { routeData ->
                        _currentRoutesData.value = routeData.copy(status_id = newStatus)
                    }

                    // Aquí se implementaría la llamada al repositorio para actualizar el estado
                    // Por ahora, solo mostramos un mensaje de confirmación
                    val statusName = when(statusId) {
                        "1" -> "En proceso"
                        "2" -> "Esperando"
                        "3" -> "Pausado"
                        "4" -> "Con problemas"
                        else -> "Desconocido"
                    }
                    //showError("Estado de ruta actualizado a: $statusName")
                } ?: showError("No hay una ruta activa para actualizar")
            } catch (e: Exception) {
                showError("Error al actualizar estado: ${e.message}")
            }
        }
    }

    /**
     * Actualiza el tipo de ruta (INBOUND/OUTBOUND)
     * @param routeType El nuevo tipo de ruta
     */
    fun updateRouteType(routeType: RouteType) {
        _currentRouteType.value = routeType

        // Si tenemos datos de ruta, actualizar también el objeto RoutesData
        _currentRoutesData.value?.let { routeData ->
            _currentRoutesData.value = routeData.copy(type_id = routeType)
        }
    }

    /**
     * Convierte la ruta seleccionada actual a un objeto RoutesData
     * para uso en persistencia y sincronización con el backend
     */
    private fun syncRouteToRoutesData() {
        val route = _selectedRoute.value ?: return
        val routeId = _currentRouteId.value ?: -System.currentTimeMillis().toInt() // ID temporal si no existe

        // Crear StopRoute a partir de los puntos de ruta
        // Omitimos el primer punto (ubicación actual) si existe
        val stopRouteList = mutableListOf<StopRoute>()

        // Comienza desde el índice 1 para omitir "Mi ubicación" si existe
        val startIndex = if (_routePoints.size > 0 && _routePoints[0].name == "Mi ubicación") 1 else 0

        // Convertir RoutePoints a StopRoute
        for (i in startIndex until _routePoints.size) {
            val point = _routePoints[i]

            // Buscar el StopPassenger correspondiente
            val stopPassenger = findStopPassengerForRoutePoint(point)

            // Si encontramos un StopPassenger válido, crear el StopRoute
            stopPassenger?.let { sp ->
                val stopRoute = StopRoute(
                    id = -i, // ID temporal negativo
                    stopPassenger = sp,
                    order = i,
                    state = true
                )
                stopRouteList.add(stopRoute)
            }
        }

        // Formatear fecha actual
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Crear el objeto RoutesData
        val routesData = RoutesData(
            id = routeId,
            name = route.getRouteName(),
            start_date = currentDate,
            // Nota: Estos son placeholders que deberían reemplazarse con valores reales
            vehicle_id = VehicleMap(
                id = 2,
                plate = "P987654",
                model = "Toyota Hiace 2020",
                driver_id = driver.id,
                year = "2020",
                color = "White",
                capacity = "20",
                updated_at = "2025-06-16T09:00:00",
                carPic = "https://example.com/toyota_hiace_2020.jpg",
                created_at = "2025-06-16T09:00:00",
                brand = "Toyota",
            ), // Placeholder
            status_id = _currentRouteStatus.value,
            type_id = RouteType.INBOUND, // Placeholder
            end_date = "", // Vacío hasta que se complete
            stopRoute = stopRouteList
        )

        // Actualizar el estado
        _currentRoutesData.value = routesData
    }

    /**
     * Busca el StopPassenger correspondiente a un punto de ruta
     * basado en el nombre y la ubicación
     */
    private fun findStopPassengerForRoutePoint(routePoint: RoutePoint): StopPassenger? {
        // Si el punto tiene un nombre que sigue el formato esperado: "NombreNiño - NombreParada (TipoParada)"
        val pointName = routePoint.name
        if (pointName.isEmpty() || !pointName.contains(" - ")) return null

        // Extraer partes del nombre
        val childName = pointName.substringBefore(" - ")
        val remainingText = pointName.substringAfter(" - ")
        val stopName = remainingText.substringBeforeLast(" (")
        val stopTypeName = remainingText.substringAfterLast(" (").removeSuffix(")")

        // Buscar en todos los StopPassengers
        return _stopPassengers.value.find { stopPassenger ->
            val nameMatches = stopPassenger.child.fullName == childName
            val locationMatches = isApproximatelySameLocation(
                LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude),
                routePoint.location
            )

            nameMatches && locationMatches
        }
    }

    /**
     * Compara dos ubicaciones para ver si están aproximadamente en el mismo lugar
     * (distancia menor a 10 metros)
     */
    private fun isApproximatelySameLocation(loc1: LatLng, loc2: LatLng): Boolean {
        return calculateDistance(loc1, loc2) < 10.0 // 10 metros de tolerancia
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
                    locationRepository = application.appProvider.provideLocationRepository(),
                )
            }
        }
    }
}
