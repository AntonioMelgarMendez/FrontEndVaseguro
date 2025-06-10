package com.VaSeguro.ui.screens.Admin.VehicleScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun VehicleScreen(viewModel: VehicleViewModel = viewModel()) {
  val vehicles = viewModel.vehicles.collectAsState().value
  val expandedMap = viewModel.expandedMap.collectAsState().value
  val checkedMap = viewModel.checkedMap.collectAsState().value
  var showDialog by remember { mutableStateOf(false) }

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
      .padding(16.dp)
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

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn {
        itemsIndexed(vehicles) { index, vehicle ->
          val isFirst = index == 0
          val isLast = index == vehicles.lastIndex

          val cardShape = when {
            isFirst && isLast -> RoundedCornerShape(16.dp)
            isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            else -> RectangleShape
          }

          VehicleCard(
            vehicle = vehicle,
            isExpanded = expandedMap[vehicle.id] ?: false,
            isChecked = checkedMap[vehicle.id] ?: false,
            onToggleExpand = { viewModel.toggleExpand(vehicle.id) },
            onCheckedChange = { viewModel.setChecked(vehicle.id, it) },
            onDeleteClick = { viewModel.deleteVehicle(vehicle.id) },
            onEditClick = { println("Editar ${vehicle.plate}") },
            shape = cardShape
          )
        }
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
  shape: Shape = RectangleShape,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onToggleExpand: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth(),
    shape = shape,
    border = BorderStroke(1.dp, Color.LightGray),
  ) {
    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Checkbox(
        checked = isChecked,
        onCheckedChange = onCheckedChange
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
      ) {
        Text(
          text = "ID ${vehicle.id}",
          color = Color(0xFF6C63FF),
          style = MaterialTheme.typography.labelSmall
        )
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
          Spacer(modifier = Modifier.height(4.dp))
          Row {
            Text(
              text = "Actions: ",
              fontWeight = FontWeight.Light,
              modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = onEditClick) {
              Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDeleteClick) {
              Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
          }
        }
      }

      IconButton(onClick = onToggleExpand) {
        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = "Expand/Collapse"
        )
      }
    }
  }
}

@Composable
fun TextRow(label: String, value: String) {
  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ){
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