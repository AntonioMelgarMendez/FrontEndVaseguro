package com.VaSeguro.ui.screens.Admin.Children

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.ui.components.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.CustomizableOutlinedTextField
import com.VaSeguro.ui.screens.Driver.Chat.ChatViewModel
import com.VaSeguro.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.collections.lastIndex


@Composable
fun ChildrenAdminScreen(
    viewModel: ChildrenAdminScreenViewModel = viewModel(factory = ChildrenAdminScreenViewModel.Factory)
) {
    val context = LocalContext.current
    val children = viewModel.children.collectAsState().value
    val expandedMap = viewModel.expandedMap.collectAsState().value
    val checkedMap = viewModel.checkedMap.collectAsState().value
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIdToDelete by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchUsersForRoles()
        viewModel.fetchAllChildren()
    }

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

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                itemsIndexed(children) { index, child ->
                    val isFirst = index == 0
                    val isLast = index == children.lastIndex

                    val shape = when {
                        isFirst && isLast -> RoundedCornerShape(16.dp)
                        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RectangleShape
                    }

                    AdminCardItem(
                        id = child.id.toString(),
                        title = child.fullName,
                        subtitle = "Age: ${child.age} | Parent: ${child.parent}",
                        details = listOf(
                            "Forenames" to child.forenames,
                            "Surnames" to child.surnames,
                            "Birth" to child.birth,
                            "Medical Info" to child.medicalInfo,
                            "Driver" to child.driver,
                            "Created" to child.createdAt
                        ),
                        isExpanded = expandedMap[child.id.toString()] ?: false,
                        isChecked = checkedMap[child.id.toString()] ?: false,
                        shape = shape,
                        onCheckedChange = { viewModel.setChecked(child.id.toString(), it) },
                        onEditClick = { println("Editar ${child.fullName}") },
                        onDeleteClick = {
                            selectedIdToDelete = child.id
                            showDeleteDialog = true
                        },
                        onToggleExpand = { viewModel.toggleExpand(child.id.toString()) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddChildDialog(
            onDismiss = { showDialog = false },
            onSave = { showDialog = false }
        )
    }

    if (showDeleteDialog && selectedIdToDelete != null) {
        ConfirmationDialog(
            message = "Are you sure you want to delete this item?",
            onConfirm = {
                viewModel.deleteChild(selectedIdToDelete!!)
                showDeleteDialog = false
                selectedIdToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedIdToDelete = null
            }
        )
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
    viewModel: ChildrenAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    var forenames by remember { mutableStateOf(TextFieldValue("")) }
    var surnames by remember { mutableStateOf(TextFieldValue("")) }
    var medicalInfo by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParent by remember { mutableStateOf<UserResponse?>(null) }
    var selectedDriver by remember { mutableStateOf<UserResponse?>(null) }

    val parentOptions = viewModel.parents
    val driverOptions = viewModel.drivers

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val selectedDateText = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
    var showDatePicker by remember { mutableStateOf(false) }

    fun resetForm() {
        forenames = TextFieldValue("")
        surnames = TextFieldValue("")
        medicalInfo = TextFieldValue("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Child") },
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
                        selectedDriver == null
                    ) {
                        Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addChild(
                        forenames = forenames.text,
                        surnames = surnames.text,
                        birth_date = selectedDateText,
                        medical_info = medicalInfo.text,
                        gender = "N/A",
                        parent_id = selectedParent!!.id,
                        driver_id = selectedDriver!!.id,
                    )

                    onSave()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) {
                Text("Agregar")
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

@Preview(showBackground = true)
@Composable
fun ChildrenAdminScreenPreview() {
    ChildrenAdminScreen()
}