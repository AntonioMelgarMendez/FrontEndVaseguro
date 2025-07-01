package com.VaSeguro.ui.screens.Parents.Map

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.R
import com.VaSeguro.helpers.bitmapDescriptorFromVector
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
    childId: Int = 48
) {
    // Estados del ViewModel
    val driverLocation by viewModel.driverLocation.collectAsStateWithLifecycle()
    val isRouteActive by viewModel.isRouteActive.collectAsStateWithLifecycle()
    val routePoints by viewModel.routePoints.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // NUEVOS: Estados para paradas filtradas y información de ruta
    val parentChildrenStops by viewModel.parentChildrenStops.collectAsStateWithLifecycle()
    val routeProgress by viewModel.routeProgress.collectAsStateWithLifecycle()
    val routeStatus by viewModel.routeStatus.collectAsStateWithLifecycle()

    // NUEVOS: Estados para notificaciones y alertas
    val proximityAlert by viewModel.proximityAlert.collectAsStateWithLifecycle()
    val stopStateChangeNotification by viewModel.stopStateChangeNotification.collectAsStateWithLifecycle()

    // Estado para la cámara del mapa
    val cameraPositionState = rememberCameraPositionState()
    var hasInitializedCamera by remember { mutableStateOf(false) }

    // SnackbarHostState para mostrar mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Contexto para convertir recursos vectoriales a BitmapDescriptor
    val context = LocalContext.current

    // Observador del ciclo de vida
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para controlar si el mapa está visible
    var isMapVisible by remember { mutableStateOf(true) }

    // Referencia del mapa para poder controlar su ciclo de vida
    var mapRef by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }

    // Estado para controlar si se debe seguir al conductor
    var followDriver by remember { mutableStateOf(true) }

    // Contador de actualizaciones para debug
    var updateCount by remember { mutableStateOf(0) }

    // Configurar el parent ID cuando se inicializa o cambia
    LaunchedEffect(childId) {
        viewModel.setChildId(childId)
    }

    // Gestión del ciclo de vida para optimizar recursos del mapa
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pauseLocationUpdates()
                    isMapVisible = false
                    mapRef?.let { map ->
                        try {
                            val mapClass = map.javaClass
                            mapClass.declaredFields.find { it.name.contains("cacheManager", ignoreCase = true) }?.let { cacheField ->
                                cacheField.isAccessible = true
                                val cacheManager = cacheField.get(map)
                                cacheManager?.javaClass?.getDeclaredMethod("releaseDbLock")?.let { releaseMethod ->
                                    releaseMethod.isAccessible = true
                                    releaseMethod.invoke(cacheManager)
                                }
                            }
                        } catch (e: Exception) {
                            println("Error al liberar bloqueo de BD: ${e.message}")
                        }
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    isMapVisible = true
                    viewModel.resumeLocationUpdates()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    mapRef = null
                }
                else -> { /* No hacer nada */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapRef = null
        }
    }

    // Efecto para mover la cámara cuando se actualiza la ubicación del conductor
    LaunchedEffect(driverLocation) {
        driverLocation?.let { location ->
            updateCount++
            println("Actualización #$updateCount recibida en MapScreen: $location")

            if (!hasInitializedCamera || followDriver) {
                val currentZoom = if (hasInitializedCamera) {
                    cameraPositionState.position.zoom
                } else {
                    15f
                }

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(location, currentZoom),
                    durationMs = 500
                )
                hasInitializedCamera = true
            }
        }
    }

    // Efecto para mostrar mensajes de error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    // NUEVO: Efecto para mostrar alertas de proximidad
    LaunchedEffect(proximityAlert) {
        proximityAlert?.let { alert ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = alert,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // NUEVO: Efecto para mostrar notificaciones de cambio de estado
    LaunchedEffect(stopStateChangeNotification) {
        stopStateChangeNotification?.let { notification ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = notification,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Solo mostrar el mapa si está visible
            if (isMapVisible) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL,
                        minZoomPreference = 5f,
                        maxZoomPreference = 20f,
                        isTrafficEnabled = false,
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true,
                        mapToolbarEnabled = false,
                    ),
                    onMapLoaded = {
                        println("Mapa cargado completamente")
                    }
                ) {
                    MapEffect { map ->
                        mapRef = map
                        map.setMaxZoomPreference(20f)
                        map.setMinZoomPreference(5f)
                        map.isBuildingsEnabled = false
                        map.isIndoorEnabled = false

                        try {
                            val mapClassType = map.javaClass
                            mapClassType.getDeclaredMethod("setPersistentCacheEnabled", Boolean::class.java)?.let {
                                it.isAccessible = true
                                it.invoke(map, false)
                            }
                            mapClassType.getDeclaredMethod("setTrimMemoryPolicy", Int::class.java)?.let {
                                it.isAccessible = true
                                it.invoke(map, 5)
                            }
                            mapClassType.getDeclaredMethod("releaseDatabaseLocks")?.let {
                                it.isAccessible = true
                                it.invoke(map)
                            }
                        } catch (e: Exception) {
                            println("No se pudo configurar política de memoria: ${e.message}")
                        }

                    }

                    // NUEVO: Mostrar el polyline de la ruta si hay una ruta activa
                    if (isRouteActive && routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color.Blue,
                            width = 8f
                        )
                    }

                    // Mostrar el marcador del conductor si está disponible
                    driverLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Ubicación del conductor",
                            icon = context.bitmapDescriptorFromVector(R.drawable.bus_map, heightDp = 40),
                            flat = true,
                        )
                    }

                    // NUEVO: Mostrar marcadores de las paradas de los hijos del padre
                    parentChildrenStops.forEach { stopPassenger ->
                        val stopLocation = LatLng(
                            stopPassenger.stop.latitude,
                            stopPassenger.stop.longitude
                        )

                        Marker(
                            state = MarkerState(position = stopLocation),
                            title = "${stopPassenger.child.fullName} - ${stopPassenger.stop.name}",
                            snippet = "Tipo: ${stopPassenger.stopType.name}",
                            icon = if (stopPassenger.stopType.name == "HOME") {
                                context.bitmapDescriptorFromVector(R.drawable.user, heightDp = 35)
                            } else {
                                context.bitmapDescriptorFromVector(R.drawable.school, heightDp = 35)
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Mapa en pausa para optimizar recursos",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // MODIFICADO: Tarjeta informativa mejorada con información de ruta
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seguimiento en tiempo real",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    // NUEVO: Mostrar alerta de proximidad si existe
                    proximityAlert?.let { alert ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = "Proximidad",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alert,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // NUEVO: Mostrar notificación de cambio de estado si existe
                    stopStateChangeNotification?.let { notification ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = notification,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // NUEVO: Mostrar información detallada de la ruta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isRouteActive) "Ruta activa" else "Sin ruta activa",
                                color = if (isRouteActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            // NUEVO: Mostrar estado de la ruta si está disponible
                            routeStatus?.let { status ->
                                Text(
                                    text = "Estado: $status",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // NUEVO: Mostrar progreso de la ruta si está activa
                        if (isRouteActive && routeProgress > 0) {
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Progreso",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${(routeProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // NUEVO: Mostrar información de paradas de los hijos
                    if (parentChildrenStops.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        val stopsOnRoute = viewModel.getStopsOnCurrentRoute()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paradas de mis hijos: ${parentChildrenStops.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            if (isRouteActive && stopsOnRoute.isNotEmpty()) {
                                Text(
                                    text = "En ruta actual: ${stopsOnRoute.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Mostrar coordenadas del conductor
                    driverLocation?.let { location ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lat: ${String.format("%.6f", location.latitude)} Lng: ${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Actualizaciones: $updateCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Botones de control en la parte inferior
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para activar/desactivar seguimiento automático
                IconButton(
                    onClick = {
                        followDriver = !followDriver
                        if (followDriver && driverLocation != null) {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(driverLocation!!, cameraPositionState.position.zoom),
                                    durationMs = 1000
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (followDriver)
                                   MaterialTheme.colorScheme.primaryContainer
                                   else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "Seguir al conductor",
                        tint = if (followDriver)
                              MaterialTheme.colorScheme.onPrimaryContainer
                              else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botón para centrar en la ubicación del conductor
                IconButton(
                    onClick = {
                        driverLocation?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(location, cameraPositionState.position.zoom),
                                    durationMs = 1000
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centrar en el conductor",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Indicador de carga principal
            if (isLoading && driverLocation == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp
                    )
                }
            }

            // Mensaje de error si no hay datos
            if (!isLoading && driverLocation == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "No se pudo obtener la ubicación del conductor",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = {
                                viewModel.setDriverId(1)
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

