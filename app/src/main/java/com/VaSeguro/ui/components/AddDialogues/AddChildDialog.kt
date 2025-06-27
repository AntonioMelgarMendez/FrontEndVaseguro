package com.VaSeguro.ui.components.AddDialogues

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.data.model.Children.Children
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
  onDismiss: () -> Unit,
  onConfirm: (Children, profileImageUri: Uri?) -> Unit,
  existingChild: Children? = null
) {
  val customColor = Color(0xFF6C63FF)

  var forenames by remember { mutableStateOf(existingChild?.forenames ?: "") }
  var surnames by remember { mutableStateOf(existingChild?.surnames ?: "") }
  var birthDate by remember { mutableStateOf(existingChild?.birth_date ?: "") }
  var medicalInfo by remember { mutableStateOf(existingChild?.medical_info ?: "") }
  var gender by remember { mutableStateOf(existingChild?.gender ?: "") }

  // Image picker state
  var profileImageUri by remember {
    mutableStateOf(
      existingChild?.profile_pic?.let { Uri.parse(it) }
    )
  }
  var showDialog by remember { mutableStateOf(false) }
  var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
  var pendingCameraLaunch by remember { mutableStateOf<Uri?>(null) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

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

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        .background(Color.White, RoundedCornerShape(16.dp))
        .padding(24.dp)
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Title
        Text(
          text = if (existingChild == null) "Agregar Niño" else "Editar Niño",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        // Avatar
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
              modifier = Modifier.size(48.dp)
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

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
          value = forenames,
          onValueChange = { forenames = it },
          label = { Text("Nombres") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = surnames,
          onValueChange = { surnames = it },
          label = { Text("Apellidos") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = birthDate,
          onValueChange = { birthDate = it },
          label = { Text("Fecha de nacimiento (yyyy-MM-dd)") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = medicalInfo,
          onValueChange = { medicalInfo = it },
          label = { Text("Info médica") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        OutlinedTextField(
          value = gender,
          onValueChange = { gender = it },
          label = { Text("Género") },
          modifier = Modifier.fillMaxWidth(),
          colors = textFieldColors(customColor)
        )

        Spacer(Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(contentColor = customColor)
          ) {
            Text("Cancelar", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              if (
                forenames.isNotBlank() && surnames.isNotBlank() && birthDate.isNotBlank()
              ) {
                val newChild = Children(
                  id = existingChild?.id ?: (10000..99999).random(),
                  forenames = forenames,
                  surnames = surnames,
                  birth_date = birthDate,
                  medical_info = medicalInfo,
                  parent_id = existingChild?.parent_id ?: 0,
                  driver_id = existingChild?.driver_id ?: 0,
                  profile_pic = existingChild?.profile_pic,
                  gender = gender
                )
                onConfirm(newChild, profileImageUri)
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = customColor,
              contentColor = Color.White
            )
          ) {
            Text(
              text = if (existingChild == null) "Agregar" else "Guardar cambios",
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

@Composable
private fun textFieldColors(customColor: Color) = TextFieldDefaults.colors(
  focusedContainerColor = Color.Transparent,
  unfocusedContainerColor = Color.Transparent,
  focusedIndicatorColor = customColor,
  unfocusedIndicatorColor = Color.LightGray,
  focusedLabelColor = customColor,
  unfocusedLabelColor = Color.Gray
)