package com.VaSeguro.ui.screens.Admin.Users

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.ui.components.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.CustomizableOutlinedTextField
import com.VaSeguro.ui.theme.PrimaryColor


@Composable
fun UsersAdminScreen(
    viewModel: UsersAdminScreenViewModel = viewModel()
){
    val users by viewModel.users.collectAsState()
    val expandedMap by viewModel.expandedMap.collectAsState()
    val checkedMap by viewModel.checkedMap.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIdToDelete by remember { mutableStateOf<String?>(null) }

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
                itemsIndexed(users) { index, user ->
                    val isFirst = index == 0
                    val isLast = index == users.lastIndex

                    val shape = when {
                        isFirst && isLast -> RoundedCornerShape(16.dp)
                        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RectangleShape
                    }

                    AdminCardItem(
                        id = user.id,
                        title = "${user.forename} ${user.surname}",
                        subtitle = "Phone: ${user.phoneNumber} | Gender: ${user.gender ?: "N/A"}",
                        details = listOf(
                            "Email" to user.email,
                            "Gender" to (user.gender ?: "N/A"),
                            "Phone" to user.phoneNumber
                        ),
                        isExpanded = expandedMap[user.id] ?: false,
                        isChecked = checkedMap[user.id] ?: false,
                        onToggleExpand = { viewModel.toggleExpand(user.id) },
                        onCheckedChange = { viewModel.setChecked(user.id, it) },
                        onDeleteClick = {
                            selectedIdToDelete = user.id
                            showDeleteDialog = true
                        },
                        onEditClick = { println("Editar ${user.forename}") },
                        shape = shape
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddUserDialog(
            onDismiss = { showDialog = false },
            onSave = { showDialog = false }
        )
    }

    if (showDeleteDialog && selectedIdToDelete != null) {
        ConfirmationDialog(
            message = "Are you sure you want to delete this item?",
            onConfirm = {
                viewModel.deleteUser(selectedIdToDelete!!)
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
fun AddUserDialog(
    viewModel: UsersAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    var forename by remember { mutableStateOf(TextFieldValue("")) }
    var surname by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    val genderOptions = listOf("Male", "Female", "Other")
    var gender by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        forename = TextFieldValue("")
        surname = TextFieldValue("")
        email = TextFieldValue("")
        phone = TextFieldValue("")
        gender = null
    }

    AlertDialog(
        onDismissRequest = {
            resetForm()
            onDismiss()
        },
        title = { Text("Add User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                CustomizableOutlinedTextField(value = forename, onValueChange = { forename = it }, label = "Forename")
                CustomizableOutlinedTextField(value = surname, onValueChange = { surname = it }, label = "Surname")
                CustomizableOutlinedTextField(value = email, onValueChange = { email = it }, label = "Email")
                CustomizableOutlinedTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number")

                DropDownSelector(
                    label = "Gender",
                    options = genderOptions,
                    selectedOption = gender,
                    onOptionSelected = { gender = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (
                        forename.text.isBlank() ||
                        surname.text.isBlank() ||
                        email.text.isBlank() ||
                        phone.text.isBlank() ||
                        gender.isNullOrBlank()
                    ) {
                        Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addUser(
                        forename.text,
                        surname.text,
                        email.text,
                        phone.text,
                        gender ?: "Not specified"
                    )
                    resetForm()
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
                onClick = {
                    resetForm()
                    onDismiss()
                },
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