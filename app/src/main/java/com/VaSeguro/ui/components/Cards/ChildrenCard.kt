package com.VaSeguro.ui.components.Cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.R
import com.VaSeguro.data.model.Child.Child

@Composable
fun ChildrenCard(
  child: Child,
  isExpanded: Boolean,
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  onToggleExpand: () -> Unit = {},
){
  var isLoading by remember { mutableStateOf(true) }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable{ onToggleExpand() },
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, Color.LightGray),
    colors = CardDefaults.cardColors(
      containerColor = Color.White
    )

  ){
    Column {
      Row(
        modifier = Modifier
          .padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp
          ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.padding(16.dp).weight(1f),
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
            text = "${child.age} years old",
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
          val imageData = child.profilePic ?: R.drawable.school_title
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(imageData)
              .crossfade(true)
              .build(),
            contentDescription = child.fullName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .width(100.dp)
              .height(100.dp),
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

      if (isExpanded){
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
              modifier = Modifier.padding(16.dp)
            ) {
              Text(
                text = child.medicalInfo.ifEmpty { "No info available" },
                color = Color.White,
              )
              Text(
                text = "Birthday: ${child.birth}",
                color = Color.White,
              )
              Text(
                text = "Joined at: ${child.createdAt}",
                color = Color.White,
              )
            }

            Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
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
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ChildrenCardPreview() {
  val child = Child(
    id = "1",
    fullName = "Jonh Doe",
    surnames = "Doe",
    forenames = "Jonh",
    birth = "2000-01-01",
    age = 12,
    driver = "1",
    parent = "1",
    medicalInfo = "",
    createdAt = "2023-01-01",
    profilePic = null,
  )
  ChildrenCard(
    child = child,
    isExpanded = true,
  )
}