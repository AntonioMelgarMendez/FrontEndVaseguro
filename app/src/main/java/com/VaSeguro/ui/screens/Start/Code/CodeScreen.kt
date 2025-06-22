package com.VaSeguro.ui.screens.Start.Code

import QRScannerView
import android.net.Uri
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.screens.Start.Code.CodeViewModel.CodeViewModel

@Composable
fun CodeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CodeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return CodeViewModel(
                    requestRepository = appProvider.provideRequestRepository(),
                    context = context
                ) as T
            }
        }
    )
    val code by viewModel.code.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                QRScannerView(
                    modifier = Modifier.size(250.dp),
                    onCodeScanned = { scannedCode ->
                        viewModel.onCodeChange(scannedCode)
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Inserte su código",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Por favor ingrese el código de su conductor.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                CodeInput(
                    code = code,
                    onCodeChange = viewModel::onCodeChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        Log.e("Image", "Driver: ${viewModel.driverProfilePic}")
                        viewModel.verifyCode(
                            onSuccess = {
                                val title = "Este es tu conductor"
                                val description = "Vamos a seguir rellenado información para que puedas comenzar tu viaje."
                                val imageRes = viewModel.driverProfilePic
                                val buttonText = "Continuar"
                                val destination = "home"
                                navController.navigate(
                                    "content/$title/$description/${Uri.encode(imageRes)}/$buttonText/$destination"
                                )
                            },
                            onError = {}
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Verificar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { /* Navegar a chat con conductor */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Contactar con tu conductor", color = Color(0xFF6C63FF))
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    length: Int = 6
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
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
                val upper = it
                if (upper.length <= length && upper.all { c -> c.isLetterOrDigit() }) onCodeChange(upper)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            singleLine = true
        )
    }
}
@Preview(showBackground = true)
@Composable
fun CodeScreenPreview() {
    MaterialTheme {
        CodeScreen(navController = rememberNavController())
    }
}