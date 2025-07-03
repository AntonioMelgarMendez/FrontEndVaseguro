package com.VaSeguro.ui.components.Container

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.HistoryInfo.TripInfo
import com.VaSeguro.ui.theme.PrimaryColor
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.VaSeguro.R
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun TripCardItem(trip: TripInfo, onViewMoreClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trip.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Duration",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(trip.duration, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pickup: ${trip.pickupTime}", style = MaterialTheme.typography.bodySmall)
                    Text("Arrival: ${trip.arrivalTime}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Driver: ${trip.driver}", style = MaterialTheme.typography.bodySmall)
                    Text("Bus: ${trip.bus}", style = MaterialTheme.typography.bodySmall)
                    Text("Distance: ${trip.distance}", style = MaterialTheme.typography.bodySmall)
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    if (trip.routePoints.isNotEmpty()) {
                        val cameraPositionState = rememberCameraPositionState()
                        LaunchedEffect(trip.routePoints) {
                            val builder = LatLngBounds.builder()
                            trip.routePoints.forEach { builder.include(it) }
                            val bounds = builder.build()
                            cameraPositionState.move(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 32)
                            )
                        }
                        GoogleMap(
                            modifier = Modifier.matchParentSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                zoomGesturesEnabled = false,
                                scrollGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                myLocationButtonEnabled = false,
                                mapToolbarEnabled = false
                            ),
                            properties = MapProperties(
                                isMyLocationEnabled = false
                            )
                        ) {
                            Polyline(
                                points = trip.routePoints,
                                color = ComposeColor(0xFF1976D2),
                                width = 6f
                            )
                            trip.routePoints.forEach { point ->
                                DotMarker(position = point)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onViewMoreClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("View more")
            }
        }
    }
}

@Composable
fun DotMarker(position: LatLng) {
    val context = LocalContext.current
    var icon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(Unit) {
        val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.user)
        val size = 32
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        icon = BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    if (icon != null) {
        Marker(
            state = rememberMarkerState(position = position),
            icon = icon
        )
    }
}