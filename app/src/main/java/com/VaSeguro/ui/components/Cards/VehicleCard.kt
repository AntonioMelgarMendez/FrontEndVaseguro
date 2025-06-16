package com.VaSeguro.ui.components.Cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.ui.components.Misc.TextRow
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleViewModel

@Composable
fun VehicleCard(
  vehicle: Vehicle,
  isExpanded: Boolean,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  shape: Shape = RectangleShape,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onToggleExpand: () -> Unit,
  viewModel: VehicleViewModel
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

          val driver = viewModel.getDriverForVehicle(vehicle.driver_id)
          TextRow("Driver", "${driver?.forename ?: "Desconocido"} ${driver?.surname ?: ""}")
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