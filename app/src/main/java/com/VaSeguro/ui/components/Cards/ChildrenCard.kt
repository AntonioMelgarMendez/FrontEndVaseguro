package com.VaSeguro.ui.components.Cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.R
import com.VaSeguro.data.model.Children.Children
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun ChildrenCard(
  child: Children,
  isExpanded: Boolean,
  isDriver: Boolean = false,
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  onToggleExpand: () -> Unit = {},
  onChat: (chatId: String) -> Unit = {}
) {
  var isLoading by remember { mutableStateOf(true) }
  var showDialog by remember { mutableStateOf(false) }
  val medicalInfo = child.medical_info.ifEmpty { "No hay informacion " }

  Card(
    modifier = Modifier.fillMaxWidth()  .clickable { onToggleExpand() },
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, Color.LightGray),
    colors = CardDefaults.cardColors(containerColor = Color.White)
  ) {
    Column {
      Row(
        modifier = Modifier
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier
            .padding(16.dp)
            .weight(1f),
        ) {
          Text(
            text = child.forenames,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = child.surnames,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${calculateAge(child.birth_date)} Años de edad",
            fontWeight = FontWeight.Light,
            fontSize = 16.sp
          )
        }

        Box(
          modifier = Modifier
            .width(100.dp)
            .height(100.dp),
          contentAlignment = Alignment.Center
        ) {
          val imageData = child.profile_pic ?: R.drawable.child
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(imageData)
              .crossfade(true)
              .build(),
            contentDescription = child.forenames,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .width(100.dp)
              .height(100.dp)
              .clip(CircleShape),
            onState = {
              isLoading = when (it) {
                is AsyncImagePainter.State.Loading -> true
                is AsyncImagePainter.State.Success,
                is AsyncImagePainter.State.Error -> false
                else -> false
              }
            }
          )
          if (isLoading) {
            CircularProgressIndicator()
          }
        }
      }

      if (isExpanded) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6C63FF)),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(
              modifier = Modifier
                .padding(16.dp)
                .weight(1f)
            ) {
              Text(
                text = "Fecha de nacimiento: ${child.birth_date}",
                color = Color.White,
              )
              Spacer(modifier = Modifier.height(8.dp))
              TextButton(onClick = { showDialog = true }) {
                Text("Ver más", color = Color.White)
              }
            }

            Row(
              modifier = Modifier
                .padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (!isDriver) {
                IconButton(onClick = onEditClick) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                  )
                }
                IconButton(onClick = onDeleteClick) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                  )
                }
              }
              IconButton(
                onClick = {
                  val chatPartnerId = if (isDriver) child.parent_id.toString()
                  else child.driver_id.toString()
                  onChat(chatPartnerId)
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Chat,
                  contentDescription = "Chat",
                  tint = Color.White,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
          }
        }
      }
    }
    if (showDialog) {
      Dialog(onDismissRequest = { showDialog = false }) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          tonalElevation = 8.dp,
          modifier = Modifier
            .widthIn(max = 350.dp)
            .heightIn(max = 480.dp)
        ) {
          val scrollState = rememberScrollState()
          Column(
            modifier = Modifier
              .padding(12.dp)
              .fillMaxWidth()
              .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.TopEnd
            ) {
              IconButton(onClick = { showDialog = false }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Cerrar"
                )
              }
            }
            Text(
              text = "Detalles del niño",
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = Color.Black,
              modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
              modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              val imageData = child.profile_pic ?: R.drawable.child
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(imageData)
                  .crossfade(true)
                  .build(),
                contentDescription = child.forenames,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(90.dp)
                  .clip(CircleShape)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoField(Icons.Outlined.Person, "Nombres", child.forenames)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoField(Icons.Outlined.Person, "Apellidos", child.surnames)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoField(Icons.Outlined.Cake, "Fecha de nacimiento", child.birth_date)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoField(Icons.Outlined.CalendarToday, "Edad", "${calculateAge(child.birth_date)} años")
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoField(Icons.Outlined.MedicalServices, "Información médica", medicalInfo)
          }
        }
      }
    }

  }
}

fun calculateAge(birthDateString: String): Int {
  return try {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val birthDate = LocalDate.parse(birthDateString, formatter)
    val today = LocalDate.now()
    Period.between(birthDate, today).years
  } catch (e: Exception) {
    0
  }
}
@Composable
private fun ProfileInfoField(icon: ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
      .background(Color(0xFFF3F3F3), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(label, color = Color.Gray, fontSize = 12.sp)
      Text(value, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = Color.DarkGray,
      modifier = Modifier.size(18.dp)
    )
  }
}