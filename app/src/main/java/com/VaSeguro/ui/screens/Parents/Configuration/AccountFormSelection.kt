package com.VaSeguro.ui.screens.Parents.Configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Forms.CustomOutlinedTextField
import com.VaSeguro.ui.theme.PrimaryColor
import com.VaSeguro.ui.theme.SecondaryColor

@Composable
fun AccountFormSection(
    state: UserData,
    original: UserData,
    onValueChange: ((UserData) -> UserData) -> Unit,
    onUpdate: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }
    val hasChanges = state != original

    if (showDialog) {
        ConfirmationDialog(
            title = "Confirm Update",
            message = "Are you sure you want to update this information?",
            onConfirm = {
                showDialog = false
                onUpdate() // Llama a la lógica real de actualización
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
            Text("Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.forename.take(1) + (state.surname.takeIf { it.isNotEmpty() }?.take(1) ?: ""),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormField(label = "Forenames", value = state.forename) { newValue ->
                onValueChange { it.copy(forename = newValue) }
            }

            FormField("Surnames", state.surname) { newValue ->
                onValueChange { it.copy(surname = newValue) }
            }

            FormField("Email", state.email) { newValue ->
                onValueChange { it.copy(email = newValue) }
            }

            FormField("Gender", state.gender ?: "") { newValue ->
                onValueChange { it.copy(gender = newValue) }
            }

            FormField("Phone", state.phoneNumber) { newValue ->
                onValueChange { it.copy(phoneNumber = newValue) }
            }

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
private fun FormField(label: String, value: String, onChange: (String) -> Unit) {
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(2.dp))
    CustomOutlinedTextField(placeholder = label, value = value, onValueChange = onChange)
    Spacer(modifier = Modifier.height(8.dp))
}
