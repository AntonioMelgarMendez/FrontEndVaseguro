package com.VaSeguro.ui.components.AddDialogues

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
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole

fun getCurrentDateTime(): String {
  val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  return java.time.LocalDateTime.now().format(formatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
  onDismiss: () -> Unit,
  onConfirm: (Child) -> Unit,
  drivers: List<UserData>,
  parents: List<UserData>,
  existingChild: Child? = null
) {
  val customColor = Color(0xFF6C63FF)

  var forenames by remember { mutableStateOf(existingChild?.forenames ?: "") }
  var surnames by remember { mutableStateOf(existingChild?.surnames ?: "") }
  var birth by remember { mutableStateOf(existingChild?.birth ?: "") }
  var age by remember { mutableStateOf(existingChild?.age?.toString() ?: "") }
  var medicalInfo by remember { mutableStateOf(existingChild?.medicalInfo ?: "") }

  var selectedDriver by remember { mutableStateOf(
    drivers.find { it.id == existingChild?.driver }
  ) }
  var selectedParent by remember { mutableStateOf(
    parents.find { it.id == existingChild?.parent }
  ) }

  var expandedDriver by remember { mutableStateOf(false) }
  var expandedParent by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        .background(Color.White, RoundedCornerShape(16.dp))
        .padding(24.dp)
    ) {
      Column {
        Text(
          text = if (existingChild == null) "Agregar Niño" else "Editar Niño",
          style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
          value = forenames,
          onValueChange = { forenames = it },
          label = { Text("Nombres") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = surnames,
          onValueChange = { surnames = it },
          label = { Text("Apellidos") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = birth,
          onValueChange = { birth = it },
          label = { Text("Fecha de nacimiento (yyyy-MM-dd)") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = age,
          onValueChange = { age = it },
          label = { Text("Edad") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = medicalInfo,
          onValueChange = { medicalInfo = it },
          label = { Text("Info médica") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        ExposedDropdownMenuBox(
          expanded = expandedDriver,
          onExpandedChange = { expandedDriver = !expandedDriver }
        ) {
          OutlinedTextField(
            value = selectedDriver?.let { "${it.forename} ${it.surname}" } ?: "",
            onValueChange = {},
            label = { Text("Conductor") },
            readOnly = true,
            trailingIcon = {
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            colors = textFieldColors(customColor),
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .clickable { expandedDriver = true }
          )

          ExposedDropdownMenu(
            expanded = expandedDriver,
            onDismissRequest = { expandedDriver = false }
          ) {
            drivers.forEach { driver ->
              DropdownMenuItem(
                text = { Text("${driver.forename} ${driver.surname}") },
                onClick = {
                  selectedDriver = driver
                  expandedDriver = false
                }
              )
            }
          }
        }

        ExposedDropdownMenuBox(
          expanded = expandedParent,
          onExpandedChange = { expandedParent = !expandedParent }
        ) {
          OutlinedTextField(
            value = selectedParent?.let { "${it.forename} ${it.surname}" } ?: "",
            onValueChange = {},
            label = { Text("Encargado") },
            readOnly = true,
            trailingIcon = {
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            colors = textFieldColors(customColor),
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .clickable { expandedParent = true }
          )

          ExposedDropdownMenu(
            expanded = expandedParent,
            onDismissRequest = { expandedParent = false }
          ) {
            parents.forEach { parent ->
              DropdownMenuItem(
                text = { Text("${parent.forename} ${parent.surname}") },
                onClick = {
                  selectedParent = parent
                  expandedParent = false
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
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(contentColor = customColor)
          ) {
            Text("Cancelar", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              if (
                forenames.isNotBlank() && surnames.isNotBlank() && birth.isNotBlank() &&
                age.isNotBlank() && selectedDriver != null && selectedParent != null
              ) {
                val newChild = Child(
                  id = existingChild?.id ?: (10000..99999).random().toString(),
                  fullName = "$forenames $surnames",
                  surnames = surnames,
                  forenames = forenames,
                  birth = birth,
                  age = age.toIntOrNull() ?: 0,
                  driver = selectedDriver?.id ?: "",
                  parent = selectedParent?.id ?: "",
                  medicalInfo = medicalInfo,
                  createdAt = existingChild?.createdAt ?: getCurrentDateTime(),
                  profilePic = existingChild?.profilePic
                )
                onConfirm(newChild)
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = customColor,
              contentColor = Color.White
            )
          ) {
            Text(
              text = if (existingChild == null) "Agregar" else "Guardar cambios",
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

@Composable
private fun textFieldColors(customColor: Color) = TextFieldDefaults.colors(
  focusedContainerColor = Color.Transparent,
  unfocusedContainerColor = Color.Transparent,
  focusedIndicatorColor = customColor,
  unfocusedIndicatorColor = Color.LightGray,
  focusedLabelColor = customColor,
  unfocusedLabelColor = Color.Gray
)
