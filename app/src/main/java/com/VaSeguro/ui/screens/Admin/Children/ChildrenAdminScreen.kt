package com.VaSeguro.ui.screens.Admin.Children


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.ui.components.Container.DropdownSelector
import com.VaSeguro.ui.components.Container.ExpandableChildCard


@Composable
fun ChildrenAdminScreen(
    viewModel: ChildrenAdminScreenViewModel = viewModel()
) {
    val children by viewModel.children.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val expandedItem = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add") },
                onClick = { showDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)) {
            items(children) { child ->
                ExpandableChildCard(
                    child = child,
                    isExpanded = child.id == expandedItem.value,
                    onExpandToggle = {
                        expandedItem.value = if (expandedItem.value == child.id) null else child.id
                    },
                    onEdit = { /* future feature */ },
                    onDelete = { viewModel.deleteChild(child.id) }
                )
            }
        }
    }

    if (showDialog) {
        AddChildDialog(
            onDismiss = { showDialog = false },
            onSave = { showDialog = false }
        )
    }
}

@Composable
fun AddChildDialog(
    viewModel: ChildrenAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var forenames by remember { mutableStateOf(TextFieldValue("")) }
    var surnames by remember { mutableStateOf(TextFieldValue("")) }
    var birth by remember { mutableStateOf(TextFieldValue("")) }
    var medicalInfo by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParent by remember { mutableStateOf<String?>(null) }
    var selectedDriver by remember { mutableStateOf<String?>(null) }

    val parents = listOf("Carlos Portillo", "Laura Gómez")
    val drivers = listOf("Juan Mendoza", "Pedro Torres")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Child") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = forenames, onValueChange = { forenames = it }, label = { Text("Forenames") })
                OutlinedTextField(value = surnames, onValueChange = { surnames = it }, label = { Text("Surnames") })
                OutlinedTextField(value = birth, onValueChange = { birth = it }, label = { Text("Birth") })
                OutlinedTextField(value = medicalInfo, onValueChange = { medicalInfo = it }, label = { Text("Medical Info") })

                DropdownSelector("Parent", parents, selectedParent) { selectedParent = it }
                DropdownSelector("Driver", drivers, selectedDriver) { selectedDriver = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.addChild(
                    forenames.text,
                    surnames.text,
                    birth.text,
                    medicalInfo.text,
                    selectedParent ?: "",
                    selectedDriver ?: ""
                )
                onSave()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChildrenAdminScreenPreview() {
    ChildrenAdminScreen()
}