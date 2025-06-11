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
import com.VaSeguro.map.calculateDistance
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Polyline
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.data.RouteSegment
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.map.formatWaypointsForApi
import com.VaSeguro.map.isPointNearPolyline
import com.VaSeguro.map.repository.MapsApiRepository
import com.VaSeguro.map.repository.RoutesApiRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de rutas.
 * Maneja la lógica relacionada con la planificación de rutas, búsqueda de lugares y
 * cálculo de rutas óptimas entre puntos.
 */
class RouteScreenViewModel(
    private val mapsApiRepository: MapsApiRepository,
    private val routesApiRepository: RoutesApiRepository
) : ViewModel() {

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

    // Estado para la velocidad actual del conductor (km/h)
    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    // Estado para el tiempo estimado ajustado según la velocidad real
    private val _adjustedTimeToNextPoint = MutableStateFlow("")
    val adjustedTimeToNextPoint: StateFlow<String> = _adjustedTimeToNextPoint.asStateFlow()

    // Última ubicación para calcular velocidad
    private var lastLocation: LatLng? = null
    private var lastLocationTime: Long = 0

    // Distancia en metros para considerar que estamos cerca de un punto
    private val proximityThreshold = 100.0

    // Distancia en metros para considerar que hemos llegado a un punto y pasar al siguiente segmento
    private val waypointArrivalThreshold = 50.0

    // Job para control de alertas de proximidad
    private var proximityAlertJob: Job? = null

    /**
     * Añade un punto a la ruta con información opcional
     */
    fun addRoutePoint(point: LatLng, name: String = "") {
        _routePoints.add(RoutePoint(point, name))
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
        dismissProximityAlert()
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
     * Reordena los puntos de la ruta
     */
    fun reorderRoutePoints(fromIndex: Int, toIndex: Int) {
        if (fromIndex in _routePoints.indices && toIndex in _routePoints.indices) {
            val item = _routePoints.removeAt(fromIndex)
            _routePoints.add(toIndex, item)

            // Recalcular la ruta si ya existe una
            if (_selectedRoute.value != null && _routePoints.size >= 2) {
                calculateRoute()
            }
        }
    }

    /**
     * Actualiza la ubicación actual del conductor y verifica proximidad a puntos de la ruta
     */
    fun updateCurrentLocation(location: LatLng) {
        // Calcular velocidad actual
        updateCurrentSpeed(location)

        _currentLocation.value = location
        updateRouteProgress(location)
        checkProximityToRoutePoints(location)
        updateCurrentSegmentIndex(location)
        updateAdjustedTimeEstimation()
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

        // Verificar si estamos cerca del siguiente waypoint
        val currentIndex = _currentSegmentIndex.value

        // Si estamos en un segmento válido
        if (currentIndex < route.segments.size) {
            val currentSegment = route.segments[currentIndex]
            val distanceToNextPoint = calculateDistance(location, currentSegment.endPoint)

            // Si llegamos al punto final del segmento actual, avanzamos al siguiente
            if (distanceToNextPoint <= waypointArrivalThreshold) {
                if (currentIndex < route.segments.size - 1) {
                    _currentSegmentIndex.value = currentIndex + 1
                    updateNextPointInfo()

                    // Mostrar alerta de llegada al punto
                    showProximityAlert("Llegaste a: ${currentSegment.endPointName}")
                } else {
                    // Si es el último segmento, mostramos alerta de llegada a destino
                    showProximityAlert("¡Has llegado a tu destino!")
                    _nextPointName.value = ""
                    _timeToNextPoint.value = ""
                }
            }
        }

        // Actualizamos la información del próximo punto
        updateNextPointInfo()
    }

    /**
     * Actualiza la información sobre el próximo punto en la ruta
     */
    private fun updateNextPointInfo() {
        val route = _selectedRoute.value ?: return
        val currentIndex = _currentSegmentIndex.value

        _nextPointName.value = route.getNextPointName(currentIndex)
        _timeToNextPoint.value = route.getTimeToNextPoint(currentIndex)
    }

    /**
     * Verifica si estamos cerca de algún punto de la ruta
     */
    private fun checkProximityToRoutePoints(currentLocation: LatLng) {
        // Solo verificar si hay ruta activa y puntos de ruta
        if (_selectedRoute.value == null || _routePoints.isEmpty()) {
            return
        }

        // Verificar proximidad a cada punto de la ruta (excepto el que ya llegamos)
        for (i in _currentSegmentIndex.value until _routePoints.size) {
            val point = _routePoints[i]
            val distance = calculateDistance(currentLocation, point.location)
            if (distance <= proximityThreshold && distance > waypointArrivalThreshold) {
                showProximityAlert("Próximamente: ${point.name.ifEmpty { "Punto de ruta" }}")
                break
            }
        }

        // Verificar si estamos cerca de la ruta para mostrar notificaciones
        val routePath = _selectedRoute.value?.polyline?.encodedPolyline?.let {
            decodePolyline(it)
        } ?: return

        if (!isPointNearPolyline(currentLocation, routePath, 50.0)) {
            showError("¡Te has desviado de la ruta!")
        }
    }

    /**
     * Muestra una alerta temporal de proximidad a un punto
     */
    private fun showProximityAlert(pointName: String) {
        // Cancelar cualquier alerta previa
        dismissProximityAlert()

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
                _routePoints.add(RoutePoint(currentLoc, "Mi ubicación"))

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

                val response = routesApiRepository.getDirections(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints
                )

                if (response.isNotEmpty()) {
                    // Crear segmentos de ruta
                    val originalRoute = response.first()
                    val routeWithSegments = createRouteSegments(originalRoute)

                    _selectedRoute.value = routeWithSegments
                    _currentSegmentIndex.value = 0
                    updateNextPointInfo()
                } else {
                    showError("No se pudo calcular la ruta")
                }
            } catch (e: Exception) {
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
     * Reordena una lista específica de puntos de ruta por proximidad
     */
    private fun reorderRoutePointsByProximity(currentLocation: LatLng, points: MutableList<RoutePoint>) {
        if (points.isEmpty()) return

        val orderedPoints = mutableListOf<RoutePoint>()
        val mutablePoints = points.toMutableList()

        // Punto de referencia inicial
        var currentPosition = currentLocation

        // Ordenar los puntos por cercanía
        while (mutablePoints.isNotEmpty()) {
            // Encontrar el punto más cercano al punto actual
            val (nextPointIndex, _) = findClosestPointIndex(currentPosition, mutablePoints)

            // Agregar el punto más cercano a la lista ordenada
            val nextPoint = mutablePoints.removeAt(nextPointIndex)
            orderedPoints.add(nextPoint)

            // Actualizar la posición actual para la próxima iteración
            currentPosition = nextPoint.location
        }

        // Actualizar la lista original con los puntos ordenados
        points.clear()
        points.addAll(orderedPoints)
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
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "$hours h $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "${seconds % 60} seg"
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
                    routesApiRepository = application.appProvider.provideRoutesApiRepository()
                )
            }
        }
    }
}
