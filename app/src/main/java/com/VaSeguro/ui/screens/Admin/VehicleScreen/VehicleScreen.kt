package com.VaSeguro.ui.screens.Admin.VehicleScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.model.Vehicle
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun VehicleScreen(viewModel: VehicleViewModel = viewModel()) {
  val vehicles = viewModel.vehicles.collectAsState().value
  val expandedMap = viewModel.expandedMap.collectAsState().value
  val checkedMap = viewModel.checkedMap.collectAsState().value
  var showDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ){
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ){
      Button(
        onClick = {  },
      ) {
        Text(text = "Filters")
      }
      Button(
        onClick = { showDialog = true },
      ) {
        Text(text = "Add +")
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
      items(vehicles) { vehicle ->
        VehicleCard(
          vehicle = vehicle,
          isExpanded = expandedMap[vehicle.id] ?: false,
          isChecked = checkedMap[vehicle.id] ?: false,
          onToggleExpand = { viewModel.toggleExpand(vehicle.id) },
          onCheckedChange = { viewModel.setChecked(vehicle.id, it) },
          onDeleteClick = { viewModel.deleteVehicle(vehicle.id) },
          onEditClick = { println("Editar ${vehicle.plate}") }
        )
      }
    }
  }

  if (showDialog) {
    AddVehicleDialog(
      onDismiss = { showDialog = false },
      onConfirm = { plate, model, driverName ->
        viewModel.addVehicle(plate, model, driverName)
        showDialog = false
      }
    )
  }
}

@Composable
fun VehicleCard(
  vehicle: Vehicle,
  isExpanded: Boolean,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onToggleExpand: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
          )
          Text(
            text = "ID ${vehicle.id}",
            color = Color(0xFF6C63FF),
            style = MaterialTheme.typography.labelSmall
          )
        }
        IconButton(onClick = onToggleExpand) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand/Collapse"
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = vehicle.plate,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      if (isExpanded) {
        Spacer(modifier = Modifier.height(8.dp))
        TextRow("Model", vehicle.model)
        TextRow("Driver", "${vehicle.driver_id.forename} ${vehicle.driver_id.surname}")
        TextRow("Created at", vehicle.created_at)

        Spacer(modifier = Modifier.height(8.dp))
        Row {
          IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
          }
          IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
          }
        }
      }
    }
  }
}

@Composable
fun TextRow(label: String, value: String) {
  Row {
    Text(text = "$label: ", fontWeight = FontWeight.Light)
    Text(text = value, fontWeight = FontWeight.Normal)
  }
}

@Composable
fun AddVehicleDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String) -> Unit
) {
  var plate by remember { mutableStateOf("") }
  var model by remember { mutableStateOf("") }
  var driverName by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Agregar Vehículo") },
    text = {
      Column {
        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it },
          label = { Text("Placa") }
        )
        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Modelo") }
        )
        OutlinedTextField(
          value = driverName,
          onValueChange = { driverName = it },
          label = { Text("Nombre del Conductor") }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (plate.isNotBlank() && model.isNotBlank() && driverName.isNotBlank()) {
            onConfirm(plate, model, driverName)
          }
        }
      ) {
        Text("Agregar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}


@Preview(showBackground = true)
@Composable
fun VehicleScreenPreview() {
  VehicleScreen()
}