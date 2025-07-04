package com.VaSeguro.ui.components

import android.R.attr.enabled
import android.R.attr.type
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, UserData) -> Unit,
  drivers: List<UserData>
) {
  val context = LocalContext.current
  val customColor = Color(0xFF6C63FF)
  var plate by remember { mutableStateOf("") }
  var model by remember { mutableStateOf("") }

  var selectedDriver by remember { mutableStateOf<UserData?>(null) }
  var expanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        .background(Color.White, RoundedCornerShape(16.dp))
        .padding(24.dp)
    ) {
      Column {
        Text("Agregar Vehículo", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it },
          label = { Text("Placa") },
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = customColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedLabelColor = customColor,
            unfocusedLabelColor = Color.Gray
          )
        )

        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Modelo") },
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = customColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedLabelColor = customColor,
            unfocusedLabelColor = Color.Gray
          )
        )

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded }
        ) {
          OutlinedTextField(
            value = selectedDriver?.let { "${it.forename} ${it.surname}" } ?: "",
            onValueChange = {},
            label = { Text("Conductor") },
            readOnly = true,
            trailingIcon = {
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedIndicatorColor = customColor,
              unfocusedIndicatorColor = Color.LightGray,
              focusedLabelColor = customColor,
              unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .clickable { expanded = true }
          )

          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            drivers.forEach { driver ->
              DropdownMenuItem(
                text = { Text("${driver.forename} ${driver.surname}") },
                onClick = {
                  selectedDriver = driver
                  expanded = false
                }
              )
            }
          }
        }

        Spacer(Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = {
              onDismiss()
            },
            border = BorderStroke(2.dp, PrimaryColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
          ) {
            Text("Cancelar")
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              if (plate.isBlank() || model.isBlank() || selectedDriver == null) {
                Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@Button
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = customColor,
              contentColor = Color.White
            )
          ) {
            Text("Agregar", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AddVehicleDialogPreview() {
  val exampleDrivers = listOf(
    UserData(
      id = "1",
      forename = "Ana",
      surname = "García",
      email = "ana.garcia@example.com",
      phoneNumber = "1234567890",
      profilePic = null,
      role_id = UserRole(1, "Conductor"),
      gender = "F"
    ),
    UserData(
      id = "2",
      forename = "Carlos",
      surname = "Mendoza",
      email = "carlos.mendoza@example.com",
      phoneNumber = "9876543210",
      profilePic = null,
      role_id = UserRole(1, "Conductor"),
      gender = "M"
    ),
    UserData(
      id = "3",
      forename = "Lucía",
      surname = "Pérez",
      email = "lucia.perez@example.com",
      phoneNumber = "5551234567",
      profilePic = null,
      role_id = UserRole(1, "Conductor"),
      gender = "F"
    )
  )

  AddVehicleDialog(
    onDismiss = {},
    onConfirm = { plate, model, driverName ->
      println("Placa: $plate, Modelo: $model, Conductor: $driverName")
    },
    drivers = exampleDrivers
  )
}
