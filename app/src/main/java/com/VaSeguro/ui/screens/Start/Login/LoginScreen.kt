package com.VaSeguro.ui.screens.Start.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.DesktopMac
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.R
import com.VaSeguro.data.AppProvider

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return LoginViewModel(
                    appProvider.provideAuthRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    LaunchedEffect(Unit) {
        viewModel.clearFields()
    }
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_login),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .width(220.dp)
                        .height(220.dp)
                )

                Text(
                    text = "Bienvenido",
                    textAlign = TextAlign.Center,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Inicia sesión con tu cuenta",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Campo de email
                TextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = "Email icon", tint = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                // Campo de contraseña
                TextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Contraseña") },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Outlined.Lock else Icons.Outlined.Lock
                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                // Recordar usuario y olvidé contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = viewModel::setRememberMe,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6C63FF),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Text("Recuérdame", modifier = Modifier.padding(start = 8.dp))
                    }
                    TextButton(onClick = { navController.navigate("forgot_password") }) {
                        Text("¿Olvidaste tu contraseña?")
                    }
                }

                // Mostrar errores con mensajes comprensibles
                error?.let { errorCode ->
                    val errorMessage = when (errorCode) {
                        "401" -> "Credenciales incorrectas. Por favor verifica tu email y contraseña."
                        "402" -> "Pago requerido. Por favor completa tu suscripción."
                        "403" -> "Acceso denegado. Su cuenta aun no ha sido verificada"
                        "404" -> "Usuario no encontrado. Verifica tus datos o regístrate."
                        "408" -> "Tiempo de espera agotado. Por favor intenta nuevamente."
                        "500" -> "Error del servidor. Por favor intenta más tarde."
                        "network_error" -> "Problema de conexión. Verifica tu conexión a internet."
                        "invalid_email" -> "Formato de email inválido. Por favor corrígelo."
                        "weak_password" -> "Contraseña demasiado débil. Debe tener al menos 6 caracteres."
                        else -> "Ocurrió un error inesperado. Por favor intenta nuevamente."
                    }

                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Botón de login
                Button(
                    onClick = {
                        viewModel.login(
                            onSuccess = { navController.navigate("home") },
                            onError = { errorCode ->
                                viewModel.setError(errorCode)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    Text("Siguiente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = "Arrow Right",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Enlace a registro
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Black)) {
                                append("¿No tienes cuenta? ")
                            }
                            withStyle(style = SpanStyle(color = Color(0xFF6C63FF))) {
                                append("Regístrate")
                            }
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

