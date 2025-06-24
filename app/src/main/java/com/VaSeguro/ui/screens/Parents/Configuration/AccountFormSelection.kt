package com.VaSeguro.ui.screens.Parents.Configuration

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Transgender
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.theme.PrimaryColor
import com.VaSeguro.ui.theme.SecondaryColor
import java.io.File

@Composable
fun AccountFormSection(
    state: UserData,
    original: UserData,
    onValueChange: ((UserData) -> UserData) -> Unit,
    onUpdate: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    updateSuccess: Boolean?,
    onDismissSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showImageDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val hasChanges = state != original

    // Image picker state
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraLaunch by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it
            onValueChange { user -> user.copy(profilePic = it.toString()) }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            profileImageUri = cameraImageUri
            onValueChange { user -> user.copy(profilePic = cameraImageUri.toString()) }
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

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
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
                    showImageDialog = false
                }) { Text("Tomar foto", color = Color.Gray) }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showImageDialog = false
                }) { Text("Elegir de galería", color = Color.Gray) }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black
        )
    }

    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirm Update",
            message = "Are you sure you want to update this information?",
            onConfirm = {
                showConfirmDialog = false
                onUpdate()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .background(Color.White)
                    .padding(horizontal = 10.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                // Avatar and edit icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageToShow = profileImageUri?.toString() ?: state.profilePic
                        if (!imageToShow.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(imageToShow),
                                contentDescription = "Profile Picture",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = state.forename.take(1) + (state.surname.takeIf { it.isNotEmpty() }?.take(1) ?: ""),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-90).dp, y = (-110).dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showImageDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormField(
                    label = "Forenames",
                    value = state.forename,
                    onChange = { newValue -> onValueChange { it.copy(forename = newValue) } },
                    trailingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = "Forenames", tint = Color.Gray)
                    }
                )

                FormField(
                    label = "Surnames",
                    value = state.surname,
                    onChange = { newValue -> onValueChange { it.copy(surname = newValue) } },
                    trailingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = "Surnames", tint = Color.Gray)
                    }
                )

                FormField(
                    label = "Gender",
                    value = state.gender ?: "",
                    onChange = { newValue -> onValueChange { it.copy(gender = newValue) } },
                    trailingIcon = {
                        Icon(Icons.Outlined.Transgender, contentDescription = "Gender", tint = Color.Gray)
                    }
                )

                FormField(
                    label = "Phone",
                    value = state.phoneNumber,
                    onChange = { newValue -> onValueChange { it.copy(phoneNumber = newValue) } },
                    trailingIcon = {
                        Icon(Icons.Outlined.PhoneIphone, contentDescription = "Phone", tint = Color.Gray)
                    }
                )

                FormField(
                    label = "Email",
                    value = state.email,
                    onChange = { newValue -> onValueChange { it.copy(email = newValue) } },
                    trailingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = "Email", tint = Color.Gray)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (hasChanges) showConfirmDialog = true
                            else onUpdate()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Actualizar")
                    }
                    TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Cancelar", color = SecondaryColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Success/Error dialog
        if (updateSuccess == true) {
            AlertDialog(
                onDismissRequest = onDismissSuccess,
                title = { Text("Cambio Realizado") },
                text = { Text("Cuenta actualizada con exito!") },
                confirmButton = {
                    TextButton(onClick = onDismissSuccess) { Text("OK") }
                }
            )
        } else if (updateSuccess == false) {
            AlertDialog(
                onDismissRequest = onDismissSuccess,
                title = { Text("Error") },
                text = { Text("Failed to update account. Please try again.") },
                confirmButton = {
                    TextButton(onClick = onDismissSuccess) { Text("OK") }
                }
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        trailingIcon = trailingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE3E3E3),
            unfocusedContainerColor = Color(0xFFE3E3E3),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
}