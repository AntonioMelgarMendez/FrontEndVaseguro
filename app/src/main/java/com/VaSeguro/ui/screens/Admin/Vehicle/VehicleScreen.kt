package com.VaSeguro.ui.screens.Admin.Vehicle

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import com.VaSeguro.ui.components.AddVehicleDialog
import com.VaSeguro.ui.components.VehicleCard

@Composable
fun VehicleScreen(viewModel: VehicleViewModel = viewModel()) {
  val vehicles = viewModel.vehicles.collectAsState().value
  val drivers = viewModel.drivers.collectAsState().value
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
      onConfirm = { plate, model, driver ->
        viewModel.addVehicle(plate, model, driver)
        showDialog = false
      },
      drivers = drivers,
    )
  }
}

@Preview(showBackground = true)
@Composable
fun VehicleScreenPreview() {
  VehicleScreen()
}