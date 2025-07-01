package com.VaSeguro.ui.components.AddDialogues

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.Misc.CustomizableOutlinedTextField
import com.VaSeguro.ui.screens.Admin.Children.ChildrenAdminScreenViewModel
import com.VaSeguro.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialogAdmin(
  viewModel: ChildrenAdminScreenViewModel = viewModel(factory = ChildrenAdminScreenViewModel.Factory),
  onDismiss: () -> Unit,
  onSave: () -> Unit,
  existingChild: Children? = null
) {
  val context = LocalContext.current
  var forenames by remember { mutableStateOf(TextFieldValue(existingChild?.forenames ?: "")) }
  var surnames by remember { mutableStateOf(TextFieldValue(existingChild?.surnames ?: "")) }
  var medicalInfo by remember { mutableStateOf(TextFieldValue(existingChild?.medical_info ?: "")) }

  val parentOptions = viewModel.parents
  val driverOptions = viewModel.drivers
  val backendFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  val genderMap = mapOf("Masculino" to "M", "Femenino" to "F")

  var selectedParent by remember {
    mutableStateOf(
      existingChild?.let { child ->
        parentOptions.find { it.id == child.parent_id }
      }
    )
  }

  var selectedDriver by remember {
    mutableStateOf(
      existingChild?.let { child ->
        driverOptions.find { it.id == child.driver_id }
      }
    )
  }

  var selectedGenderLabel by remember {
    mutableStateOf(
      existingChild?.let { child ->
        genderMap.entries.find { it.value == child.gender }?.key
      }
    )
  }

  val displayFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
  var selectedDateMillis by remember {
    mutableStateOf(
      existingChild?.birth_date?.let { dateStr ->
        try {
          backendFormatter.parse(dateStr)?.time
        } catch (e: Exception) {
          null
        }
      }
    )
  }

  val selectedDateText = selectedDateMillis?.let { displayFormatter.format(Date(it)) } ?: ""
  var showDatePicker by remember { mutableStateOf(false) }

  fun resetForm() {
    forenames = TextFieldValue("")
    surnames = TextFieldValue("")
    medicalInfo = TextFieldValue("")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (existingChild != null) "Editar niño" else "Agregar niño")
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        CustomizableOutlinedTextField(forenames, { forenames = it }, "Forenames")
        CustomizableOutlinedTextField(surnames, { surnames = it }, "Surnames")

        OutlinedTextField(
          value = selectedDateText,
          onValueChange = {},
          label = { Text("Birth") },
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
              Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
            }
          },
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = PrimaryColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = Color.Gray
          )
        )

        DropDownSelector(
          label = "Género",
          options = genderMap.keys.toList(),
          selectedOption = selectedGenderLabel,
          onOptionSelected = { selectedGenderLabel = it }
        )

        CustomizableOutlinedTextField(medicalInfo, { medicalInfo = it }, "Medical Info")

        DropDownSelector(
          label = "Parent",
          options = parentOptions.map { "${it.forenames} ${it.surnames}" },
          selectedOption = selectedParent?.let { "${it.forenames} ${it.surnames}" },
          onOptionSelected = { name ->
            selectedParent = parentOptions.find { "${it.forenames} ${it.surnames}" == name }
          }
        )

        DropDownSelector(
          label = "Driver",
          options = driverOptions.map { "${it.forenames} ${it.surnames}" },
          selectedOption = selectedDriver?.let { "${it.forenames} ${it.surnames}" },
          onOptionSelected = { name ->
            selectedDriver = driverOptions.find { "${it.forenames} ${it.surnames}" == name }
          }
        )

      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (
            forenames.text.isBlank() ||
            surnames.text.isBlank() ||
            selectedDateText.isBlank() ||
            medicalInfo.text.isBlank() ||
            selectedParent == null ||
            selectedDriver == null ||
            selectedGenderLabel == null
          ) {
            Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
            return@Button
          }

          val updatedChild = Children(
            id = existingChild?.id ?: 0, // ← si es nuevo, id = 0 (aunque este bloque solo debería ejecutarse si es edición)
            forenames = forenames.text,
            surnames = surnames.text,
            birth_date = selectedDateText,
            medical_info = medicalInfo.text,
            gender = genderMap[selectedGenderLabel]!!,
            parent_id = selectedParent!!.id,
            driver_id = selectedDriver!!.id,
            profile_pic = existingChild?.profile_pic // opcional
          )

          if (existingChild != null) {
            viewModel.updateChild(
              id = existingChild.id.toString(),
              child = updatedChild
            )
          } else {
            viewModel.addChild(
              forenames = forenames.text,
              surnames = surnames.text,
              birth_date = selectedDateText,
              medical_info = medicalInfo.text,
              gender = genderMap[selectedGenderLabel]!!,
              parent_id = selectedParent!!.id,
              driver_id = selectedDriver!!.id
            )
          }

          onSave()
        },
        colors = ButtonDefaults.buttonColors(
          contentColor = Color.White,
          containerColor = PrimaryColor
        )
      ) {
        Text(if (existingChild != null) "Guardar" else "Agregar")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = {
          resetForm()
          onDismiss()
        },
        border = BorderStroke(2.dp, PrimaryColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
      ) {
        Text("Cancelar")
      }
    }
  )

  if (showDatePicker) {
    val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = state.selectedDateMillis
          showDatePicker = false
        }) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = state, showModeToggle = false)
    }
  }
}