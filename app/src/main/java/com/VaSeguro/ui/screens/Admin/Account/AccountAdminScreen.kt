package com.VaSeguro.ui.screens.Admin.Account

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.theme.NeutralColor
import com.VaSeguro.ui.theme.PrimaryColor


@Composable
fun AccountAdminScreen(viewModel: AccountAdminScreenViewModel = viewModel(), ) {
    val account by viewModel.account.collectAsState()
    val navController = rememberNavController()
    // Estado para mostrar el dialog de edición
    var showDialog by remember { mutableStateOf(false) }

    // Mostrar el Dialog de actualización
    if (showDialog) {
        EditAccountDialog(
            currentData = account,
            onDismiss = { showDialog = false },
            onSave = { newData ->
                viewModel.updateAccount(newData)
                showDialog = false
            }
        )
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Foto de perfil, si no hay, mostrar un icono
            if (account.profilePic != null) {
                /*Image(
                    painter = rememberImagePainter(account.profilePic),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colors.primary, CircleShape)
                )*/
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de usuario",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, PrimaryColor, CircleShape),
                    tint = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar la información del usuario con bordes en labels
            InfoRow(label = "Nombre", value = "${account.forename} ${account.surname}")
            InfoRow(label = "Correo", value = account.email)
            InfoRow(label = "Teléfono", value = account.phoneNumber)
            InfoRow(label = "Género", value = account.gender ?: "No especificado")
            InfoRow(label = "Rol", value = account.role_id.role_name)

            // Botón para editar la información
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showDialog = true }) {
                Text("Editar Información")
            }
        }

}

@Composable
fun InfoRow(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Mostrar la etiqueta (label)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Caja con borde para el valor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(8.dp)
                .border(1.dp, PrimaryColor, RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(9.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun EditAccountDialog(
    currentData: UserData,
    onDismiss: () -> Unit,
    onSave: (UserData) -> Unit
) {
    var updatedForename by remember { mutableStateOf(currentData.forename) }
    var updatedSurname by remember { mutableStateOf(currentData.surname) }
    var updatedEmail by remember { mutableStateOf(currentData.email) }
    var updatedPhone by remember { mutableStateOf(currentData.phoneNumber) }
    var updatedGender by remember { mutableStateOf(currentData.gender) }
    var updatedRole by remember { mutableStateOf(currentData.role_id.role_name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cuenta") },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedForename,
                    onValueChange = { updatedForename = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = updatedSurname,
                    onValueChange = { updatedSurname = it },
                    label = { Text("Apellidos") }
                )
                OutlinedTextField(
                    value = updatedEmail,
                    onValueChange = { updatedEmail = it },
                    label = { Text("Correo") }
                )
                OutlinedTextField(
                    value = updatedPhone,
                    onValueChange = { updatedPhone = it },
                    label = { Text("Teléfono") }
                )
                OutlinedTextField(
                    value = updatedGender ?: "",
                    onValueChange = { updatedGender = it },
                    label = { Text("Género") }
                )
                OutlinedTextField(
                    value = updatedRole,
                    onValueChange = { updatedRole = it },
                    label = { Text("Rol") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedUserData = currentData.copy(
                    forename = updatedForename,
                    surname = updatedSurname,
                    email = updatedEmail,
                    phoneNumber = updatedPhone,
                    gender = updatedGender,
                    role_id = UserRole(currentData.role_id.id, updatedRole)
                )
                onSave(updatedUserData)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AccountAdminScreenPreview() {
    AccountAdminScreen()
}