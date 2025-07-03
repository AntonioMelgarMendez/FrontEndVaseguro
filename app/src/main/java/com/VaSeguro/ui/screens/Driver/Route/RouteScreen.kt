package com.VaSeguro.ui.screens.Driver.Route

import android.Manifest
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.R
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.helpers.bitmapDescriptorFromVector
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.ui.components.Dialogs.ConfirmationDialog
import com.VaSeguro.ui.components.Dialogs.StopInfoDialog
import com.VaSeguro.ui.components.Map.FloatingMenu
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteScreen(
    viewModel: RouteScreenViewModel = viewModel(factory = RouteScreenViewModel.Factory),
    routeId: Int? = null,  // Parámetro para recibir el ID de la ruta
    onNavigateToSavedRoutes: () -> Unit = {}, // Navegación a pantalla de rutas guardadas
    snackbarHostState: SnackbarHostState // Nuevo parámetro para el SnackbarHostState
) {
    // Estados principales
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    val selectedRoute by remember { derivedStateOf { viewModel.selectedRoute } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val routeProgress by viewModel.routeProgress.collectAsStateWithLifecycle()

    // Estados para controlar la navegación y el estado de la ruta
    val currentRouteId by viewModel.currentRouteId.collectAsStateWithLifecycle()

    // Nuevos estados para la información dinámica
    val nextPointName by viewModel.nextPointName.collectAsStateWithLifecycle()
    val timeToNextPoint by viewModel.timeToNextPoint.collectAsStateWithLifecycle()
    val adjustedTimeToNextPoint by viewModel.adjustedTimeToNextPoint.collectAsStateWithLifecycle()
    val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()

    // Estado del estado de la ruta
    val routeStatus by viewModel.currentRouteStatus.collectAsStateWithLifecycle()

    // NUEVO: Estados para el timing de segmentos
    val currentSegmentElapsedTime by viewModel.currentSegmentElapsedTime.collectAsStateWithLifecycle()
    val realTimeToNextPoint by viewModel.realTimeToNextPoint.collectAsStateWithLifecycle()
    val segmentTimings by viewModel.segmentTimings.collectAsStateWithLifecycle()

    // Estado para el tipo de ruta (INBOUND/OUTBOUND)
    val currentRouteType by viewModel.currentRouteType.collectAsStateWithLifecycle()

    // Estados para control de ruta
    var showStartRouteConfirmation by remember { mutableStateOf(false) }
    // Estado para controlar si la ruta ya fue iniciada. Usamos rememberSaveable para que
    // sobreviva a cambios de configuración (como rotar la pantalla).
    var isRouteStarted by rememberSaveable { mutableStateOf(false) }

    // Este estado sobrevive a cambios de configuración y a estar en el backstack.
    // Se reinicia a `false` si el `routeId` cambia.
    var hasBeenClearedOnThisRoute by rememberSaveable(routeId) { mutableStateOf(false) }

    // NUEVOS: Estados para manejo de desviaciones y recálculo automático
    val isDeviatedFromRoute by viewModel.isDeviatedFromRoute.collectAsStateWithLifecycle()
    val completedSegments by viewModel.completedSegments.collectAsStateWithLifecycle()
    val originalCompleteRoute by viewModel.originalCompleteRoute.collectAsStateWithLifecycle()

    // Estado para controlar la visualización de información de desviación
    var showDeviationInfo by remember { mutableStateOf(false) }
    var showRecalculateDialog by remember { mutableStateOf(false) }

    // Efecto para cargar la ruta, asegurando que solo se ejecute una vez por ruta válida.
    LaunchedEffect(routeId, currentRouteId, hasBeenClearedOnThisRoute, currentLocation) {
        val shouldLoad = routeId != null && routeId > 0 && routeId != currentRouteId && !hasBeenClearedOnThisRoute

        if (shouldLoad) {
            if (currentLocation != null) {
                // Tenemos todo para cargar la ruta.
                viewModel.loadSavedRoute(routeId)
                isRouteStarted = false
            }
            // Si la ubicación no está disponible, el efecto se volverá a ejecutar cuando
            // `currentLocation` se actualice, y la carga se intentará de nuevo.
        }
    }


    // Estados de UI
    val cameraPositionState = rememberCameraPositionState()
    val modalBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Contexto y servicios de ubicación
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Configuración de solicitud de ubicación
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Actualización cada 5 segundos
            .setMinUpdateIntervalMillis(2000) // Mínimo 2 segundos entre actualizaciones
            .setMaxUpdateDelayMillis(10000) // Máximo 10 segundos de retraso
            .build()
    }

    // Estado de permisos
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Estado de diálogos
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var systemPermissionDialogShown by remember { mutableStateOf(false) }

    // Obtener última ubicación conocida inmediatamente después de obtener permisos
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            try {
                val lastLocationTask = fusedLocationClient.lastLocation
                lastLocationTask.addOnSuccessListener { location ->
                    location?.let {
                        val initialLatLng = LatLng(location.latitude, location.longitude)
                        viewModel.updateCurrentLocation(initialLatLng, routeStatus)

                        // Mover cámara a la ubicación inicial
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f)
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Manejo de excepción si los permisos no están disponibles
                viewModel.showError("Error al obtener ubicación: permisos insuficientes")
            }
        }
    }

    // Callback para recibir actualizaciones de ubicación
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    viewModel.updateCurrentLocation(currentLatLng, routeStatus )

                    // Mover la cámara automáticamente si la ruta está iniciada,
                    // o si no hay ninguna ruta seleccionada (para seguir al usuario al abrir la pantalla).
                    if (isRouteStarted || selectedRoute == null) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Efecto para manejar el flujo de permisos
    LaunchedEffect(locationPermissions.permissions) {
        if (!permissionRequested) {
            permissionRequested = true

            // Primero verificamos si ya tenemos permisos
            if (locationPermissions.allPermissionsGranted) {
                return@LaunchedEffect
            }

            // Verificamos si debemos mostrar la explicación primero
            val shouldShowRationale = locationPermissions.permissions.any { permission ->
                permission.status.shouldShowRationale
            }

            if (shouldShowRationale) {
                showPermissionDialog = true
            } else {
                // Si no necesita explicación, solicitamos directamente
                systemPermissionDialogShown = true
                locationPermissions.launchMultiplePermissionRequest()
            }
        }
    }

    // Efecto para iniciar las actualizaciones de ubicación
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                viewModel.showError("Error al iniciar seguimiento de ubicación")
            }
        }
    }

    // Mostrar mensaje de error si existe
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.errorMessageShown()
        }
    }

    // ModalBottomSheet para mostrar opciones de ruta
    if (showBottomSheet) {
        LaunchedEffect(Unit) {
            // Expandir automáticamente el BottomSheet al mostrarlo
            modalBottomSheetState.expand()
        }

        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = modalBottomSheetState,
            // Configuramos el sheet para que use el máximo tamaño disponible
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        ) {
            RouteMenuBottomSheetContent(
                viewModel = viewModel,
                onDismiss = {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                        showBottomSheet = false
                    }
                }
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

// Manejar eventos del ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // App va a segundo plano
                    viewModel.pauseRouteOnAppBackground()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // App vuelve al primer plano
                    viewModel.resumeRouteOnAppForeground()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // App se cierra
                    viewModel.finalizeRouteOnAppDestroy()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // NUEVO: Dialog para mostrar información de paradas cercanas
    val isStopInfoDialogVisible by viewModel.isStopInfoDialogVisible.collectAsStateWithLifecycle()
    val nearbyStop by viewModel.currentNearbyStop.collectAsStateWithLifecycle()
    val nearbyStopPassengers by viewModel.currentStopPassengers.collectAsStateWithLifecycle()
    val stopCompletionStates by viewModel.stopCompletionStates.collectAsStateWithLifecycle()

    // NUEVO: Estados para el diálogo de confirmación de cierre
    val showStopCloseConfirmation by viewModel.showStopCloseConfirmation.collectAsStateWithLifecycle()

    // Mostrar diálogo de información de parada
    StopInfoDialog(
        isVisible = isStopInfoDialogVisible,
        stopData = nearbyStop,
        stopPassengers = nearbyStopPassengers,
        currentStopStates = stopCompletionStates,
        onDismiss = { viewModel.dismissStopInfoDialogWithConfirmation() }, // MODIFICADO: Usar nueva función
        onStateChanged = { stopPassengerId, isCompleted ->
            viewModel.updateStopPassengerState(stopPassengerId, isCompleted)
        },
        routeType = currentRouteType // Pasar el tipo de ruta actual al diálogo
    )

    // NUEVO: Diálogo de confirmación para cerrar StopInfoDialog
    ConfirmationDialog(
        isVisible = showStopCloseConfirmation,
        title = "Confirmar cierre de parada",
        message = "¿Estás seguro de que quieres cerrar esta parada? Si no has completado todos los pasajeros, no se avanzará al siguiente punto de la ruta.",
        confirmButtonText = "Cerrar parada",
        dismissButtonText = "Continuar trabajando",
        onConfirm = { viewModel.confirmStopClose() },
        onDismiss = { viewModel.cancelStopCloseConfirmation() }
    )

    // Dialog de configuración del umbral de proximidad
    var showProximitySettingsDialog by remember { mutableStateOf(false) }
    var proximityThreshold by remember { mutableFloatStateOf(viewModel.stopProximityThreshold.value.toFloat()) }

    if (showProximitySettingsDialog) {
        AlertDialog(
            onDismissRequest = { showProximitySettingsDialog = false },
            title = { Text("Configurar distancia de detección") },
            text = {
                Column {
                    Text(
                        "Ajusta la distancia (en metros) a la que se mostrará la notificación cuando te acerques a una parada.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "20m",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Slider(
                            value = proximityThreshold,
                            onValueChange = { proximityThreshold = it },
                            valueRange = 20f..200f,
                            steps = 18,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "200m",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "${proximityThreshold.toInt()} metros",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setStopProximityThreshold(proximityThreshold.toDouble())
                        showProximitySettingsDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProximitySettingsDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para iniciar la ruta
    ConfirmationDialog(
        isVisible = showStartRouteConfirmation,
        title = "Iniciar ruta",
        message = "¿Estás seguro de iniciar esta ruta? Una vez iniciada, se comenzará a monitorear tu progreso.",
        confirmButtonText = "Iniciar",
        onConfirm = {
            // Aquí es donde realmente inicia la ruta
            isRouteStarted = true
            // Si el progreso es 0, lo inicializamos con un valor pequeño para mostrar que ha comenzado
            if (viewModel.routeProgress.value == 0f) {
                viewModel.updateRouteProgress(0.001f)
            }
            // Actualizar el estado de la ruta a "En proceso"
            viewModel.updateRouteStatus(RouteStatus.ON_PROGRESS.id)
            showStartRouteConfirmation = false

            // Mostrar mensaje de confirmación
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Ruta iniciada correctamente")
            }
        },
        onDismiss = { showStartRouteConfirmation = false }
    )

    // Box principal que contiene todo el contenido de RouteScreen
    Box(modifier = Modifier.fillMaxSize()) { // Box exterior para anclar elementos flotantes
        // Box interior para el contenido principal (mapa, etc.) que necesita padding
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Mapa principal
            if (locationPermissions.allPermissionsGranted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL,
                        isTrafficEnabled = false,
                        isBuildingEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,

                        ),
                    onMapLoaded = {
                        // Cuando se carga el mapa, centramos en la ubicación actual si existe
                        currentLocation?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(location, 15f)
                            )
                            }
                        }
                    }
                ) {

                    currentLocation?.let { location ->
                        Marker(
                            state = MarkerState(LatLng(location.latitude, location.longitude)),
                            icon = context.bitmapDescriptorFromVector(R.drawable.bus_map, heightDp = 40),
                            title = "Tu ubicación"
                        )
                    }

                    // Mostrar marcadores para los puntos de ruta
                    routePoints.forEach { point ->
                        // No mostrar el marcador si no tiene un tipo de parada (ubicación actual)
                        if (point.stopType != null) {
                            Marker(
                                state = MarkerState(position = point.location),
                                title = point.name.ifEmpty { "Punto de ruta" },
                                icon = if (point.stopType == StopType.HOME) {
                                    context.bitmapDescriptorFromVector(R.drawable.user, heightDp = 40)
                            } else {
                                context.bitmapDescriptorFromVector(R.drawable.school,heightDp= 40)
                            },
                            onClick = {
                                // Al hacer clic en un marcador, verificar si estamos lo suficientemente cerca
                                // para mostrar el diálogo de información de parada
                                viewModel.checkMarkerProximityAndShowDialog(point.location)
                                // Devolver true para que el mapa no muestre la ventana de información por defecto
                                true
                            }
                        )
                        }
                    }

                    // Mostrar polyline de la ruta si existe
                    selectedRoute?.polyline?.encodedPolyline?.let { polyline ->
                        val routePath = decodePolyline(polyline)
                        if (routePath.isNotEmpty()) {
                            Polyline(
                                points = routePath,
                                color = Color.Blue,
                                width = 8f
                            )
                        }
                    }
                }
            } else {
                // Mostrar mensaje si no hay permisos
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Para usar esta función, necesitas conceder permisos de ubicación")
                }
            }

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                    strokeWidth = 4.dp
                )
            }

            // Panel de información de ruta activa
            if (selectedRoute != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    // NUEVO: Indicador de desviación de ruta
                    if (isDeviatedFromRoute && isRouteStarted) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.9f) // Naranja para advertencia
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Desviación",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "¡Desviación detectada!",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Te has salido de la ruta. Se recalculará automáticamente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                IconButton(
                                    onClick = { showRecalculateDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Recalcular ahora",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (completedSegments.isNotEmpty() && isRouteStarted) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.8f) // Verde para completados
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completados",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${completedSegments.size} tramo${if (completedSegments.size != 1) "s" else ""} completado${if (completedSegments.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { showDeviationInfo = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Ver detalles",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Indicador de progreso
                    if (routeProgress > 0 || isRouteStarted) {
                        LinearProgressIndicator(
                            progress = if (routeProgress > 0) routeProgress else 0.001f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // NUEVO: Selector de tipo de ruta
                    if (!isRouteStarted) {  // Solo permitir cambiar si la ruta no está iniciada
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tipo de ruta:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Row {
                                RouteTypeChip(
                                    type = RouteType.INBOUND,
                                    isSelected = currentRouteType == RouteType.INBOUND,
                                    onSelect = { viewModel.updateRouteType(RouteType.INBOUND) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                RouteTypeChip(
                                    type = RouteType.OUTBOUND,
                                    isSelected = currentRouteType == RouteType.OUTBOUND,
                                    onSelect = { viewModel.updateRouteType(RouteType.OUTBOUND) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Información del próximo punto
                    if (nextPointName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Próximo punto:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = nextPointName,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if(isRouteStarted){
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    // Mostrar tiempo real restante si está disponible
                                    if (realTimeToNextPoint.isNotEmpty()) {
                                        Text(
                                            text = "Tiempo restante:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = realTimeToNextPoint,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "Tiempo estimado:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            textAlign = TextAlign.End
                                        )
                                        // Mostrar tiempo estimado ajustado si está disponible, de lo contrario el tiempo original
                                        val timeToShow = if (adjustedTimeToNextPoint.isNotEmpty())
                                            adjustedTimeToNextPoint else timeToNextPoint

                                        Text(
                                            text = timeToShow,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Mostrar velocidad actual
                        if (currentSpeed > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Velocidad",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "%.1f km/h".format(currentSpeed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    } else if (routeProgress > 0.95f || routeStatus == RouteStatus.FINISHED) {
                        // Si estamos casi al final de la ruta o la ruta está finalizada
                        Column(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            if (routeStatus == RouteStatus.FINISHED) {
                                // Mensaje de ruta finalizada
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completado",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "¡Ruta completada exitosamente!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Todas las paradas han sido procesadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // Mensaje de llegada al destino (implementación anterior)
                                Text(
                                    text = "¡Has llegado a tu destino!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Menú de estados del viaje (solo visible cuando la ruta está iniciada)
                if (isRouteStarted && selectedRoute != null) {
                    // Estado para el diálogo de confirmación para borrar la ruta
                    var showDeleteConfirmation by remember { mutableStateOf(false) }
                    // Estado para el diálogo de confirmación para pausar la ruta
                    var showPauseConfirmation by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp) // Añadir espacio para no solapar con el menú flotante
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Estado del viaje: ${routeStatus?.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Botón "En proceso"
                                val isProcessActive = routeStatus == RouteStatus.ON_PROGRESS
                                RouteStatusButton(
                                    text = "En proceso",
                                    icon = Icons.Default.DirectionsCar,
                                    isActive = isProcessActive,
                                    activeColor = Color(0xFF4CAF50), // Verde
                                    onClick = {
                                        if (!isRouteStarted) isRouteStarted = true
                                        viewModel.updateRouteStatus(RouteStatus.ON_PROGRESS.id)
                                    }
                                )

                                // Botón "Pausado"
                                val isPausedActive = routeStatus == RouteStatus.STOPED
                                RouteStatusButton(
                                    text = "Pausado",
                                    icon = Icons.Default.Pause,
                                    isActive = isPausedActive,
                                    activeColor = Color(0xFFFFA000), // Ámbar
                                    onClick = {
                                        // Mostrar diálogo de confirmación para pausar
                                        showPauseConfirmation = true
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Botón "Finalizado"
                                val isFinishedActive = routeStatus == RouteStatus.PROBLEMS
                                RouteStatusButton(
                                    text = "Con Problemas",
                                    icon = Icons.Default.Warning,
                                    isActive = isFinishedActive,
                                    activeColor = Color(0xFFF44336), // Rojo para problemas
                                    onClick = {
                                        viewModel.updateRouteStatus(RouteStatus.PROBLEMS.id)
                                    }
                                )

                                // Botón "Borrar ruta"
                                RouteStatusButton(
                                    text = "Borrar ruta",
                                    icon = Icons.Default.Delete,
                                    isActive = false,
                                    activeColor = Color(0xFF9C27B0), // Morado
                                    onClick = {
                                        showDeleteConfirmation = true
                                    }
                                )
                            }
                        }
                    }

                    // Diálogo de confirmación para borrar la ruta
                    ConfirmationDialog(
                        isVisible = showDeleteConfirmation,
                        title = "Borrar ruta",
                        message = "¿Estás seguro de borrar esta ruta? Esta acción no se puede deshacer.",
                        confirmButtonText = "Borrar",
                        onConfirm = {
                            viewModel.clearRoute()
                            viewModel.clearSelections() // Limpiar selección de niños
                            isRouteStarted = false
                            hasBeenClearedOnThisRoute = true
                            showDeleteConfirmation = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ruta borrada correctamente")
                            }
                        },
                        onDismiss = { showDeleteConfirmation = false }
                    )

                    // Diálogo de confirmación para pausar la ruta
                    ConfirmationDialog(
                        isVisible = showPauseConfirmation,
                        title = "Pausar ruta",
                        message = "¿Estás seguro de pausar esta ruta? Se detendrá temporalmente el seguimiento.",
                        confirmButtonText = "Pausar",
                        onConfirm = {
                            isRouteStarted = false // pausar la ruta
                            viewModel.updateRouteStatus(RouteStatus.STOPED.id) // CORREGIDO: usar .id en lugar de .toString()
                            showPauseConfirmation = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ruta pausada correctamente")
                            }
                        },
                        onDismiss = { showPauseConfirmation = false }
                    )
                }
            }

            // Menú flotante extendido con opción para rutas guardadas (AHORA FUERA DEL BOX CON PADDING)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp, 16.dp, 16.dp, 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // NUEVO: Botón flotante para recálculo manual cuando hay desviación
                if (isDeviatedFromRoute && isRouteStarted) {
                    FloatingActionButton(
                        onClick = { viewModel.forceRouteRecalculation() },
                        containerColor = Color(0xFFFF9800), // Naranja para recálculo
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recalcular ruta",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Si hay una ruta seleccionada pero no iniciada, mostrar botón de inicio
                if (selectedRoute != null && !isRouteStarted) {
                    //Detectar si es ruta guardada
                    val isRouteSaved by viewModel.isCurrentRouteSaved.collectAsStateWithLifecycle()
                    FloatingActionButton(
                        onClick = {

                            val success = if (isRouteSaved) {
                                // Si es una ruta guardada, usar startSavedRoute()
                                println("DEBUG_START_BUTTON: Iniciando ruta guardada")
                                viewModel.startSavedRoute()
                            } else {
                                // Si es una ruta nueva, usar startNewRoute()
                                println("DEBUG_START_BUTTON: Creando e iniciando nueva ruta")
                                viewModel.startNewRoute()
                            }

                            if (success) {
                                showStartRouteConfirmation = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar ruta")
                    }
                }

                // FloatingMenu posicionado independientemente (AHORA FUERA DEL BOX CON PADDING)
                if (!isRouteStarted) {
                    FloatingMenu(
                        onOptionSelected = { option ->
                            when (option) {
                                1 -> {
                                    // Centrar en ubicación actual
                                    currentLocation?.let { location ->
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(location, 15f)
                                            )
                                        }
                                    } ?: run {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("No hay ubicación disponible")
                                        }
                                    }
                                }
                                2 -> showBottomSheet = true
                                3 -> {
                                    viewModel.clearRoute()
                                    viewModel.clearSelections() // Limpiar selección de niños
                                    hasBeenClearedOnThisRoute = true // Marcamos que esta ruta ha sido borrada.
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Ruta limpiada correctamente")
                                    }
                                }
                                4 -> onNavigateToSavedRoutes()
                            }
                        }
                    )
                }
            }
        }
    }

    // NUEVOS: Diálogos para funcionalidad de desviaciones

    // Diálogo de confirmación para recálculo manual
    ConfirmationDialog(
        isVisible = showRecalculateDialog,
        title = "Recalcular ruta",
        message = "¿Quieres recalcular la ruta desde tu ubicación actual? Esto eliminará los tramos ya completados y optimizará la ruta restante.",
        confirmButtonText = "Recalcular",
        onConfirm = {
            viewModel.forceRouteRecalculation()
            showRecalculateDialog = false
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Recalculando ruta...")
            }
        },
        onDismiss = { showRecalculateDialog = false }
    )

    // Diálogo de información detallada sobre segmentos completados
    if (showDeviationInfo) {
        AlertDialog(
            onDismissRequest = { showDeviationInfo = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completados",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tramos completados")
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (completedSegments.isEmpty()) {
                        item {
                            Text(
                                text = "Aún no has completado ningún tramo de la ruta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        items(completedSegments) { segment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completado",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tramo completado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${segment.startPointName} → ${segment.endPointName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Distancia: ${(segment.distance / 1000.0).format(1)} km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Resumen del progreso",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• ${completedSegments.size} tramo${if (completedSegments.size != 1) "s" else ""} completado${if (completedSegments.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    val totalCompletedDistance = completedSegments.sumOf { it.distance }
                                    Text(
                                        text = "• ${(totalCompletedDistance / 1000.0).format(1)} km recorridos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    if (originalCompleteRoute != null) {
                                        val totalDistance = originalCompleteRoute!!.distanceMeters
                                        val progressPercentage = ((totalCompletedDistance / totalDistance) * 100).toInt()
                                        Text(
                                            text = "• $progressPercentage% de la ruta original completada",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDeviationInfo = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

// Componente para mostrar un chip de selección de tipo de ruta
@Composable
fun RouteTypeChip(
    type: RouteType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onSelect() },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = type.type,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun RouteMenuBottomSheetContent(
    viewModel: RouteScreenViewModel,
    onDismiss: () -> Unit
) {
    // Estados para la sección de niños
    val childrenSearchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredChildren by viewModel.filteredChildren.collectAsStateWithLifecycle()
    val selectedChildIds by viewModel.selectedChildIds.collectAsStateWithLifecycle()
    val currentRouteId by viewModel.currentRouteId.collectAsStateWithLifecycle()

    // Obtenemos el estado de la ruta seleccionada
    val selectedRoute = viewModel.selectedRoute

    // Verificar si hay una ruta activa (cargada desde guardadas O planificada)
    val hasActiveRoute = currentRouteId != null || selectedRoute != null

    // Estado para mostrar advertencia
    var showDisabledSelectionInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Planificar Ruta",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        // Indicador de ruta cargada
        if (hasActiveRoute) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hay una ruta activa. Para modificar los niños, primero debes borrar la ruta actual.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de niños
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Niños disponibles:",
                style = MaterialTheme.typography.titleMedium
            )

            if (hasActiveRoute) {
                IconButton(onClick = { showDisabledSelectionInfo = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buscador de niños
        OutlinedTextField(
            value = childrenSearchQuery ?: "", // Proteger contra null
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar niños...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (!childrenSearchQuery.isNullOrEmpty()) { // Usar isNullOrEmpty
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            enabled = !hasActiveRoute
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de niños filtrados
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            userScrollEnabled = true
        ) {
            items(
                items = filteredChildren,
                key = { child -> "${child.id}_${selectedChildIds.contains(child.id)}" }
            ) { child ->
                val isChildSelected = selectedChildIds.contains(child.id)

                ChildSelectionCard(
                    child = child,
                    isSelected = isChildSelected,
                    onToggleSelection = {
                        if (!hasActiveRoute) {
                            // Llamamos a la función que devuelve el nuevo estado y lo usamos directamente
                            val isNowSelected = viewModel.toggleChildSelection(child.id)

                            if (isNowSelected) {
                                // Añadir paradas, sin calcular ruta automáticamente
                                viewModel.addChildStopsToRoute(child.id, false)
                            } else {
                                // Si se deselecciona, limpiar sus paradas
                                viewModel.removeChildStops(child.id)
                            }
                        } else {
                            // Si hay una ruta cargada, mostrar recordatorio
                            showDisabledSelectionInfo = true
                        }
                    },
                    isEnabled = !hasActiveRoute
                )
            }
        }

        // Lista de puntos seleccionados
        if (viewModel.routePoints.isNotEmpty()) {
            // Resumen de puntos seleccionados
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Puntos seleccionados: ${viewModel.routePoints.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                // Badge con el número de niños seleccionados
                if (selectedChildIds.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = "${selectedChildIds.size} ${if (selectedChildIds.size == 1) "niño" else "niños"} seleccionado${if (selectedChildIds.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de puntos añadidos a la ruta (limitada a los primeros 5)
            val displayPoints = if (viewModel.routePoints.size > 5) {
                viewModel.routePoints.take(5) + RoutePoint(LatLng(0.0, 0.0), "... y ${viewModel.routePoints.size - 5} puntos más")
            } else {
                viewModel.routePoints
            }

            // Tarjeta con los puntos de la ruta
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    displayPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (!point.name.isNullOrEmpty()) point.name else "Punto sin nombre", // Usar isNullOrEmpty
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Mostrar información de la ruta si está calculada
            viewModel.selectedRoute?.let { route ->
                Text(
                    text = "Información de la ruta calculada:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ruta optimizada",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Distancia: ${(route.distanceMeters / 1000.0).format(1)} km",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Tiempo estimado: ${
                                    if (viewModel.currentSegmentIndex.collectAsStateWithLifecycle().value < route.segments.size) {
                                        route.segments[viewModel.currentSegmentIndex.collectAsStateWithLifecycle().value].duration
                                    } else {
                                        route.duration
                                    }
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.calculateRoute()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.routePoints.size >= 2
            ) {
                Text("Calcular ruta")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Diálogo de información sobre selección deshabilitada
    ConfirmationDialog(
        isVisible = showDisabledSelectionInfo,
        title = "Selección deshabilitada",
        message = "Para modificar la selección de niños, primero debes borrar la ruta actual usando el botón 'Limpiar ruta' en el menú principal.",
        confirmButtonText = "Entendido",
        dismissButtonText = "", // Sin botón de cancelar
        onConfirm = { showDisabledSelectionInfo = false },
        onDismiss = { showDisabledSelectionInfo = false }
    )
}


@Composable
fun SearchResultItem(
    place: PlaceResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Extensión para formatear números double
fun Double.format(digits: Int): String = "%.${digits}f".format(this)

@Composable
fun RouteStatusButton(
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(160.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
