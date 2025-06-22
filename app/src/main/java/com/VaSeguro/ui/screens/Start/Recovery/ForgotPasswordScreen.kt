
package com.VaSeguro.ui.screens.Start.Recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.data.AppProvider

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return ForgotPasswordViewModel(
                    appProvider.provideAuthRepository()
                ) as T
            }
        }
    )

    val email = viewModel.email
    val isLoading = viewModel.isLoading
    val message = viewModel.message
    val error = viewModel.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        IconButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp) // Less padding, closer to the corner
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back to login",
                tint = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 16.dp), // Less padding for data
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recuperar contraseña", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()

            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.recoverPassword() },
                enabled = !isLoading
            ) {
                Text("Enviar enlace")
            }
            message?.let { Text(it, color = Color.Green, modifier = Modifier.padding(top = 8.dp)) }
            error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}