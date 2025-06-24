package com.VaSeguro.ui.screens.Admin.Users

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.ui.components.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.CustomizableOutlinedTextField
import com.VaSeguro.ui.theme.PrimaryColor

@Composable
fun UsersAdminScreen() {
    val context = LocalContext.current
    val viewModel: UsersAdminScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return UsersAdminScreenViewModel(
                    appProvider.provideAuthRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )

    LaunchedEffect(Unit) { viewModel.fetchAllUsers() }

    val users by viewModel.users.collectAsState()
    val expandedMap by viewModel.expandedMap.collectAsState()
    val checkedMap by viewModel.checkedMap.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIdToDelete by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserData?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filter dialog state
    var filterText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf("Name") }
    var sortAscending by remember { mutableStateOf(true) }

    // Filtering and sorting logic
    val filteredUsers = users
        .filter {
            (filterText.text.isBlank() ||
                    it.forename.contains(filterText.text, true) ||
                    it.surname.contains(filterText.text, true) ||
                    it.email.contains(filterText.text, true))
                    && (selectedGender == null || it.gender == selectedGender)
        }
        .let { list ->
            when (sortOption) {
                "Name" -> if (sortAscending) list.sortedBy { it.forename + it.surname }
                else list.sortedByDescending { it.forename + it.surname }
                "Email" -> if (sortAscending) list.sortedBy { it.email }
                else list.sortedByDescending { it.email }
                else -> list
            }
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
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
            // Filter/Add bar always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = { showFilterDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Filter")
                        Spacer(Modifier.width(8.dp))
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
                        Text("Add")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            // User list or loading spinner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = PrimaryColor
                    )
                } else {
                    LazyColumn {
                        itemsIndexed(filteredUsers) { index, user ->
                            val isFirst = index == 0
                            val isLast = index == filteredUsers.lastIndex
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
                                onEditClick = {
                                    userToEdit = user
                                    showEditDialog = true
                                },
                                shape = shape
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddUserDialog(
            viewModel = viewModel,
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
    if (showEditDialog && userToEdit != null) {
        EditUserDialog(
            user = userToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                viewModel.updateUser(
                    userId = updatedUser.id,
                    forename = updatedUser.forename,
                    surname = updatedUser.surname,
                    email = updatedUser.email,
                    phoneNumber = updatedUser.phoneNumber,
                    gender = updatedUser.gender ?: ""
                )
                showEditDialog = false
            }
        )
    }
    if (showFilterDialog) {
        FilterDialog(
            filterText = filterText,
            onFilterTextChange = { filterText = it },
            selectedGender = selectedGender,
            onGenderChange = { selectedGender = it },
            sortOption = sortOption,
            onSortOptionChange = { sortOption = it },
            sortAscending = sortAscending,
            onSortOrderChange = { sortAscending = it },
            onDismiss = { showFilterDialog = false },
            onClear = {
                filterText = TextFieldValue("")
                selectedGender = null
                sortOption = "Name"
                sortAscending = true
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    viewModel: UsersAdminScreenViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    var forename by remember { mutableStateOf(TextFieldValue("")) }
    var surname by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    val genderOptions = listOf("Male", "Female", "Other")
    var gender by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        forename = TextFieldValue("")
        surname = TextFieldValue("")
        email = TextFieldValue("")
        phone = TextFieldValue("")
        password = TextFieldValue("")
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
                CustomizableOutlinedTextField(value = password, onValueChange = { password = it }, label = "Password")
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
                        password.text.isBlank() ||
                        gender.isNullOrBlank()
                    ) {
                        Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addUser(
                        forename = forename.text,
                        surname = surname.text,
                        email = email.text,
                        password = password.text,
                        phoneNumber = phone.text,
                        gender = gender ?: "M",
                        roleId = 3,
                        profilePic = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: UserData,
    onDismiss: () -> Unit,
    onSave: (UserData) -> Unit
) {
    var forename by remember { mutableStateOf(TextFieldValue(user.forename)) }
    var surname by remember { mutableStateOf(TextFieldValue(user.surname)) }
    var email by remember { mutableStateOf(TextFieldValue(user.email)) }
    var phone by remember { mutableStateOf(TextFieldValue(user.phoneNumber)) }
    val genderOptions = listOf("Male", "Female", "Other")
    var gender by remember { mutableStateOf(user.gender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
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
                    onSave(
                        user.copy(
                            forename = forename.text,
                            surname = surname.text,
                            email = email.text,
                            phoneNumber = phone.text,
                            gender = gender
                        )
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filterText: TextFieldValue,
    onFilterTextChange: (TextFieldValue) -> Unit,
    selectedGender: String?,
    onGenderChange: (String?) -> Unit,
    sortOption: String,
    onSortOptionChange: (String) -> Unit,
    sortAscending: Boolean,
    onSortOrderChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    val genderOptions = listOf("All", "Male", "Female", "Other")
    val sortOptions = listOf("Name", "Email")
    var genderMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort Users") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = filterText,
                    onValueChange = onFilterTextChange,
                    label = { Text("Search by name, surname or email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Gender filter
                Box {
                    OutlinedButton(
                        onClick = { genderMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedGender ?: "All")
                    }
                    DropdownMenu(
                        expanded = genderMenuExpanded,
                        onDismissRequest = { genderMenuExpanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onGenderChange(if (option == "All") null else option)
                                    genderMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                // Sort options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sort by:")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuBox(
                        options = sortOptions,
                        selectedOption = sortOption,
                        onOptionSelected = onSortOptionChange
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onSortOrderChange(!sortAscending) }) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortAscending) "Ascending" else "Descending"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) { Text("Apply") }
        },
        dismissButton = {
            Row {
                OutlinedButton(
                    onClick = {
                        onClear()
                        onDismiss()
                    },
                    border = BorderStroke(2.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) { Text("Clear") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(2.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}