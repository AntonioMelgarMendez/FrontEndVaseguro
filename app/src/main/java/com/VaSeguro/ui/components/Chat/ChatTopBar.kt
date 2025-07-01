
package com.VaSeguro.ui.components.Chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.data.model.User.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
  user: UserData,
  onBackClick: () -> Unit,
  onCallClick: () -> Unit = {}

) {
  TopAppBar(
    title = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val initials = (user.forename.take(1) + (user.surname.takeIf { it.isNotEmpty() }?.take(1) ?: "")).uppercase()
          if (!user.profilePic.isNullOrBlank()) {
            var isImageLoading by remember { mutableStateOf(true) }
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(user.profilePic)
                  .crossfade(true)
                  .build(),
                contentDescription = user.forename,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape),
                onState = {
                  isImageLoading = it is AsyncImagePainter.State.Loading
                }
              )
              if (isImageLoading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp
                )
              }
            }
          } else {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = initials,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = Bold
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "${user.forename} ${user.surname}",
            fontWeight = Bold,
            fontSize = 24.sp
          )
        }
      }
    },
    navigationIcon = {
      IconButton(onClick = onBackClick) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
      }
    },
    actions = {
      IconButton(onClick = onCallClick) {
        Icon(Icons.Default.Call, contentDescription = "Call")
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color.White
    )
  )
}