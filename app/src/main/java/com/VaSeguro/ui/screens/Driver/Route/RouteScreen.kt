package com.VaSeguro.ui.screens.Driver.Route

import android.Manifest
import android.os.Looper
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.R
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.helpers.bitmapDescriptorFromVector
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.ui.components.Dialogs.ConfirmationDialog
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    onNavigateToSavedRoutes: () -> Unit = {} // Navegación a pantalla de rutas guardadas
) {
    // Estados principales
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    val selectedRoute by remember { derivedStateOf { viewModel.selectedRoute } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val routeProgress by viewModel.routeProgress.collectAsStateWithLifecycle()
    val isProximityAlertVisible by viewModel.isProximityAlertVisible.collectAsStateWithLifecycle()
    val currentPointName by viewModel.currentPointName.collectAsStateWithLifecycle()

    // Estados para controlar la navegación y el estado de la ruta
    val currentRouteId by viewModel.currentRouteId.collectAsStateWithLifecycle()

    // Nuevos estados para la información dinámica
    val nextPointName by viewModel.nextPointName.collectAsStateWithLifecycle()
    val timeToNextPoint by viewModel.timeToNextPoint.collectAsStateWithLifecycle()
    val adjustedTimeToNextPoint by viewModel.adjustedTimeToNextPoint.collectAsStateWithLifecycle()
    val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()

    // Estados para control de ruta
    var showStartRouteConfirmation by remember { mutableStateOf(false) }
    // Estado para controlar si la ruta ya fue iniciada. Usamos rememberSaveable para que
    // sobreviva a cambios de configuración (como rotar la pantalla).
    var isRouteStarted by rememberSaveable { mutableStateOf(false) }

    // Este estado sobrevive a cambios de configuración y a estar en el backstack.
    // Se reinicia a `false` si el `routeId` cambia.
    var hasBeenClearedOnThisRoute by rememberSaveable(routeId) { mutableStateOf(false) }

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
    val snackbarHostState = remember { SnackbarHostState() }

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
                        viewModel.updateCurrentLocation(initialLatLng)

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
                    viewModel.updateCurrentLocation(currentLatLng)

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

    // Efecto para mostrar alerta cuando estamos cerca de un punto de ruta
    LaunchedEffect(isProximityAlertVisible, currentPointName) {
        if (isProximityAlertVisible && currentPointName.isNotEmpty() ) {
            snackbarHostState.showSnackbar(
                message = "Estás cerca de: $currentPointName",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Mostrar mensaje de error si existe
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
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
            showStartRouteConfirmation = false

            // Mostrar mensaje de confirmación
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Ruta iniciada correctamente")
            }
        },
        onDismiss = { showStartRouteConfirmation = false }
    )

    // Scaffold para organizar la UI con Snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
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
                    } else if (routeProgress > 0.95f) {
                        // Si estamos casi al final de la ruta
                        Text(
                            text = "¡Has llegado a tu destino!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Menú de estados del viaje (solo visible cuando la ruta está iniciada)
            if (isRouteStarted && selectedRoute != null) {
                // Estado de la ruta
                var currentRouteStatus by remember { mutableStateOf("En proceso") }
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
                            text = "Estado del viaje: $currentRouteStatus",
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
                            val isProcessActive = currentRouteStatus == "En proceso"
                            RouteStatusButton(
                                text = "En proceso",
                                icon = Icons.Default.DirectionsCar,
                                isActive = isProcessActive,
                                activeColor = Color(0xFF4CAF50), // Verde
                                onClick = {
                                    if(!isRouteStarted) isRouteStarted = true
                                    currentRouteStatus = "En proceso"
                                    viewModel.updateRouteStatus("1") // ID para "En proceso"
                                }
                            )

                            // Botón "Pausado"
                            val isPausedActive = currentRouteStatus == "Pausado"
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

                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ){
                            // Botón "Problemas"
                            val isIssueActive = currentRouteStatus == "Problemas"
                            RouteStatusButton(
                                text = "Problemas",
                                icon = Icons.Default.Warning,
                                isActive = isIssueActive,
                                activeColor = Color(0xFFF44336), // Rojo
                                onClick = {
                                    currentRouteStatus = "Problemas"
                                    viewModel.updateRouteStatus("4") // ID para "Problemas"
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
                        currentRouteStatus = "Pausado"
                        viewModel.updateRouteStatus("3") // ID para "Pausado"
                        showPauseConfirmation = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Ruta pausada correctamente")
                        }
                    },
                    onDismiss = { showPauseConfirmation = false }
                )
            }

            // Menú flotante extendido con opción para rutas guardadas
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Si hay una ruta seleccionada pero no iniciada, mostrar botón de inicio
                if (selectedRoute != null && !isRouteStarted) {
                    FloatingActionButton(
                        onClick = { showStartRouteConfirmation = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar ruta")
                    }
                }
                if (!isRouteStarted){
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
}

@Composable
fun RouteMenuBottomSheetContent(
    viewModel: RouteScreenViewModel,
    onDismiss: () -> Unit
) {
    // Estados para la sección de niños
    val childrenSearchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredChildren = viewModel.filteredChildren.collectAsStateWithLifecycle()
    val selectedChildIds = viewModel.selectedChildIds.collectAsStateWithLifecycle()
    val currentRouteId = viewModel.currentRouteId.collectAsStateWithLifecycle()

    // Obtenemos el estado de la ruta seleccionada
    val selectedRoute = viewModel.selectedRoute

    // Estado local para forzar actualizaciones de la UI
    var selectionUpdateTrigger by remember { mutableStateOf(0) }

    // Verificar si hay una ruta activa (cargada desde guardadas O planificada)
    val hasActiveRoute = currentRouteId.value != null || selectedRoute != null

    // Estado para mostrar advertencia
    var showDisabledSelectionInfo by remember { mutableStateOf(false) }

    // Coroutine scope para mostrar snackbar
    val coroutineScope = rememberCoroutineScope()

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
            value = childrenSearchQuery.value,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar niños...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (childrenSearchQuery.value.isNotEmpty()) {
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
                items = filteredChildren.value,
                key = { child -> child.id }  // Usamos ID como clave estable
            ) { child ->
                val isChildSelected = selectedChildIds.value.contains(child.id)

                // La modificación del trigger aquí asegura que el componente se recomponga cuando cambie
                val currentTrigger by remember { derivedStateOf { selectionUpdateTrigger } }

                // Forzamos la recomposición al detectar cambios en el trigger
                LaunchedEffect(currentTrigger) {
                    // Este efecto solo se usa para forzar recomposiciones
                }

                ChildSelectionCard(
                    child = child,
                    isSelected = isChildSelected,
                    onToggleSelection = {
                        if (!hasActiveRoute) {
                            // Primero actualizamos el estado de selección en el viewModel
                            viewModel.toggleChildSelection(child.id)

                            // Determinamos si estamos seleccionando o deseleccionando
                            val willBeSelected = !isChildSelected

                            if (willBeSelected) {
                                // Añadir paradas, sin calcular ruta automáticamente
                                viewModel.addChildStopsToRoute(child.id, false)
                            } else {
                                // Si se deselecciona, limpiar sus paradas
                                viewModel.removeChildStops(child.id)
                            }

                            // Forzamos una actualización de la UI
                            selectionUpdateTrigger++
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
                if (selectedChildIds.value.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = "${selectedChildIds.value.size} ${if (selectedChildIds.value.size == 1) "niño" else "niños"} seleccionado${if (selectedChildIds.value.size != 1) "s" else ""}",
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
                                text = if (point.name.isNotEmpty()) point.name else "Punto sin nombre",
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
                                text = "Tiempo estimado: ${route.duration}",
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
fun RoutePointItem(
    point: RoutePoint,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Punto",
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (point.name.isNotEmpty()) {
                    Text(
                        text = point.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "Lat: ${"%.6f".format(point.location.latitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Lng: ${"%.6f".format(point.location.longitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, "Eliminar")
            }
        }
    }
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
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
