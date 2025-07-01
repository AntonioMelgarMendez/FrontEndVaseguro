package com.VaSeguro.ui.screens.Driver.Route

import android.content.Context
import android.util.Log
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
import com.VaSeguro.data.model.Routes.CreateFullRouteRequest
import com.VaSeguro.data.model.Routes.CreateStopRouteRequest
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.map.repository.StopPassengerRepository
import com.VaSeguro.map.calculateDistance
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.data.RouteSegment
import com.VaSeguro.map.formatDuration
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.MapsApiRepository
import com.VaSeguro.map.repository.RoutesApiRepository
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val locationRepository: LocationRepository,
    private val vehicleRepository: VehicleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
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

    private val _driverId = mutableStateOf<Int?>(null)
    val driverId: Int? get() = _driverId.value

    private val _vehicleId = mutableStateOf<Int?>(1)
    val vehicleId: Int? get() = _vehicleId.value

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

    // Set para rastrear las paradas que ya han sido procesadas (para evitar mostrar el diálogo múltiples veces)
    private val _processedStops = MutableStateFlow<Set<String>>(emptySet())
    val processedStops: StateFlow<Set<String>> = _processedStops.asStateFlow()

    // Estado para el diálogo de confirmación al cerrar StopInfoDialog
    private val _showStopCloseConfirmation = MutableStateFlow(false)
    val showStopCloseConfirmation: StateFlow<Boolean> = _showStopCloseConfirmation.asStateFlow()

    // Estado para la velocidad actual del conductor (km/h)
    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    // Estado para el tiempo estimado ajustado según la velocidad real
    private val _adjustedTimeToNextPoint = MutableStateFlow("")
    val adjustedTimeToNextPoint: StateFlow<String> = _adjustedTimeToNextPoint.asStateFlow()

    // Estado para controlar si la ruta current está guardada en la base de datos
    private val _isCurrentRouteSaved = MutableStateFlow(false)
    val isCurrentRouteSaved: StateFlow<Boolean> = _isCurrentRouteSaved.asStateFlow()

    // Estado para el tipo de ruta actual (INBOUND/OUTBOUND)
    private val _currentRouteType = MutableStateFlow(RouteType.INBOUND)
    val currentRouteType: StateFlow<RouteType> = _currentRouteType.asStateFlow()

    // ID de la ruta cargada actualmente (si es una ruta guardada)
    private val _currentRouteId = MutableStateFlow<Int?>(null)
    val currentRouteId: StateFlow<Int?> = _currentRouteId.asStateFlow()

    // Estado para el objeto RoutesData sincronizado con la ruta actual
    private val _currentRoutesData = MutableStateFlow<RoutesData?>(null)
    val currentRoutesData: StateFlow<RoutesData?> = _currentRoutesData.asStateFlow()

    // Estado para el status actual de la ruta
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
        // Inicializar el ViewModel con el ID del conductor y del vehiculo
        viewModelScope.launch {
            val user = userPreferencesRepository.getUserData()
            val token = userPreferencesRepository.getAuthToken()
            val idToUse = user?.id

            if(idToUse!=null){
                Log.d("DRIVER", "driverID: ${idToUse}")

                vehicleRepository.getVehicleById(idToUse!!, token!!).collectLatest { vehicleResource ->
                    if (vehicleResource is Resource.Success) {
                        setVehicleId(vehicleResource.data.id)
                        setDriverId(vehicleResource.data.driverId)

                        // Cargar los datos de niños DESPUÉS de establecer el driverId
                        loadChildrenData()
                    }
                }
            } else {
                Log.d("DRIVER", "No se pudo obtener el driverID: ${DriverPrefs.getDriverId(context).toString()}")

            }

        }

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
                // Verificar que tenemos un driverId válido antes de cargar
                val currentDriverId = _driverId.value
                if (currentDriverId == null) {
                    showError("No se ha establecido el ID del conductor")
                    return@launch
                }

                // Obtener todas las paradas usando el driverId correcto
                val stops = stopPassengerRepository.getAllStopPassengers(currentDriverId).first()
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
                child.calculatedFullName.lowercase().contains(query) ||
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
     * Alterna la selección de un niño y devuelve el nuevo estado de selección.
     * @return `true` si el niño ahora está seleccionado, `false` si no lo está.
     */
    fun toggleChildSelection(childId: Int): Boolean {
        val currentSelection = _selectedChildIds.value.toMutableList()
        val isSelected: Boolean

        if (currentSelection.contains(childId)) {
            currentSelection.remove(childId)
            isSelected = false
        } else {
            currentSelection.add(childId)
            isSelected = true
        }

        _selectedChildIds.value = currentSelection
        return isSelected
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

        // Si no hay paradas existentes, limpiamos la ruta para empezar de cero.
        // Si ya hay, no limpiamos para mantener el historial de otros niños.
        if (_routePoints.isEmpty()) {
            clearRoute()
        }

        // Convertir StopPassenger a RoutePoint y agregar a la ruta
        childStops.forEach { stop ->
            val latLng = LatLng(stop.stop.latitude, stop.stop.longitude)
            val name = "${stop.child.calculatedFullName} - ${stop.stop.name} (${stop.stopType.name})"
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
            "${stop.child.calculatedFullName} - ${stop.stop.name} (${stop.stopType.name})"
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
     * Limpia las selecciones de niños
     */
    fun clearSelections() {
        _selectedChildIds.value = emptyList()
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

        //Limpiar estados de desviación y copias de seguridad
        _originalCompleteRoute.value = null
        _originalRoutePoints.clear()
        _completedSegments.clear()
        _isDeviatedFromRoute.value = false
        consecutiveDeviations = 0
        lastDeviationTime = 0L
        _processedStops.value = emptySet()

        dismissProximityAlert()
        dismissStopInfoDialog() // Asegurar que el diálogo se cierra

        // NO deseleccionar los niños aquí. La deselección debe ser una acción explícita
        // del usuario, no un efecto secundario de limpiar la ruta.
        // _selectedChildIds.value = emptyList()

        // Actualizar la base de datos para resetear todos los valores de ruta
        viewModelScope.launch {
            _currentLocation.value?.let { location ->
                locationRepository.updateLocationWithRoute(
                    driverId = _driverId.value!!,
                    lat = location.latitude,
                    lon = location.longitude,
                    encodedPolyline = null, // Limpiar polyline
                    routeActive = false, // Ruta no activa
                    routeProgress = 0.0f, // Progreso en 0
                    currentSegment = 0, // Segmento en 0
                    routeStatus = RouteStatus.NO_INIT.id // Estado inicial
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

                    // Crear copia de seguridad de la ruta y puntos originales
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
     * Inicia una nueva ruta usando el endpoint /routes/full
     * Crea el registro de ruta en el backend con todas las paradas
     */
    fun startNewRoute(routeName: String = ""): Boolean {
        if (_routePoints.isEmpty()) {
            showError("No hay puntos de ruta para iniciar")
            return false
        }

        val vehicleId = _vehicleId.value
        val driverId = _driverId.value

        if (vehicleId == null || driverId == null) {
            showError("Información de conductor o vehículo no disponible")
            return false
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Construir el request para crear la ruta completa
                val request = buildCreateFullRouteRequest(routeName, vehicleId)

                // Llamar al servicio para crear la ruta usando SavedRoutesRepository
                val createdRoute = savedRoutesRepository.createFullRoute(request)

                // Actualizar el estado local con la ruta creada
                _currentRouteId.value = createdRoute.id
                _currentRoutesData.value = createdRoute
                _currentRouteStatus.value = createdRoute.status_id
                _currentRouteType.value = createdRoute.type_id
                _isCurrentRouteSaved.value = true

                // Cambiar el estado a "En progreso"
                updateRouteStatus(RouteStatus.ON_PROGRESS.id)

                showError("Ruta iniciada exitosamente: ${createdRoute.name}")
                println("DEBUG_START_ROUTE: Ruta creada con ID: ${createdRoute.id}")

            } catch (e: Exception) {
                println("DEBUG_START_ROUTE: Error al iniciar ruta: ${e.message}")
                showError("Error al iniciar la ruta: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }

        return true
    }

    /**
     * Construye el request para crear una ruta completa
     */
    private fun buildCreateFullRouteRequest(routeName: String, vehicleId: Int): CreateFullRouteRequest {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val startDate = dateFormat.format(Date())

        // Generar nombre automático si no se proporciona
        val finalRouteName = if (routeName.isBlank()) {
            "Ruta ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}"
        } else {
            routeName
        }

        // Construir la lista de StopRoute basada en los puntos de ruta actuales
        val stopRouteRequests = buildStopRouteRequestsFromCurrentPoints()

        return CreateFullRouteRequest(
            name = finalRouteName,
            start_date = startDate,
            vehicle_id = vehicleId,
            status_id = RouteStatus.NO_INIT.id.toInt(),
            type_id = _currentRouteType.value.id.toInt(),
            stopRoute = stopRouteRequests
        )
    }

    /**
     * Construye la lista de CreateStopRouteRequest basada en los puntos de ruta actuales
     */
    private fun buildStopRouteRequestsFromCurrentPoints(): List<CreateStopRouteRequest> {
        val stopRouteRequests = mutableListOf<CreateStopRouteRequest>()

        // Iterar sobre los puntos de ruta (excluyendo "Mi ubicación")
        _routePoints.forEachIndexed { index, routePoint ->
            if (routePoint.name != "Mi ubicación" && routePoint.stopType != null) {
                // Buscar el StopPassenger correspondiente a este punto
                val matchingStopPassenger = _stopPassengers.value.find { stopPassenger ->
                    val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
                    isApproximatelySameLocation(routePoint.location, stopLocation)
                }

                matchingStopPassenger?.let { stopPassenger ->
                    val stopRouteRequest = CreateStopRouteRequest(
                        stopPassengerId = stopPassenger.id,
                        order = index, // Usar el índice como orden
                        state = false // Inicialmente todas las paradas están pendientes
                    )
                    stopRouteRequests.add(stopRouteRequest)
                }
            }
        }

        return stopRouteRequests
    }

    /**
     * Construye el objeto RoutesData para la nueva ruta
     */
    private fun buildRoutesDataForNewRoute(): RoutesData {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = dateFormat.format(Date())

        return RoutesData(
            id = 0, // Nuevo registro, será asignado por el backend
            name = "Ruta ${now}", // Nombre por defecto, se puede cambiar
            start_date = now,
            vehicle_id = VehicleMap(
                id = _vehicleId.value ?: 0,
                plate = "",
                driver_id = _driverId.value ?: 0,
                model = "",
                brand = "",
                year = "",
                color = "",
                capacity = "",
                updated_at = "",
                carPic = "",
                created_at = ""
            ),
            status_id = RouteStatus.NO_INIT,
            type_id = _currentRouteType.value,
            end_date = "",
            encodedPolyline = "",
            stopRoute = emptyList() // Se llenará al crear la ruta
        )
    }


    /**
     * Actualiza el estado de un StopPassenger y su StopRoute correspondiente
     */
    fun updateStopPassengerState(stopPassengerId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                // Actualizar el estado local inmediatamente
                val currentStates = _stopCompletionStates.value.toMutableMap()
                currentStates[stopPassengerId] = isCompleted
                _stopCompletionStates.value = currentStates

                // Actualizar en el servidor
                val success = stopPassengerRepository.updateStopRouteState(stopPassengerId, routeId = currentRouteId.value!!, isCompleted = isCompleted)

                if (success) {
                    println("DEBUG_UPDATE: StopPassenger $stopPassengerId actualizado: $isCompleted")
                } else {
                    println("WARNING: StopRoute actualizado localmente, pero falló la notificación al backend")
                }

            } catch (e: Exception) {
                println("ERROR: Error al actualizar StopPassenger y StopRoute: ${e.message}")
                _errorMessage.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }

    /**
     * Obtiene una ruta guardada por su ID
     */
    fun getSavedRouteById(routeId: Int): RoutesData? {
        return if (routeId > 0) {
            // Buscar en la lista de rutas guardadas
            _currentRoutesData.value?.let { routeData ->
                if (routeData.id == routeId) {
                    routeData
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    /**
     *  Genera una clave única para una parada basada en su ubicación
     */
    private fun generateStopKey(stopData: StopData): String {
        return "${stopData.latitude}_${stopData.longitude}_${stopData.id}"
    }

    /**
     * Muestra el diálogo de confirmación antes de cerrar StopInfoDialog
     */
    fun showStopCloseConfirmation() {
        _showStopCloseConfirmation.value = true
    }

    /**
     * Cancela el diálogo de confirmación de cierre
     */
    fun cancelStopCloseConfirmation() {
        _showStopCloseConfirmation.value = false
    }

    /**
     * Configura el ID del conductor
     */
    fun setDriverId(id: Int) {
        if (_driverId.value != id) {
            _driverId.value = id
        }
    }

    /**
     * Configura el ID del vehiculo
     */
    fun setVehicleId(id: Int) {
        if (_vehicleId.value != id) {
            _vehicleId.value = id
        }
    }

    /**
     * Confirma el cierre del StopInfoDialog y procesa la parada
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
     * Nueva versión de dismissStopInfoDialog que maneja el flujo de confirmación
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

                //Finalizar la ruta cuando se complete el último segmento
                finalizeRoute()
            }
        }
    }

    /**
     * Finaliza la ruta cuando se han completado todas las paradas
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

                // Guardar la ruta completada automáticamente
                val routeToSave = buildRoutesDataForSaving()
                routeToSave?.let { route ->
                    val savedRoute = savedRoutesRepository.saveCompletedRoute(route)
                    if (savedRoute != null) {
                        showError("¡Ruta completada y guardada exitosamente!")
                        println("DEBUG_ROUTE_SAVE: Ruta guardada con ID: ${savedRoute.id}")
                    } else {
                        showError("Ruta completada, pero hubo un problema al guardarla.")
                        println("DEBUG_ROUTE_SAVE: Error al guardar la ruta completada")
                    }
                } ?: run {
                    showError("¡Ruta completada! (No se pudo construir datos para guardar)")
                    println("DEBUG_ROUTE_SAVE: No se pudieron construir los datos de la ruta para guardar")
                }

                // Limpiar estados de paradas procesadas para permitir nuevas rutas
                _processedStops.value = emptySet()

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
     * Construye un objeto RoutesData para guardar basado en el estado actual
     */
    private fun buildRoutesDataForSaving(): RoutesData? {
        return try {
            val currentLoc = _currentLocation.value
            val selectedRoute = _selectedRoute.value
            val vehicleId = _vehicleId.value
            val driverId = _driverId.value

            if (currentLoc == null || selectedRoute == null || vehicleId == null || driverId == null) {
                println("DEBUG_BUILD_ROUTE: Faltan datos esenciales para construir RoutesData")
                return null
            }

            // Usar datos existentes si ya tenemos un RoutesData, o crear uno nuevo
            val existingRouteData = _currentRoutesData.value

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val now = Date()

            // Construir StopRoutes basado en los puntos de ruta actuales y estados de completación
            val stopRoutes = buildStopRoutesFromCurrentState()

            // Obtener vehículo completo
            val vehicleMap = VehicleMap(
                id = vehicleId,
                plate = "", // Se completará en el repositorio
                driver_id = driverId,
                model = "",
                brand = "",
                year = "",
                color = "",
                capacity = "",
                updated_at = "",
                carPic = "",
                created_at = ""
            )

            val routeData = RoutesData(
                id = existingRouteData?.id ?: 0, // 0 para nueva ruta, será asignado por el backend
                name = existingRouteData?.name ?: "", // Se generará automáticamente en el repositorio
                start_date = existingRouteData?.start_date ?: dateFormat.format(now),
                vehicle_id = vehicleMap,
                status_id = RouteStatus.FINISHED,
                type_id = _currentRouteType.value,
                end_date = dateFormat.format(now),
                encodedPolyline = "", // No guardamos el polyline
                stopRoute = stopRoutes
            )

            println("DEBUG_BUILD_ROUTE: RoutesData construido exitosamente")
            println("DEBUG_BUILD_ROUTE: Tipo: ${routeData.type_id.type}")
            println("DEBUG_BUILD_ROUTE: StopRoutes: ${routeData.stopRoute.size}")

            routeData
        } catch (e: Exception) {
            println("DEBUG_BUILD_ROUTE: Error al construir RoutesData: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Construye StopRoutes basado en el estado actual de la ruta
     */
    private fun buildStopRoutesFromCurrentState(): List<StopRoute> {
        val stopRoutes = mutableListOf<StopRoute>()

        try {
            // Iterar sobre los puntos de ruta (excluyendo "Mi ubicación")
            _routePoints.forEachIndexed { index, routePoint ->
                if (routePoint.name != "Mi ubicación" && routePoint.stopType != null) {
                    // Buscar el StopPassenger correspondiente a este punto
                    val matchingStopPassenger = _stopPassengers.value.find { stopPassenger ->
                        val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
                        isApproximatelySameLocation(routePoint.location, stopLocation)
                    }

                    matchingStopPassenger?.let { stopPassenger ->
                        // Verificar si esta parada fue completada
                        val isCompleted = _stopCompletionStates.value[stopPassenger.id] ?: false

                        val stopRoute = StopRoute(
                            id = 0, // Será asignado por el backend
                            stopPassenger = stopPassenger,
                            order = index, // Usar el índice como orden
                            state = isCompleted
                        )

                        stopRoutes.add(stopRoute)
                    }
                }
            }

            println("DEBUG_BUILD_STOP_ROUTES: ${stopRoutes.size} StopRoutes construidos")
        } catch (e: Exception) {
            println("DEBUG_BUILD_STOP_ROUTES: Error al construir StopRoutes: ${e.message}")
            e.printStackTrace()
        }

        return stopRoutes
    }

    /**
     * Muestra un mensaje de error
     */
    public fun showError(message: String) {
        _errorMessage.value = message
    }

    /**
     * Actualiza la información del próximo punto en la ruta
     */
    private fun updateNextPointInfo() {
        val route = _selectedRoute.value ?: return
        val currentIndex = _currentSegmentIndex.value

        if (currentIndex < _routePoints.size - 1) {
            val nextPoint = _routePoints[currentIndex + 1]
            _nextPointName.value = nextPoint.name

            if (currentIndex < route.segments.size) {
                val segment = route.segments[currentIndex]
                _timeToNextPoint.value = formatGoogleDuration(segment.duration)
            }
        } else {
            _nextPointName.value = "Destino final"
            _timeToNextPoint.value = ""
        }
    }

    /**
     * Actualiza el estado de la ruta actual
     * @param statusId ID del nuevo estado de la ruta (basado en RouteStatus enum)
     */
    fun updateRouteStatus(statusId: Int) {
        viewModelScope.launch {
            try {
                // Buscar el RouteStatus correspondiente al ID
                val newStatus = RouteStatus.entries.find { it.id == statusId }
                    ?: RouteStatus.NO_INIT

                // Actualizar el estado local inmediatamente
                _currentRouteStatus.value = newStatus

                // Si tenemos una ruta activa guardada, actualizar en el servidor
                _currentRouteId.value?.let { routeId ->
                    try {
                        // Determinar si necesitamos fecha de finalización
                        val endDate = if (newStatus == RouteStatus.FINISHED) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            dateFormat.format(Date())
                        } else {
                            null
                        }

                        // Actualizar el estado en el servidor usando el repositorio
                        val updatedRoute = savedRoutesRepository.updateRouteStatus(routeId, statusId, endDate)

                        if (updatedRoute != null) {
                            // Actualizar el objeto RoutesData local
                            _currentRoutesData.value = updatedRoute

                            println("DEBUG_STATUS: Estado de ruta actualizado exitosamente: ${newStatus.status}")
                            if (endDate != null) {
                                println("DEBUG_STATUS: Fecha de finalización establecida: $endDate")
                            }
                        } else {
                            // Si falla la actualización en el servidor, mostrar error pero mantener el estado local
                            showError("Error al actualizar el estado de la ruta en el servidor")
                            println("DEBUG_STATUS: Error al actualizar estado en servidor para ruta $routeId")
                        }
                    } catch (e: Exception) {
                        // Si hay excepción, mostrar error pero mantener el estado local
                        showError("Error de conexión al actualizar estado: ${e.message}")
                        println("DEBUG_STATUS: Excepción al actualizar estado: ${e.message}")
                    }
                } ?: run {
                    // Si no hay ruta guardada activa, solo actualizamos el estado local
                    println("DEBUG_STATUS: Estado actualizado localmente (sin ruta guardada): ${newStatus.status}")
                }

                // Lógica adicional según el estado
                when (newStatus) {
                    RouteStatus.ON_PROGRESS -> {
                        // Si se pone en progreso, asegurar que el progreso no sea 0
                        if (_routeProgress.value == 0f) {
                            updateRouteProgress(0.001f)
                        }
                    }
                    RouteStatus.FINISHED -> {
                        // Si se finaliza, poner progreso al 100%
                        updateRouteProgress(1.0f)

                        // Mostrar mensaje de finalización
                        showError("¡Ruta finalizada exitosamente!")

                        // Limpiar estados de paradas procesadas
                        _processedStops.value = emptySet()

                        // Opcional: Limpiar la ruta después de un delay
                        viewModelScope.launch {
                            delay(3000)
                            clearRoute()
                        }
                    }
                    RouteStatus.STOPED -> {
                        // Pausar el seguimiento de proximidad si existe
                        proximityAlertJob?.cancel()
                    }
                    RouteStatus.PROBLEMS -> {
                        // Log para problemas reportados
                        println("DEBUG_STATUS: Ruta marcada con problemas")
                    }
                    else -> {
                        // Estados iniciales o no definidos
                        println("DEBUG_STATUS: Estado actualizado a: ${newStatus.status}")
                    }
                }

            } catch (e: Exception) {
                showError("Error al actualizar estado de la ruta: ${e.message}")
                println("DEBUG_STATUS: Error general en updateRouteStatus: ${e.message}")
            }
        }
    }

    /**
     * Optimiza el orden de los puntos de ruta para minimizar la distancia total
     */
    private fun optimizeRouteOrder(startLocation: LatLng, points: List<RoutePoint>): List<RoutePoint> {
        if (points.size <= 2) return points

        // Algoritmo simple de optimización: ordenar por proximidad
        val optimized = mutableListOf<RoutePoint>()
        val remaining = points.toMutableList()
        var currentLocation = startLocation

        while (remaining.isNotEmpty()) {
            val nearest = remaining.minByOrNull { point ->
                calculateDistance(currentLocation, point.location)
            }

            nearest?.let { point ->
                optimized.add(point)
                remaining.remove(point)
                currentLocation = point.location
            }
        }

        return optimized
    }

    /**
     * Crea segmentos de ruta a partir de una ruta básica
     */
    private fun createRouteSegments(route: Route): Route {
        val segments = mutableListOf<RouteSegment>()

        // Usar las RouteLegs de la ruta para obtener información real de los segmentos
        if (route.legs.isNotEmpty()) {
            // Crear segmentos basados en las RouteLegs
            route.legs.forEachIndexed { index, leg ->
                val startPointIndex = index
                val endPointIndex = index + 1

                // Obtener nombres de los puntos si están disponibles
                val startPointName = if (startPointIndex < _routePoints.size) {
                    _routePoints[startPointIndex].name
                } else {
                    "Punto ${startPointIndex + 1}"
                }

                val endPointName = if (endPointIndex < _routePoints.size) {
                    _routePoints[endPointIndex].name
                } else {
                    "Destino final"
                }

                val segment = RouteSegment(
                    startPoint = leg.startLocation.latLng,
                    endPoint = leg.endLocation.latLng,
                    startPointName = startPointName,
                    endPointName = endPointName,
                    distance = leg.distanceMeters,
                    duration = leg.duration,
                    polyline = leg.polyline
                )

                segments.add(segment)
            }
        } else {
            // Fallback: crear segmentos básicos si no hay RouteLegs
            for (i in 0 until _routePoints.size - 1) {
                val startPoint = _routePoints[i]
                val endPoint = _routePoints[i + 1]

                val segment = RouteSegment(
                    startPoint = startPoint.location,
                    endPoint = endPoint.location,
                    startPointName = startPoint.name,
                    endPointName = endPoint.name,
                    distance = calculateDistance(startPoint.location, endPoint.location).toInt(),
                    duration = "5 min", // Estimación básica como fallback
                    polyline = route.polyline // Usar el polyline de la ruta original
                )

                segments.add(segment)
            }
        }

        return route.copy(segments = segments)
    }

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
     * Crea una copia de seguridad de la ruta original
     */
    private fun createRouteBackup(route: Route, points: List<RoutePoint>) {
        _originalCompleteRoute.value = route
        _originalRoutePoints.clear()
        _originalRoutePoints.addAll(points)
    }

    /**
     * Manejo de desviaciones y recálculo automático
     */
    fun forceRouteRecalculation() {
        // Implementación básica para recálculo forzado
        if (_routePoints.size >= 2) {
            calculateRoute()
        }
    }

    fun pauseRouteOnAppBackground() {
        // Pausar actualizaciones cuando la app va a segundo plano
        if (_currentRouteStatus.value == RouteStatus.ON_PROGRESS) {
            updateRouteStatus(RouteStatus.STOPED.id)
        }
    }

    fun resumeRouteOnAppForeground() {
        // Reanudar cuando la app vuelve al primer plano
        if (_currentRouteStatus.value == RouteStatus.STOPED) {
            updateRouteStatus(RouteStatus.ON_PROGRESS.id)
        }
    }

    fun finalizeRouteOnAppDestroy() {
        // Finalizar la ruta si la app se cierra
        if (_currentRouteStatus.value == RouteStatus.ON_PROGRESS) {
            updateRouteStatus(RouteStatus.FINISHED.id)
        }
    }

    /**
     * Carga una ruta guardada desde la base de datos
     */
    fun loadSavedRoute(routeId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Obtener la ruta desde el repositorio
                savedRoutesRepository.getRoute(routeId).collect { routeData ->
                    routeData?.let { route ->
                        // Limpiar estado actual
                        clearRoute()

                        // Configurar la nueva ruta
                        _currentRouteId.value = route.id
                        _currentRoutesData.value = route
                        _currentRouteType.value = route.type_id
                        _currentRouteStatus.value = route.status_id

                        // Convertir StopRoutes a RoutePoints
                        route.stopRoute.forEach { stopRoute ->
                            val stopLocation = LatLng(
                                stopRoute.stopPassenger.stop.latitude,
                                stopRoute.stopPassenger.stop.longitude
                            )
                            val pointName = "${stopRoute.stopPassenger.child.calculatedFullName} - ${stopRoute.stopPassenger.stop.name}"

                            addRoutePoint(stopLocation, pointName, stopRoute.stopPassenger.stopType)

                            // Restaurar estado de completación
                            val currentStates = _stopCompletionStates.value.toMutableMap()
                            currentStates[stopRoute.stopPassenger.id] = stopRoute.state
                            _stopCompletionStates.value = currentStates
                        }

                        // Calcular la ruta con los puntos cargados
                        if (_routePoints.isNotEmpty()) {
                            calculateRoute()
                        }

                        println("DEBUG_LOAD: Ruta cargada exitosamente: ${route.name}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG_LOAD: Error al cargar ruta: ${e.message}")
                showError("Error al cargar la ruta: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza la ubicación actual del conductor y realiza operaciones relacionadas
     */
    fun updateCurrentLocation(location: LatLng, routeStatus: RouteStatus) {
        val previousLocation = _currentLocation.value
        _currentLocation.value = location

        // Si hay una ubicación previa, calcular la velocidad
        previousLocation?.let { prevLoc ->
            calculateSpeed(prevLoc, location)
        }

        // Si hay una ruta activa y está en progreso, realizar verificaciones
        if (_selectedRoute.value != null && routeStatus == RouteStatus.ON_PROGRESS) {
            // Verificar proximidad a puntos de la ruta
            checkProximityToRoutePoints(location)

            // Actualizar el progreso de la ruta
            updateRouteProgress(location)
        }

        // Actualizar la ubicación en el repositorio si tenemos una ruta activa
        viewModelScope.launch {
            try {
                _driverId.value?.let { driverId ->
                    val selectedRoute = _selectedRoute.value
                    val encodedPolyline = selectedRoute?.polyline?.encodedPolyline
                    val routeActive = selectedRoute != null && routeStatus == RouteStatus.ON_PROGRESS
                    val progress = _routeProgress.value
                    val currentSegment = _currentSegmentIndex.value
                    val statusId = routeStatus.id

                    locationRepository.updateLocationWithRoute(
                        driverId = driverId,
                        lat = location.latitude,
                        lon = location.longitude,
                        encodedPolyline = encodedPolyline,
                        routeActive = routeActive,
                        routeProgress = progress,
                        currentSegment = currentSegment,
                        routeStatus = statusId
                    )
                }
            } catch (e: Exception) {
                println("DEBUG_LOCATION: Error al actualizar ubicación en repositorio: ${e.message}")
            }
        }
    }

    /**
     * Actualiza el progreso de la ruta manualmente
     */
    fun updateRouteProgress(progress: Float) {
        _routeProgress.value = progress.coerceIn(0.0f, 1.0f)
    }

    /**
     * Configura el umbral de proximidad para las paradas
     */
    fun setStopProximityThreshold(threshold: Double) {
        _stopProximityThreshold.value = threshold
    }

    /**
     * Actualiza el tipo de ruta (INBOUND/OUTBOUND)
     */
    fun updateRouteType(routeType: RouteType) {
        _currentRouteType.value = routeType
    }

    /**
     * Verifica si un marcador está cerca y muestra el diálogo de información de parada
     */
    fun checkMarkerProximityAndShowDialog(markerLocation: LatLng) {
        val currentLoc = _currentLocation.value ?: return
        val distance = calculateDistance(currentLoc, markerLocation)

        // Si estamos dentro del umbral de proximidad
        if (distance <= _stopProximityThreshold.value) {
            // Buscar el StopPassenger correspondiente a esta ubicación
            _stopPassengers.value.forEach { stopPassenger ->
                val stopLocation = LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)

                if (isApproximatelySameLocation(markerLocation, stopLocation)) {
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
                    userPreferencesRepository = application.appProvider.provideUserPreferences(),
                    vehicleRepository = application.appProvider.provideVehicleRepository(),
                    context = application.applicationContext
                )
            }
        }
    }
}
