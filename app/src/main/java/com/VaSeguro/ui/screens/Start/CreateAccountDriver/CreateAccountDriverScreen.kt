package com.VaSeguro.ui.screens.Start.CreateAccountDriver

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowLeft
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.data.AppProvider
import java.io.File
import java.util.regex.Pattern
import com.VaSeguro.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountDriverScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CreateAccountDriverViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return CreateAccountDriverViewModel(
                    appProvider.provideAuthRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    val name by viewModel.name.collectAsState()
    val surname by viewModel.surname.collectAsState()
    val email by viewModel.email.collectAsState()
    val phoneNumber by viewModel.phone.collectAsState()
    val password by viewModel.password.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var emailValid by remember { mutableStateOf(true) }
    var phoneValid by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("M", "F", "Otro")
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraLaunch by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it
            viewModel.onImageChange(it)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            profileImageUri = cameraImageUri
            viewModel.onImageChange(cameraImageUri)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingCameraLaunch?.let { uri ->
            if (isGranted) {
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
            pendingCameraLaunch = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (profileImageUri == null) Color(0xFF6C63FF) else Color.Transparent
                        )
                        .then(
                            if (profileImageUri != null)
                                Modifier.border(4.dp, Color(0xFF6C63FF), CircleShape)
                            else Modifier
                        )
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Default profile",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Seleccionar imagen") },
                        text = { Text("¿Cómo quieres agregar tu foto?") },
                        confirmButton = {
                            TextButton(onClick = {
                                // Camera
                                val photoFile = File.createTempFile(
                                    "profile_", ".jpg",
                                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                )
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    cameraImageUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    pendingCameraLaunch = uri
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                                showDialog = false
                            }) { Text("Tomar foto") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                galleryLauncher.launch("image/*")
                                showDialog = false
                            }) { Text("Elegir de galería") }
                        }
                    )
                }

                TextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nombre") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = "Nombre", tint = Color.Gray)
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

                TextField(
                    value = surname,
                    onValueChange = viewModel::onSurnameChange,
                    label = { Text("Apellido") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = "Apellido", tint = Color.Gray)
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
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Género") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE3E3E3),
                            unfocusedContainerColor = Color(0xFFE3E3E3),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onGenderChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                TextField(
                    value = email,
                    onValueChange = {
                        viewModel.onEmailChange(it)
                        emailValid = Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", it)
                    },
                    label = { Text("Email") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = "Email", tint = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = !emailValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                TextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            viewModel.onPhoneChange(it)
                            phoneValid = true
                        } else {
                            phoneValid = false
                        }
                    },
                    label = { Text("Número de teléfono") },
                    trailingIcon = {
                        Icon(Icons.Outlined.PhoneIphone, contentDescription = "Teléfono", tint = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    isError = !phoneValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                TextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        viewModel.register(
                            context = context,
                            onSuccess = {         navController.navigate(
                                "content/" +
                                        Uri.encode("¡Registro exitoso!") + "/" +
                                        Uri.encode("Tu cuenta ha sido creada correctamente. Debes esperar a que sea verificada por nuestro grupo de expertos. Se te enviara un correo de verificacion ") + "/" +
                                        R.drawable.aprove + "/" +
                                        Uri.encode("Continuar")+"/"+
                                        Uri.encode("login")

                            )},
                            onError = { }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Registrarse", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}