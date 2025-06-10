package com.VaSeguro.ui.screens.Admin.Stops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.ui.components.Container.ExpandableInfoCard
import com.VaSeguro.ui.components.Container.TopBar
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.screens.Admin.Users.UsersAdminScreen
import com.VaSeguro.ui.theme.PrimaryColor

@Composable
fun StopsAdminScreen(
    viewModel: StopsAdminScreenViewModel = viewModel()
) {
    val stops by viewModel.stops.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var stopToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopBar("Stops")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add") },
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            items(stops) { stop: StopData ->
                ExpandableInfoCard(
                    id = stop.id,
                    title = stop.name,
                    info = listOf(
                        "Latitude" to stop.latitude,
                        "Longitude" to stop.longitude,
                        "Stop Type" to stop.stopType.type,
                        "Driver" to stop.driver
                    ),
                    onEdit = { /* future */ },
                    onDelete = { stopToDelete = stop.id }
                )
            }
        }

        if (showAddDialog) {
            AddStopDialog(
                onDismiss = { showAddDialog = false },
                onSave = { showAddDialog = false }
            )
        }

        stopToDelete?.let {
            ConfirmationDialog(
                message = "Are you sure you want to delete this stop?",
                onConfirm = {
                    viewModel.deleteStop(it)
                    stopToDelete = null
                },
                onDismiss = { stopToDelete = null }
            )
        }
    }
}

@Composable
fun AddStopDialog(
    viewModel: StopsAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var latitude by remember { mutableStateOf(TextFieldValue("")) }
    var longitude by remember { mutableStateOf(TextFieldValue("")) }
    var stopType by remember { mutableStateOf<StopType?>(null) }
    var driver by remember { mutableStateOf<String?>(null) }

    val stopTypes = listOf(StopType("1", "School"), StopType("2", "House"), StopType("3", "Another"))
    val drivers = listOf("Juan Mendoza", "Pedro Torres")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") })
                OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") })

                DropDownSelector("Stop Type", stopTypes.map { it.type }, stopType?.type) { selectedType ->
                    stopType = stopTypes.find { it.type == selectedType }
                }
                DropDownSelector("Driver", drivers, driver) { driver = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.addStop(
                    name.text,
                    latitude.text,
                    longitude.text,
                    stopType!!,
                    driver ?: "Unknown"
                )
                onSave()
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
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryColor
                )
            ) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StopsAdminPreview() {
    StopsAdminScreen()
}