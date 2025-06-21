package com.VaSeguro.ui.components.Chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.R
import com.VaSeguro.data.model.User.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(user: UserData, onBackClick: () -> Unit) {
  var isLoading by remember { mutableStateOf(true) }
  TopAppBar(
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape),
          contentAlignment = Alignment.Center
        ) {
          val imageData = user.profilePic ?: R.drawable.school_title
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(imageData)
              .crossfade(true)
              .build(),
            contentDescription = user.forename,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(36.dp)
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

        Spacer(modifier = Modifier.width(8.dp))

        Text("${user.forename} ${user.surname}")
      }
    },
    navigationIcon = {
      IconButton(onClick = onBackClick) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color.White
    )
  )
}
