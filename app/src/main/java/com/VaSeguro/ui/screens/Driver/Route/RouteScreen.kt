package com.VaSeguro.ui.screens.Driver.Route

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.RoutePoint
import com.VaSeguro.map.decodePolyline
import com.VaSeguro.ui.components.Map.FloatingMenu
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteScreen(
    viewModel: RouteScreenViewModel = viewModel(factory = RouteScreenViewModel.Factory)
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

    // Nuevos estados para la información dinámica
    val nextPointName by viewModel.nextPointName.collectAsStateWithLifecycle()
    val timeToNextPoint by viewModel.timeToNextPoint.collectAsStateWithLifecycle()
    val adjustedTimeToNextPoint by viewModel.adjustedTimeToNextPoint.collectAsStateWithLifecycle()
    val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()

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

                    // Solo mover la cámara automáticamente si:
                    // 1. No hay ruta activa, o
                    // 2. Es la primera ubicación que recibimos
                    if (selectedRoute == null || currentLocation == null) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                        )
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
        if (isProximityAlertVisible && currentPointName.isNotEmpty()) {
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
                        isMyLocationEnabled = true,
                        mapType = MapType.NORMAL,
                        isTrafficEnabled = true
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
                    // Mostrar marcadores para los puntos de ruta
                    routePoints.forEach { point ->
                        Marker(
                            state = MarkerState(position = point.location),
                            title = point.name.ifEmpty { "Punto de ruta" }
                        )
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
                    if (routeProgress > 0) {
                        LinearProgressIndicator(
                            progress = { routeProgress },
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

            // Menú flotante
            FloatingMenu(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
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
                        3 -> viewModel.clearRoute()
                        4 -> {
                            // Ver todas las rutas (para implementación futura)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Función en desarrollo")
                            }
                        }
                    }
                }
            )

            // Bottom sheet para planificación de ruta
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = modalBottomSheetState,
                ) {
                    RouteMenuBottomSheetContent(
                        viewModel = viewModel,
                        onDismiss = { showBottomSheet = false }
                    )
                }
            }

            // Diálogo de permisos
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Permisos requeridos") },
                    text = {
                        Text("La ubicación es necesaria para mostrar tu posición en el mapa y calcular rutas. Por favor, habilita los permisos de ubicación.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showPermissionDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Abrir configuración")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }

}

@Composable
fun RouteMenuBottomSheetContent(
    viewModel: RouteScreenViewModel,
    onDismiss: () -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<PlaceResult>() }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar lugares...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, "Limpiar")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.value.isNotEmpty()) {
                        coroutineScope.launch {
                            searchResults.clear()
                            val results = viewModel.searchPlaces(searchQuery.value)
                            searchResults.addAll(results)
                        }
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de puntos seleccionados
        if (viewModel.routePoints.isNotEmpty()) {
            Text(
                text = "Puntos de la ruta:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mostrar información de la ruta si está seleccionada
            viewModel.selectedRoute?.let { route ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ruta calculada",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Distancia: ${(route.distanceMeters / 1000.0).format(1)} km",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Duración estimada: ${route.duration}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                itemsIndexed(viewModel.routePoints) { index, point ->
                    RoutePointItem(
                        point = point,
                        onRemove = { viewModel.removeRoutePoint(point.location) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

        // Resultados de búsqueda
        if (searchResults.isNotEmpty()) {
            Text(
                text = "Resultados:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                itemsIndexed(searchResults) { index, place ->
                    SearchResultItem(
                        place = place,
                        onClick = {
                            viewModel.addRoutePoint(
                                LatLng(
                                    place.geometry.location.lat,
                                    place.geometry.location.lng
                                ),
                                place.name
                            )
                            searchQuery.value = ""
                            searchResults.clear()
                        }
                    )
                }
            }
        } else if (searchQuery.value.isNotEmpty() && searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron resultados")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
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

