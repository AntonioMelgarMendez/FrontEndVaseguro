package com.VaSeguro.ui.screens.Admin.Stops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.ui.components.Cards.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog

@Composable
fun StopsAdminScreen() {
    val context = LocalContext.current
    val viewModel: StopsAdminScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return StopsAdminScreenViewModel(
                    appProvider.provideStopsRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    val stops = viewModel.stops.collectAsState().value
    val expandedMap = viewModel.expandedMap.collectAsState().value
    val checkedMap = viewModel.checkedMap.collectAsState().value
    val isLoading = viewModel.loading.collectAsState().value

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIdToDelete by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var stopToEdit by remember { mutableStateOf<StopData?>(null) }

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
                horizontalArrangement = Arrangement.Start,
            ) {
                Button(
                    onClick = { /* No filter action yet */ },
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    itemsIndexed(stops) { index, stop ->
                        val isFirst = index == 0
                        val isLast = index == stops.lastIndex

                        val shape = when {
                            isFirst && isLast -> RoundedCornerShape(16.dp)
                            isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                            else -> RectangleShape
                        }

                        AdminCardItem(
                            id = stop.id.toString(),
                            title = stop.name,
                            subtitle = "",
                            details = listOf(
                                "Latitude" to stop.latitude.toString(),
                                "Longitude" to stop.longitude.toString()
                            ),
                            isExpanded = expandedMap[stop.id.toString()] ?: false,
                            isChecked = checkedMap[stop.id.toString()] ?: false,
                            shape = shape,
                            onCheckedChange = { viewModel.setChecked(stop.id.toString(), it) },
                            onEditClick = {
                                stopToEdit = stop
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedIdToDelete = stop.id.toString()
                                showDeleteDialog = true
                            },
                            onToggleExpand = { viewModel.toggleExpand(stop.id.toString()) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedIdToDelete != null) {
        ConfirmationDialog(
            message = "Are you sure you want to delete this stop?",
            onConfirm = {
                viewModel.deleteStop(selectedIdToDelete!!)
                showDeleteDialog = false
                selectedIdToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedIdToDelete = null
            }
        )
    }

    if (showEditDialog && stopToEdit != null) {
        EditStopDialog(
            stop = stopToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedStop, driverId ->
                viewModel.editStop(updatedStop, driverId)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditStopDialog(
    stop: StopData,
    onDismiss: () -> Unit,
    onSave: (StopData, Int?) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(stop.name)) }
    var latitude by remember { mutableStateOf(TextFieldValue(stop.latitude.toString())) }
    var longitude by remember { mutableStateOf(TextFieldValue(stop.longitude.toString())) }
    var driverId by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") }
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") }
                )
                OutlinedTextField(
                    value = driverId,
                    onValueChange = { driverId = it },
                    label = { Text("Driver ID") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedStop = stop.copy(
                        name = name.text,
                        latitude = latitude.text.toDoubleOrNull() ?: stop.latitude,
                        longitude = longitude.text.toDoubleOrNull() ?: stop.longitude
                    )
                    val driverIdValue = driverId.text.toIntOrNull()
                    onSave(updatedStop, driverIdValue)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StopsAdminPreview() {
    StopsAdminScreen()
}