package com.VaSeguro.ui.screens.Parents.Bus

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.R
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.helpers.Resource
import com.VaSeguro.ui.components.Misc.InfoBox

@Composable
fun BusScreen() {
    val context = LocalContext.current
    val viewModel: BusViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return BusViewModel(
                    appProvider.provideUserPreferences(),
                    appProvider.provideVehicleRepository(),
                    appProvider.provideAuthRepository(),
                    context
                ) as T
            }
        }
    )

    var isLoadingImage by remember { mutableStateOf(true) }
    val resolvedImageUrl by viewModel.resolvedImageUrl.collectAsState()
    val driverFullName by viewModel.driverFullName.collectAsState()
    val driverPhoneNumber by viewModel.driverPhoneNumber.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.loadVehicle()
    }

    val vehicleResource by viewModel.vehicle.collectAsState()

    when (vehicleResource) {
        is Resource.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${(vehicleResource as Resource.Error).message}")
            }
        }
        is Resource.Success -> {
            val vehicleResponse = (vehicleResource as Resource.Success).data
            Log.d("BusScreen", "Vehicle data: $vehicleResponse")
            // Convert to domain model if needed
            val vehicle = Vehicle(
                id = vehicleResponse.id.toString(),
                plate = vehicleResponse.plate,
                driver_id = vehicleResponse.driverId.toString(),
                model = vehicleResponse.model,
                brand = vehicleResponse.brand,
                year = vehicleResponse.year,
                color = vehicleResponse.color,
                capacity = vehicleResponse.capacity,
                updated_at = vehicleResponse.update_at,
                carPic = vehicleResponse.carPic ?: "",
                created_at = vehicleResponse.created_at
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        text = "${vehicle.model} (${vehicle.plate})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LaunchedEffect(vehicle.carPic) {
                            viewModel.resolveVehicleImage(vehicle.carPic)
                        }
                        AsyncImage(
                            model = resolvedImageUrl ?: R.drawable.default_bus,
                            contentDescription = vehicle.model,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                            onState = {
                                isLoadingImage = when (it) {
                                    is AsyncImagePainter.State.Loading -> true
                                    is AsyncImagePainter.State.Success,
                                    is AsyncImagePainter.State.Error -> false
                                    else -> false
                                }
                            }
                        )
                        if (isLoadingImage) {
                            CircularProgressIndicator()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoBox(
                        icon = Icons.Default.Person,
                        title = "Driver:",
                        data = driverFullName ?: "Juan"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.DirectionsCar,
                        title = "Plate:",
                        data = vehicle.plate
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.Build,
                        title = "Brand:",
                        data = vehicle.brand
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.Event,
                        title = "Year:",
                        data = vehicle.year
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.Palette,
                        title = "Color:",
                        data = vehicle.color
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.People,
                        title = "Capacity:",
                        data = vehicle.capacity
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.Phone,
                        title = "Phone number:",
                        data = driverPhoneNumber ?: "+50362819210"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.AccessTime,
                        title = "Schedule",
                        data = "07:00 AM - 05:00 PM"
                    )
                }
            }
        }
    }
}