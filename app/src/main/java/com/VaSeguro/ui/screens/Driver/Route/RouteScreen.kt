package com.VaSeguro.ui.screens.Driver.Route

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.map.data.PlaceResult
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
import com.google.android.gms.maps.model.LatLngBounds
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
    val routePoints by remember { derivedStateOf { viewModel.routePoints } }
    val selectedRoute by remember { derivedStateOf { viewModel.selectedRoute } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    val cameraPositionState = rememberCameraPositionState()
    val modalBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
    }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var systemPermissionDialogShown by remember { mutableStateOf(false) }

    // Efecto para manejar el flujo completo de permisos
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


// Efecto para manejar la respuesta de los permisos
    LaunchedEffect(locationPermissions.allPermissionsGranted, systemPermissionDialogShown) {
        if (systemPermissionDialogShown && !locationPermissions.allPermissionsGranted) {
            // Verificamos si algún permiso fue denegado permanentemente
            val permanentlyDenied = locationPermissions.permissions.any { permission ->
                !permission.status.isGranted && !permission.status.shouldShowRationale
            }

            if (permanentlyDenied) {
                showPermissionDialog = true
            }
        }
    }

    // Efecto para obtener la ubicación cuando los permisos son concedidos
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                        )
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permisos requeridos") },
            text = {
                Text("La ubicación es necesaria, habilita los permisos de ubicación en la configuración de la app.")
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissions.allPermissionsGranted) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                )
            ) {
                routePoints.forEach { point ->
                    Marker(
                        state = MarkerState(position = point),
                        title = "Punto de ruta"
                    )
                }

                selectedRoute?.overview_polyline?.points?.let { polyline ->
                    val routePath = decodePolyline(polyline)
                    Polyline(
                        points = routePath,
                        color = Color.Blue,
                        width = 8f
                    )
                }
            }
        }
        else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Permisos de ubicación no concedidos")
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp),
                strokeWidth = 4.dp
            )
        }

        FloatingMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onOptionSelected = { option ->
                when (option) {
                    1 -> {} // Otra acción
                    2 -> showBottomSheet = true
                    3 -> viewModel.clearRoute()
                    4 -> {} // Otra acción
                }
            }
        )

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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                itemsIndexed(viewModel.routePoints) { index, point ->
                    RoutePointItem(
                        point = point,
                        onRemove = { viewModel.removeRoutePoint(point) }
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
                                )
                            )
                            searchQuery.value = ""
                            searchResults.clear()
                        }
                    )
                }
            }
        } else if (searchQuery.value.isNotEmpty()) {
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
    point: LatLng,
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
                Text(
                    text = "Lat: ${"%.6f".format(point.latitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Lng: ${"%.6f".format(point.longitude)}",
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