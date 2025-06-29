package com.VaSeguro.ui.screens.Admin.Routes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RouteResponse
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.ui.components.Cards.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.Misc.CustomizableOutlinedTextField
import com.VaSeguro.ui.theme.PrimaryColor
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoutesAdminScreen(
    viewModel: RoutesAdminScreenViewModel = viewModel(factory = RoutesAdminScreenViewModel.Factory)
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllRoutes()
    }

    val isLoading = viewModel.loading.collectAsState().value
    val routes = viewModel.routes.collectAsState().value
    val expandedMap = viewModel.expandedMap.collectAsState().value
    val checkedMap = viewModel.checkedMap.collectAsState().value
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var routeToEdit by remember { mutableStateOf<RouteResponse?>(null) }
    var selectedIdToDelete by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Filter")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7367F0),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = PrimaryColor
                    )
                }
            } else {
                if (routes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No routes found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                else {
                    LazyColumn {
                        itemsIndexed(routes) { index, route ->
                            val isFirst = index == 0
                            val isLast = index == routes.lastIndex

                            val shape = when {
                                isFirst && isLast -> RoundedCornerShape(16.dp)
                                isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                else -> RectangleShape
                            }

                            // Get the full vehicle object by ID
                            val vehicle = viewModel.getVehicleById(route.vehicle_id)

                            AdminCardItem(
                                id = route.id.toString(),
                                title = route.name,
                                subtitle = "Type: ${route.type_id} | Status: ${route.status_id}",
                                details = listOf(
                                    "Start Date" to route.start_date,
                                    "End Date" to (route.end_date ?: ""),
                                    "Vehicle Plate" to (vehicle?.plate ?: "Unknown"),
                                    "Vehicle Brand" to (vehicle?.brand ?: "Unknown")
                                ),
                                isExpanded = expandedMap[route.id] ?: false,
                                isChecked = checkedMap[route.id] ?: false,
                                shape = shape,
                                onCheckedChange = { viewModel.setChecked(route.id, it) },
                                onEditClick = {
                                    routeToEdit = route
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    selectedIdToDelete = route.id
                                    showDeleteDialog = true
                                },
                                onToggleExpand = { viewModel.toggleExpand(route.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddRouteDialog(
            onDismiss = { showDialog = false },
            onSave = { showDialog = false }
        )
    }

    if (showDeleteDialog && selectedIdToDelete != null) {
        ConfirmationDialog(
            message = "Are you sure you want to delete this item?",
            onConfirm = {
                viewModel.deleteRoute(selectedIdToDelete!!)
                showDeleteDialog = false
                selectedIdToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedIdToDelete = null
            }
        )
    }

    if (showEditDialog && routeToEdit != null) {
        EditRouteDialog(
            route = routeToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedRoute ->
                viewModel.updateRoute(updatedRoute.id, updatedRoute)
                showEditDialog = false
                routeToEdit = null
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteDialog(
    viewModel: RoutesAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    val routeTypes = RouteType.getAll()
    val routeStatuses = RouteStatus.getAll()
    val allVehicles = viewModel.vehicles.collectAsState().value

    var name by remember { mutableStateOf(TextFieldValue(viewModel.name)) }
    var startDate by remember { mutableStateOf(TextFieldValue(viewModel.start_date)) }
    var plate by remember { mutableStateOf(viewModel.plate) }
    var routeName by remember { mutableStateOf(viewModel.routeName) }
    var routeStatus by remember { mutableStateOf(viewModel.routeStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Route") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomizableOutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.updateRouteName(it.text)
                    }, label = "Route Name"
                )
                CustomizableOutlinedTextField(
                    value = startDate,
                    onValueChange = {
                        startDate = it
                        viewModel.updateRouteStartDate(it.text)
                    },
                    label = "Start Date"
                )
                // Uncomment and fix this block if you want to select vehicle by plate
                // DropDownSelector(
                //     "Vehicles",
                //     allVehicles.map { vehicle -> vehicle.plate },
                //     plate
                // ) { selectedPlate ->
                //     val selectedVehicle = allVehicles.find { vehicle -> vehicle.plate == selectedPlate }
                //     if (selectedVehicle != null) {
                //         viewModel.updateRouteVehicleId(selectedVehicle.plate)
                //         plate = selectedPlate
                //     }
                // }
                DropDownSelector(
                    "Route Type",
                    routeTypes.map { it.type },
                    routeName
                ) { selectedType ->
                    viewModel.updateTypeId(routeTypes.find { it.type == selectedType }
                        ?: routeTypes[0])
                    routeName = selectedType
                }

                DropDownSelector(
                    "Route Status",
                    routeStatuses.map { it.status },
                    routeStatus
                ) { selectedStatus ->
                    viewModel.updateStatusId(routeStatuses.find { it.status == selectedStatus }
                        ?: routeStatuses[0])
                    routeStatus = selectedStatus
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (
                        viewModel.areFieldsValid().not()
                    ) {
                        Toast.makeText(
                            context,
                            "Por favor completa todos los campos.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button

                    } else {

                        viewModel.addRoute()
                        onSave()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    viewModel.resetForm()
                    onDismiss()
                },
                border = BorderStroke(2.dp, PrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRouteDialog(
    route: RouteResponse,
    onDismiss: () -> Unit,
    onSave: (RouteResponse) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(route.name)) }
    var startDate by remember { mutableStateOf(TextFieldValue(route.start_date)) }
    var endDate by remember { mutableStateOf(TextFieldValue(route.end_date ?: "")) }
    var routeType by remember { mutableStateOf(route.type_id) }
    var routeStatus by remember { mutableStateOf(route.status_id) }

    val routeTypes = RouteType.getAll()
    val routeStatuses = RouteStatus.getAll()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Route") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomizableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Route Name"
                )
                CustomizableOutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "Start Date"
                )
                CustomizableOutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "End Date"
                )
                CustomizableOutlinedTextField(
                    value = TextFieldValue(routeType.toString()),
                    onValueChange = { routeType = it.text.toInt() },
                    label = "Route Type ID"
                )
                CustomizableOutlinedTextField(
                    value = TextFieldValue(routeStatus.toString()),
                    onValueChange = { routeStatus = it.text.toInt() },
                    label = "Route Status ID"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        route.copy(
                            name = name.text,
                            start_date = startDate.text,
                            end_date = endDate.text,
                            type_id = routeType,
                            status_id = routeStatus
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(2.dp, PrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
            ) { Text("Cancel") }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun RoutesAdminPreview() {
    RoutesAdminScreen()
}