package com.VaSeguro.ui.screens.Admin.Vehicle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.ui.components.Cards.AdminCardItem
import com.VaSeguro.ui.theme.PrimaryColor


@Composable
fun VehicleScreen() {
  val context = LocalContext.current
  val viewModel: VehicleViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appProvider = AppProvider(context.applicationContext)
        return VehicleViewModel(
          appProvider.provideVehicleRepository(),
          appProvider.provideUserPreferences(),
            appProvider.provideVehicleDao()
        ) as T
      }
    }
  )

  val vehicles by viewModel.vehicles.collectAsState()
  val expandedMap by viewModel.expandedMap.collectAsState()
  val checkedMap by viewModel.checkedMap.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  var showEditDialog by remember { mutableStateOf(false) }
  var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
  // Filter dialog state
  var showFilterDialog by remember { mutableStateOf(false) }
  var filterText by remember { mutableStateOf(TextFieldValue("")) }
  var sortOption by remember { mutableStateOf("Name") }
  var sortAscending by remember { mutableStateOf(true) }

  // Filtering and sorting logic for vehicles
  val filteredVehicles = vehicles
    .filter {
      filterText.text.isBlank() ||
              it.plate.contains(filterText.text, true) ||
              it.model.contains(filterText.text, true) ||
              it.brand.contains(filterText.text, true)
    }
    .let { list ->
      when (sortOption) {
        "Name" -> if (sortAscending) list.sortedBy { it.plate } else list.sortedByDescending { it.plate }
        "Model" -> if (sortAscending) list.sortedBy { it.model } else list.sortedByDescending { it.model }
        else -> list
      }
    }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .border(
        width = 1.dp,
        color = Color.LightGray,
        shape = RoundedCornerShape(16.dp)
      )
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White)
      .padding(16.dp),
    contentAlignment = Alignment.TopCenter
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
          onClick = { showFilterDialog = true },
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

      Spacer(modifier = Modifier.height(16.dp))

      if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else {
        LazyColumn {
          itemsIndexed(filteredVehicles) { index, vehicle ->
            val isFirst = index == 0
            val isLast = index == filteredVehicles.lastIndex

            val cardShape = when {
              isFirst && isLast -> RoundedCornerShape(16.dp)
              isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
              isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
              else -> RectangleShape
            }

            AdminCardItem(
              id = vehicle.id,
              title = vehicle.plate,
              subtitle = "Modelo: ${vehicle.model} | Conductor: ${vehicle.driver_id}",
              details = listOf(
                "Marca" to vehicle.brand,
                "Modelo" to vehicle.model,
                "Año" to vehicle.year.toString(),
                "Color" to vehicle.color,
                "Capacidad" to vehicle.capacity.toString()
              ),
              isExpanded = expandedMap[vehicle.id] ?: false,
              isChecked = checkedMap[vehicle.id] ?: false,
              shape = cardShape,
              onCheckedChange = { viewModel.setChecked(vehicle.id, it) },
              onEditClick = {
                vehicleToEdit = vehicle
                showEditDialog = true
              },
              onDeleteClick = { viewModel.deleteVehicle(vehicle.id) },
              onToggleExpand = { viewModel.toggleExpand(vehicle.id) }
            )
          }
        }
      }
    }
  }
  if (showEditDialog && vehicleToEdit != null) {
    EditVehicleDialog(
      vehicle = vehicleToEdit!!,
      onDismiss = { showEditDialog = false },
      onSave = { updated ->
        viewModel.updateVehicle(
          id = updated.id.toInt(),
          plate = updated.plate,
          model = updated.model,
          brand = updated.brand,
          year = updated.year.toString(),
          color = updated.color,
          capacity = updated.capacity.toString(),
          carPic = null // or your image part
        ) { success, error ->
          // Optionally show a message
          showEditDialog = false
        }
      }
    )
  }

  if (showFilterDialog) {
    VehicleFilterDialog(
      filterText = filterText,
      onFilterTextChange = { filterText = it },
      sortOption = sortOption,
      onSortOptionChange = { sortOption = it },
      sortAscending = sortAscending,
      onSortOrderChange = { sortAscending = it },
      onDismiss = { showFilterDialog = false },
      onClear = {
        filterText = TextFieldValue("")
        sortOption = "Name"
        sortAscending = true
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFilterDialog(
  filterText: TextFieldValue,
  onFilterTextChange: (TextFieldValue) -> Unit,
  sortOption: String,
  onSortOptionChange: (String) -> Unit,
  sortAscending: Boolean,
  onSortOrderChange: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  onClear: () -> Unit
) {
  val sortOptions = listOf("Name", "Model")
  var sortMenuExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Filter & Sort Vehicles") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = filterText,
          onValueChange = onFilterTextChange,
          label = { Text("Search by plate, model or brand") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        // Sort options
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Sort by:")
          Spacer(Modifier.width(8.dp))
          Box {
            OutlinedButton(onClick = { sortMenuExpanded = true }) {
              Text(sortOption)
            }
            DropdownMenu(
              expanded = sortMenuExpanded,
              onDismissRequest = { sortMenuExpanded = false }
            ) {
              sortOptions.forEach { option ->
                DropdownMenuItem(
                  text = { Text(option) },
                  onClick = {
                    onSortOptionChange(option)
                    sortMenuExpanded = false
                  }
                )
              }
            }
          }
          Spacer(Modifier.width(8.dp))
          IconButton(onClick = { onSortOrderChange(!sortAscending) }) {
            Icon(
              imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
              contentDescription = if (sortAscending) "Ascending" else "Descending"
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          contentColor = Color.White,
          containerColor = PrimaryColor
        )
      ) { Text("Apply") }
    },
    dismissButton = {
      Row {
        OutlinedButton(
          onClick = {
            onClear()
            onDismiss()
          },
          border = BorderStroke(2.dp, PrimaryColor),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryColor
          )
        ) { Text("Clear") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
          onClick = onDismiss,
          border = BorderStroke(2.dp, PrimaryColor),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryColor
          )
        ) { Text("Cancel") }
      }
    }
  )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleDialog(
  vehicle: Vehicle,
  onDismiss: () -> Unit,
  onSave: (Vehicle) -> Unit
) {
  var plate by remember { mutableStateOf(vehicle.plate) }
  var model by remember { mutableStateOf(vehicle.model) }
  var brand by remember { mutableStateOf(vehicle.brand) }
  var year by remember { mutableStateOf(vehicle.year.toString()) }
  var color by remember { mutableStateOf(vehicle.color) }
  var capacity by remember { mutableStateOf(vehicle.capacity.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Vehicle") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it },
          label = { Text("Plate") },
          singleLine = true
        )
        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Model") },
          singleLine = true
        )
        OutlinedTextField(
          value = brand,
          onValueChange = { brand = it },
          label = { Text("Brand") },
          singleLine = true
        )
        OutlinedTextField(
          value = year,
          onValueChange = { year = it },
          label = { Text("Year") },
          singleLine = true
        )
        OutlinedTextField(
          value = color,
          onValueChange = { color = it },
          label = { Text("Color") },
          singleLine = true
        )
        OutlinedTextField(
          value = capacity,
          onValueChange = { capacity = it },
          label = { Text("Capacity") },
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            vehicle.copy(
              plate = plate,
              model = model,
              brand = brand,
              year = (year.toIntOrNull() ?: vehicle.year).toString(),
              color = color,
              capacity = (capacity.toIntOrNull() ?: vehicle.capacity).toString()
            )
          )
        }
      ) { Text("Save") }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Preview(showBackground = true)
@Composable
fun VehicleScreenPreview() {
  VehicleScreen()
}