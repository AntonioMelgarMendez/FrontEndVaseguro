package com.VaSeguro.ui.screens.Admin.Children

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.ui.components.Container.TopBar
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.Container.ExpandableInfoCard
import com.VaSeguro.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ChildrenAdminScreen(
    viewModel: ChildrenAdminScreenViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val children by viewModel.children.collectAsState()
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopBar("Children")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add") },
                onClick = { showDialog = true },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(children) { child ->
                ExpandableInfoCard(
                    id = child.id,
                    title = child.fullName,
                    info = listOf(
                        "Forenames" to child.forenames,
                        "Surnames" to child.surnames,
                        "Birth" to child.birth,
                        "Age" to child.age.toString(),
                        "Driver" to child.driver,
                        "Parent" to child.parent,
                        "Medical information" to child.medicalInfo,
                        "Created at" to child.createdAt
                    ),
                    onEdit = { /* Acción futura */ },
                    onDelete = {
                        showConfirmDialog = child.id
                    }
                )
            }
        }

        if (showConfirmDialog != null) {
            ConfirmationDialog(
                message = "Are you sure you want to delete this child?",
                onConfirm = {
                    viewModel.deleteChild(showConfirmDialog!!)
                    showConfirmDialog = null
                },
                onDismiss = { showConfirmDialog = null }
            )
        }

        if (showDialog) {
            AddChildDialog(
                onDismiss = { showDialog = false },
                onSave = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
    viewModel: ChildrenAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var forenames by remember { mutableStateOf(TextFieldValue("")) }
    var surnames by remember { mutableStateOf(TextFieldValue("")) }
    var medicalInfo by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParent by remember { mutableStateOf<String?>(null) }
    var selectedDriver by remember { mutableStateOf<String?>(null) }

    val parents = listOf("Carlos Portillo", "Laura Gómez")
    val drivers = listOf("Juan Mendoza", "Pedro Torres")

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val selectedDateText = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Child") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = forenames, onValueChange = { forenames = it }, label = { Text("Forenames") })
                OutlinedTextField(value = surnames, onValueChange = { surnames = it }, label = { Text("Surnames") })

                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    label = { Text("Birth") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                        }
                    }
                )

                OutlinedTextField(value = medicalInfo, onValueChange = { medicalInfo = it }, label = { Text("Medical Info") })
                DropDownSelector("Parent", parents, selectedParent) { selectedParent = it }
                DropDownSelector("Driver", drivers, selectedDriver) { selectedDriver = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.addChild(
                    forenames.text,
                    surnames.text,
                    selectedDateText,
                    medicalInfo.text,
                    selectedParent ?: "",
                    selectedDriver ?: ""
                )
                onSave()
            },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            )
            { Text("Save") }
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

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

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
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = state,
                showModeToggle = false
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ChildrenAdminScreenPreview() {
    ChildrenAdminScreen()
}