package com.VaSeguro.ui.screens.Start.CreateAccountDriver.RegisterBus

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.VaSeguro.R
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.Aux.ColorDropdownField
import com.VaSeguro.ui.Aux.YearDropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusScreen(
    navController: NavController,
    onRegisterSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: RegisterBusViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return RegisterBusViewModel(
                    appProvider.provideVehicleRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    var plate by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    val imageUrl by viewModel.imageUrl.collectAsState()
    val error by viewModel.error.collectAsState()
    val isImageLoading by viewModel.isImageLoading.collectAsState()
    val isRegisterLoading by viewModel.isRegisterLoading.collectAsState()
    val colorOptions = listOf("Blanco", "Negro", "Rojo", "Azul", "Verde", "Amarillo", "Gris", "Plateado", "Marrón")
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val yearOptions = (1980..currentYear).map { it.toString() }.reversed()

    LaunchedEffect(brand, model) {
        if (brand.length > 1 && model.length > 1) {
            kotlinx.coroutines.delay(500)
            viewModel.fetchCarImage(brand, model)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registra tu vehículo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                val fancyboxGif = "https://cdnjs.cloudflare.com/ajax/libs/fancybox/2.1.5/fancybox_loading.gif"
                val showDefault = imageUrl.isNullOrBlank() ||
                        imageUrl!!.endsWith(".gif", ignoreCase = true) ||
                        imageUrl == fancyboxGif
                Crossfade(targetState = showDefault, label = "busImageCrossfade") { isDefault ->
                    if (isDefault) {
                        Image(
                            painter = painterResource(R.drawable.default_bus),
                            contentDescription = "Default bus",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Bus photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.default_bus),
                            error = painterResource(R.drawable.default_bus)
                        )
                    }
                }
                if (isImageLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White
                        )
                    }
                }
            }
            OutlinedTextField(
                value = plate,
                onValueChange = { input ->
                    plate = input.filter { it.isLetterOrDigit() }.uppercase()
                },
                label = { Text("Placa") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii
                )
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth()
            )
            ColorDropdownField(value = color, onValueChange = { color = it }, colorOptions = colorOptions)
            YearDropdownField(value = year, onValueChange = { year = it }, yearOptions = yearOptions)
            OutlinedTextField(
                value = capacity,
                onValueChange = { input ->
                    capacity = input.filter { it.isDigit() }
                },
                label = { Text("Capacidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    if (plate.isBlank() || model.isBlank() || brand.isBlank() ||
                        year.isBlank() || color.isBlank() || capacity.isBlank()) {

                    } else {
                        viewModel.registerBus(
                            plate = plate,
                            model = model,
                            brand = brand,
                            year = year,
                            color = color,
                            capacity = capacity,
                            carPicFile = null,
                            onSuccess = {
                                onRegisterSuccess()
                                navController.navigate(
                                    "content/" +
                                            Uri.encode("¡Registro exitoso!") + "/" +
                                            Uri.encode("Tu cuenta ha sido creada correctamente. Debes esperar a que sea verificada por nuestro grupo de expertos. Se te enviara un correo de verificacion ") + "/" +
                                            R.drawable.aprove + "/" +
                                            Uri.encode("Continuar") + "/" +
                                            Uri.encode("login")
                                )
                            },
                            onError = { errorMessage ->

                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF),
                    contentColor = Color.White
                )
            ) {
                Text("Registrar Bus", fontSize = 18.sp)
            }
        }
        if (isRegisterLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}