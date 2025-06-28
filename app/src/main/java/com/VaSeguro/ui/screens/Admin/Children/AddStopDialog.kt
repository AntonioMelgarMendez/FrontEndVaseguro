package com.VaSeguro.ui.screens.Admin.Children

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.collections.get
import kotlin.compareTo
import kotlin.text.compareTo
import kotlin.toString
import androidx.compose.material3.ModalBottomSheet

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopDialog(
    onDismiss: () -> Unit,
    onConfirm: (pickupPoint: String, school: String) -> Unit
) {
    val context = LocalContext.current
    var pickupPoint by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }
    var isLoading by remember { mutableStateOf(false) }
    var mapPosition by remember { mutableStateOf(LatLng(19.4326, -99.1332)) }
    var countryCode by remember { mutableStateOf("SV") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition, 15f)
    }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSuggestions by remember { mutableStateOf(false) }
    var lastQuery by remember { mutableStateOf("") }

    // Get current location and country code
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                mapPosition = LatLng(it.latitude, it.longitude)
                latitude = it.latitude.toString()
                longitude = it.longitude.toString()
                pickupPoint = "${it.latitude}, ${it.longitude}"
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    countryCode = addresses[0].countryCode ?: "MX"
                }
            }
        }
    }

    // Animate camera when mapPosition changes
    LaunchedEffect(mapPosition) {
        cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mapPosition, 15f)))
    }

    // Debounced search for schools
    LaunchedEffect(school, countryCode) {
        if (school.length > 2 && school != lastQuery) {
            lastQuery = school
            showSuggestions = false
            isLoading = true
            coroutineScope.launch {
                delay(350)
                suggestions = searchSchools(school, context, countryCode)
                isLoading = false
                showSuggestions = true
            }
        } else if (school.length <= 2) {
            showSuggestions = false
            suggestions = emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()) {
            Text("Agregar Parada", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text("Seleccione el punto de recogida en el mapa y el colegio asociado.", fontSize = 14.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = {},
                    label = { Text("Latitud") },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = {},
                    label = { Text("Longitud") },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        mapPosition = latLng
                        latitude = latLng.latitude.toString()
                        longitude = latLng.longitude.toString()
                        pickupPoint = "${latLng.latitude}, ${latLng.longitude}"
                    }
                ) {
                    Marker(
                        state = MarkerState(position = mapPosition),
                        title = "Punto de recogida"
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = school,
                onValueChange = {
                    school = it
                },
                label = { Text("Colegio (buscar)") },
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = showSuggestions && suggestions.isNotEmpty(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .verticalScroll(rememberScrollState())
                ) {
                    suggestions.forEachIndexed { idx, prediction ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    school = prediction.getFullText(null).toString()
                                    showSuggestions = false
                                    suggestions = emptyList()
                                }
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = prediction.getFullText(null).toString(),
                                fontSize = 16.sp
                            )
                        }
                        if (idx < suggestions.lastIndex) {
                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        }
                    }
                }
            }
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Button(onClick = { onConfirm(pickupPoint, school) }) { Text("Guardar") }
            }
        }
    }
}

// Update your searchSchools function to accept countryCode and set it in the request
suspend fun searchSchools(query: String, context: Context, countryCode: String): List<AutocompletePrediction> {
    val placesClient = Places.createClient(context)
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .setCountries(listOf(countryCode))
        .build()
    return suspendCancellableCoroutine { cont ->
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                cont.resume(response.autocompletePredictions, onCancellation = null)
            }
            .addOnFailureListener {
                cont.resume(emptyList(), onCancellation = null)
            }
    }
}