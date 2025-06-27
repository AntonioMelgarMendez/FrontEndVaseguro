
package com.VaSeguro.ui.screens.Parents.Bus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
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
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
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
    val isDriverLoading by viewModel.isDriverLoading.collectAsState()
    val vehicleResource by viewModel.vehicle.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // Modal state and editable fields
    var showEditDialog by remember { mutableStateOf(false) }
    var editableBrand by remember { mutableStateOf("") }
    var editableModel by remember { mutableStateOf("") }
    var editablePlate by remember { mutableStateOf("") }
    var editableYear by remember { mutableStateOf("") }
    var editableColor by remember { mutableStateOf("") }
    var editableCapacity by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadVehicle()
    }

    when {
        vehicleResource is Resource.Loading || isDriverLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        vehicleResource is Resource.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${(vehicleResource as Resource.Error).message}")
            }
        }
        vehicleResource is Resource.Success -> {
            val vehicleResponse = (vehicleResource as Resource.Success).data
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

            // Initialize editable fields when dialog opens
            fun fillEditFields() {
                editableBrand = vehicle.brand
                editableModel = vehicle.model
                editablePlate = vehicle.plate
                editableYear = vehicle.year
                editableColor = vehicle.color
                editableCapacity = vehicle.capacity
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    // Title above the image
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${vehicle.model}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(200.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        // Vehicle image
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LaunchedEffect(vehicle.carPic) {
                                viewModel.resolveVehicleImage(vehicle.carPic)
                            }
                            AsyncImage(
                                model = resolvedImageUrl ?: R.drawable.default_bus,
                                contentDescription = vehicle.model,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().padding(5.dp),
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
                        if (userRole == 4) {
                            IconButton(
                                onClick = {
                                    fillEditFields()
                                    showEditDialog = true
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp) // Increase size
                                        .background(
                                            color = Color.White,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp) // Icon size
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    InfoBox(
                        icon = Icons.Default.Build,
                        title = "Brand:",
                        data = vehicle.brand
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.DirectionsCar,
                        title = "Model:",
                        data = vehicle.model
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoBox(
                        icon = Icons.Default.DirectionsCar,
                        title = "Plate:",
                        data = vehicle.plate
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
                        icon = Icons.Default.Person,
                        title = "Driver:",
                        data = driverFullName ?: "Juan"
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

            // Edit modal dialog
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Vehicle") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editableBrand,
                                onValueChange = { editableBrand = it },
                                label = { Text("Brand") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editableModel,
                                onValueChange = { editableModel = it },
                                label = { Text("Model") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editablePlate,
                                onValueChange = { editablePlate = it },
                                label = { Text("Plate") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editableYear,
                                onValueChange = { editableYear = it },
                                label = { Text("Year") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editableColor,
                                onValueChange = { editableColor = it },
                                label = { Text("Color") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editableCapacity,
                                onValueChange = { editableCapacity = it },
                                label = { Text("Capacity") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.editVehicle(
                                plate = editablePlate,
                                model = editableModel,
                                brand = editableBrand,
                                year = editableYear,
                                color = editableColor,
                                capacity = editableCapacity,
                                carPic = null
                            ) { success, _ ->
                                if (success) {
                                    showEditDialog = false
                                }
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}