package com.VaSeguro.ui.screens.Start.SignUp.ChildrenScreen

import ChildrenViewModel
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.data.AppProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterChildrenScreen(navController: NavController, driverId: String, profile_pic: String? = null) {
    val context = LocalContext.current
    val viewModel: ChildrenViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return ChildrenViewModel(
                    childrenRepository = appProvider.provideChildrenRepository(),
                    userPreferencesRepository = appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    fun uriToMultipart(context: android.content.Context, uri: Uri?): MultipartBody.Part? {
        uri ?: return null
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "child_profile.jpg")
        file.outputStream().use { output -> inputStream.copyTo(output) }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
    }

    var forenames by remember { mutableStateOf("") }
    var surnames by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var medicalInfo by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    val genderOptions = listOf("M", "F", "Otro")
    var expanded by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraLaunch by remember { mutableStateOf<Uri?>(null) }

    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { profileImageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            profileImageUri = cameraImageUri
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
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registra tu hijo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
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
                    text = { Text("¿Cómo quieres agregar la foto?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val photoFile = File.createTempFile(
                                "child_profile_", ".jpg",
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
                        }) { Text("Tomar foto", color = Color.Gray) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        }) { Text("Elegir de galería", color = Color.Gray) }
                    },
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    textContentColor = Color.Black
                )
            }

            TextField(
                value = forenames,
                onValueChange = { forenames = it },
                label = { Text("Nombres") },
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
                value = surnames,
                onValueChange = { surnames = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3E3E3),
                    unfocusedContainerColor = Color(0xFFE3E3E3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            // Birth date fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = day,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) day = it },
                    label = { Text("Día") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                TextField(
                    value = month,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) month = it },
                    label = { Text("Mes") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                TextField(
                    value = year,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) year = it },
                    label = { Text("Año") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions =KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                TextField(
                    value = medicalInfo,
                    onValueChange = { medicalInfo = it },
                    label = { Text("Información médica") },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 6,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE3E3E3),
                        unfocusedContainerColor = Color(0xFFE3E3E3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

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
                                gender = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (day.isBlank() || month.isBlank() || year.isBlank()) {
                        // Optionally show error to user
                        return@Button
                    }
                    val dayInt = day.toIntOrNull()
                    val monthInt = month.toIntOrNull()
                    val yearInt = year.toIntOrNull()
                    if (dayInt == null || monthInt == null || yearInt == null ||
                        dayInt !in 1..31 || monthInt !in 1..12 || yearInt < 1900) {
                        // Optionally show error to user
                        return@Button
                    }
                    val birthDate = "%04d-%02d-%02d".format(yearInt, monthInt, dayInt)
                    viewModel.createChild(
                        context = context,
                        forenames = forenames,
                        surnames = surnames,
                        birthDate = birthDate, // always a valid string here
                        medicalInfo = medicalInfo,
                        gender = gender,
                        driverId = driverId.toIntOrNull() ?: 0,
                        profilePicUri = profileImageUri,
                        onSuccess = {
                            navController.navigate("content/Este es tu conductor/Vamos a seguir rellenado información para que puedas comenzar tu viaje./${Uri.encode(profile_pic)}/Continuar/home")
                        },
                        onError = { errorMsg -> }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF),
                    contentColor = Color.White
                )
            ) {
                Text("Registrar hijo", fontSize = 18.sp)
            }
        }
    }
}

