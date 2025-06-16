package com.VaSeguro.ui.screens.Parents.Configuration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Security.SecurityFormState
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Forms.ValidationChecklist
import com.VaSeguro.ui.theme.PrimaryColor
import com.VaSeguro.ui.theme.SecondaryColor
import com.VaSeguro.ui.theme.SecunrayColorDark

@Composable
fun SecurityFormSection(
    state: SecurityFormState,
    original: SecurityFormState,
    onValueChange: ((SecurityFormState) -> SecurityFormState) -> Unit,
    onUpdate: () -> Unit,
    onCancel: () -> Unit
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
                .padding(horizontal = 24.dp)
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = SecondaryColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(2.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = null,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = toggleVisibility) {
                Icon(if (show) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(BorderStroke(2.dp, SecunrayColorDark), RoundedCornerShape(20.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecunrayColorDark,
            unfocusedBorderColor = SecunrayColorDark
        )
    )
}