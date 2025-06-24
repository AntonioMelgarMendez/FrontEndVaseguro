package com.VaSeguro.ui.screens.Parents.Configuration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Security.SecurityFormState
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Forms.ValidationChecklist
import com.VaSeguro.ui.theme.PrimaryColor
import com.VaSeguro.ui.theme.SecondaryColor

@Composable
fun SecurityFormSection(
    state: SecurityFormState,
    original: SecurityFormState,
    onValueChange: ((SecurityFormState) -> SecurityFormState) -> Unit,
    onUpdate: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    updateSuccess: Boolean?,
    onDismissSuccess: () -> Unit
) {
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val hasChanges = state != original
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmationDialog(
            title = "Confirm Update",
            message = "Are you sure you want to update this information?",
            onConfirm = {
                showDialog = false
                onUpdate()
            },
            onDismiss = { showDialog = false }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (updateSuccess == true) {
        AlertDialog(
            onDismissRequest = onDismissSuccess,
            title = { Text("Hecho") },
            text = { Text("Contraseña cambiada") },
            confirmButton = {
                TextButton(onClick = onDismissSuccess) {
                    Text("OK")
                }
            }
        )
    } else if (updateSuccess == false) {
        AlertDialog(
            onDismissRequest = onDismissSuccess,
            title = { Text("Error") },
            text = { Text("Failed to update password. Please try again.") },
            confirmButton = {
                TextButton(onClick = onDismissSuccess) {
                    Text("OK")
                }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            PasswordFieldWithLabel(
                label = "Old password",
                value = state.oldPassword,
                show = showOld,
                toggleVisibility = { showOld = !showOld }
            ) { newValue ->
                onValueChange { it.copy(oldPassword = newValue) }
            }

            PasswordFieldWithLabel(
                label = "New password",
                value = state.newPassword,
                show = showNew,
                toggleVisibility = { showNew = !showNew }
            ) { newValue ->
                onValueChange { it.copy(newPassword = newValue) }
            }

            PasswordFieldWithLabel(
                label = "Confirm new password",
                value = state.confirmPassword,
                show = showConfirm,
                toggleVisibility = { showConfirm = !showConfirm }
            ) { newValue ->
                onValueChange { it.copy(confirmPassword = newValue) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ValidationChecklist(
                minLength = state.isMinLengthValid,
                case = state.isCaseValid,
                special = state.isSpecialCharValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (hasChanges) showDialog = true
                        else onUpdate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Update")
                }
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Text("Cancel", color = SecondaryColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PasswordFieldWithLabel(
    label: String,
    value: String,
    show: Boolean,
    toggleVisibility: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        color = Color.DarkGray,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray) },
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = toggleVisibility) {
                Icon(
                    if (show) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(vertical = 4.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE3E3E3),
            unfocusedContainerColor = Color(0xFFE3E3E3),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
}