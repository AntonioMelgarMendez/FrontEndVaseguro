package com.VaSeguro.ui.screens.Admin.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.ui.components.Container.ExpandableInfoCard
import com.VaSeguro.ui.components.Container.TopBar
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.theme.PrimaryColor


@Composable
fun UsersAdminScreen(
    viewModel: UsersAdminScreenViewModel = viewModel()
){
    var showDialog by remember { mutableStateOf(false) }
    val users by viewModel.users.collectAsState()
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopBar("Users")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add User") },
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
            items(users) { user ->
                ExpandableInfoCard(
                    id = user.id,
                    title = "${user.forename} ${user.surname}",
                    info = listOf(
                        "Forename" to user.forename,
                        "Surname" to user.surname,
                        "Email" to user.email,
                        "Phone Number" to user.phoneNumber,
                        "Gender" to (user.gender ?: "Not specified")
                    ),
                    onEdit = { /* Future edit */ },
                    onDelete = { showConfirmDialog = user.id }
                )
            }
        }

        if (showConfirmDialog != null) {
            ConfirmationDialog(
                message = "Are you sure you want to delete this user?",
                onConfirm = {
                    viewModel.deleteUser(showConfirmDialog!!)
                    showConfirmDialog = null
                },
                onDismiss = { showConfirmDialog = null }
            )
        }

        if (showDialog) {
            AddUserDialog(
                onDismiss = { showDialog = false },
                onSave = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    viewModel: UsersAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var forename by remember { mutableStateOf(TextFieldValue("")) }
    var surname by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    val genderOptions = listOf("Male", "Female", "Other")
    var gender by remember { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = forename, onValueChange = { forename = it }, label = { Text("Forename") })
                OutlinedTextField(value = surname, onValueChange = { surname = it }, label = { Text("Surname") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })

                DropDownSelector(
                    label = "Gender",
                    options = genderOptions,
                    selectedOption = gender,
                    onOptionSelected = { gender = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.addUser(
                    forename.text,
                    surname.text,
                    email.text,
                    phone.text,
                    gender ?: "Not specified"
                )
                onSave()
            },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) { Text("Save") }
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
}

@Preview(showBackground = true)
@Composable
fun UserAdminScreenPreview() {
    UsersAdminScreen()
}