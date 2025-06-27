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
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
) {
    // Estados del ViewModel
    val driverLocation by viewModel.driverLocation.collectAsStateWithLifecycle()
    val isRouteActive by viewModel.isRouteActive.collectAsStateWithLifecycle()
    val routePoints by viewModel.routePoints.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

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

    // Gestión del ciclo de vida para optimizar recursos del mapa
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pauseLocationUpdates()
                    // Cuando la app va a segundo plano, liberar recursos del mapa
                    isMapVisible = false
                    // Forzar liberación del bloqueo de SQLite
                    mapRef?.let { map ->
                        try {
                            // Acceder por reflection a métodos internos para liberar bloqueos de BD
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
                    // Asegurar liberación completa de recursos
                    mapRef = null
                }
                else -> { /* No hacer nada */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Liberar recursos adicionales
            mapRef = null
        }
    }

    // Efecto para mover la cámara cuando se actualiza la ubicación del conductor
    LaunchedEffect(driverLocation) {
        driverLocation?.let { location ->
            // Incrementar contador de actualizaciones para verificar que estamos recibiendo cambios
            updateCount++
            println("Actualización #$updateCount recibida en MapScreen: $location")

            // Mover la cámara si es primera vez o se ha activado el seguimiento
            if (!hasInitializedCamera || followDriver) {
                // Preservar el zoom actual en actualizaciones posteriores
                val currentZoom = if (hasInitializedCamera) {
                    cameraPositionState.position.zoom
                } else {
                    15f // Zoom predeterminado solo para la primera inicialización
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Solo mostrar el mapa si está visible (controla ciclo de vida)
            if (isMapVisible) {
                // Mapa de Google con optimizaciones
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false, // Usamos nuestro propio marcador
                        mapType = MapType.NORMAL,
                        // Reducir el uso de memoria caché
                        minZoomPreference = 5f,
                        maxZoomPreference = 20f,
                        isTrafficEnabled = false, // Desactivar tráfico para reducir uso de memoria
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true,
                        mapToolbarEnabled = false,
                    ),
                    // Guardar referencia del mapa cuando se crea
                    onMapLoaded = {
                        println("Mapa cargado completamente")
                    }
                ) {
                    // Efecto para aplicar configuraciones al mapa
                    MapEffect { map ->
                        // Guardar referencia del mapa para usar en el ciclo de vida
                        mapRef = map

                        // Limitar el uso de caché para evitar los errores de "pinning"
                        map.setMaxZoomPreference(20f)
                        map.setMinZoomPreference(5f)

                        // Optimizar renderizado
                        map.isBuildingsEnabled = false
                        map.isIndoorEnabled = false

                        // Configurar límites de memoria
                        try {
                            // Configurar política de memoria usando reflection
                            val mapClassType = map.javaClass

                            // Deshabilitar el almacenamiento de caché persistente
                            mapClassType.getDeclaredMethod("setPersistentCacheEnabled", Boolean::class.java)?.let {
                                it.isAccessible = true
                                it.invoke(map, false)
                            }

                            // Configurar política de memoria más agresiva para evitar bloqueos
                            mapClassType.getDeclaredMethod("setTrimMemoryPolicy", Int::class.java)?.let {
                                it.isAccessible = true
                                it.invoke(map, 5) // TRIM_MEMORY_RUNNING_MODERATE
                            }

                            // Liberar bloqueos de base de datos al iniciar
                            mapClassType.getDeclaredMethod("releaseDatabaseLocks")?.let {
                                it.isAccessible = true
                                it.invoke(map)
                            }
                        } catch (e: Exception) {
                            // Si falla la reflection, no afecta la funcionalidad principal
                            println("No se pudo configurar política de memoria: ${e.message}")
                        }

                        // Forzar actualización de mapa sin usar pinning
                        map.snapshot { /* No hacer nada, solo para forzar actualización */ }
                    }

                    // Mostrar el polyline de la ruta si hay una ruta activa
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
                }
            } else {
                // Placeholder cuando el mapa no está visible para conservar recursos
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

            // Tarjeta informativa en la parte superior
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar estado de la ruta
                    Text(
                        text = if (isRouteActive) "Ruta activa" else "Sin ruta activa",
                        color = if (isRouteActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    // Mostrar coordenadas del conductor - Actualizado para mostrar actualizaciones
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
                                // Usar el zoom actual para mantener la experiencia del usuario
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
                                // Usar el zoom actual en lugar de un valor fijo
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
                                // Reintentar cargar la ubicación del conductor
                                viewModel.setDriverId(1) // Esto forzará una recarga
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

