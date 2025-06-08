package com.VaSeguro.ui.screens.Start.Login


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.DesktopMac
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.R

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
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
            TextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                trailingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = "Email icon", tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            TextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                trailingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = "Lock icon",tint = Color.Gray )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors =  TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),

            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6C63FF),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Recuérdame", modifier = Modifier.padding(start = 8.dp))
                }
                TextButton(onClick = {
                }) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF),
                    contentColor = Color.White
                ),
                onClick = {
                    viewModel.login(
                        onSuccess = { navController.navigate("home") },
                        onError = { error = it }
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Siguiente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                    contentDescription = "Arrow Right",
                    modifier = Modifier.size(32.dp)
                )
            }
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
