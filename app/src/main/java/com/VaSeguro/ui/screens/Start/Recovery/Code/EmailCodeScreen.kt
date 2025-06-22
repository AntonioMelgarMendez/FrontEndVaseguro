package com.VaSeguro.ui.screens.Start.Recovery.Code

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.R
import com.VaSeguro.data.AppProvider

@Composable
fun EmailCodeScreen(
    navController:NavController

) {
    val context = LocalContext.current
    val viewModel: EmailCodeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return EmailCodeViewModel(
                    appProvider.provideAuthRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        IconButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 48.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(64.dp)
            )
        }

        // Main content centered
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.family_title),
                contentDescription = "Recovery",
                modifier = Modifier.size(300.dp)
            )
            Spacer(Modifier.height(8.dp))

            when (viewModel.step) {
                0 -> {
                    Text(
                        "Enter your email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.sendEmail() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                           contentColor = Color.White
                    )
                    ) { Text("Enviar codigo ", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    viewModel.error?.let { Text(it, color = Color.Red) }
                }
                1 -> {
                    Text(
                        "Introduce el codigo de 6 digitos enviado a tu correo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    RecoveryCodeInput(
                        code = viewModel.code,
                        onCodeChange = { viewModel.code = it }
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.verifyCode() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            contentColor = Color.White
                        )
                    ) { Text("Verificar codigo", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    viewModel.error?.let { Text(it, color = Color.Red) }
                }
                2 -> {
                    Text(
                        "Introduce tu nueva contraseña",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.resetPassword() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            contentColor = Color.White
                        )
                    ) { Text("Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    viewModel.error?.let { Text(it, color = Color.Red) }
                }
                3 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Contraseña cambiada con exito!",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("login") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Volver a login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun RecoveryCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    length: Int = 6
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
        ) {
            for (i in 0 until length) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFD3D3D3), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = code.getOrNull(i)?.toString() ?: "",
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        OutlinedTextField(
            value = code,
            onValueChange = {
                val filtered = it.filter { c -> c.isDigit() }
                if (filtered.length <= length) onCodeChange(filtered)
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

