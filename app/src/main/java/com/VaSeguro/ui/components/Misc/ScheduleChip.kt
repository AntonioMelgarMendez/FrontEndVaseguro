package com.VaSeguro.ui.components.Misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ScheduleChip(time: String) {
  Card(
    shape = RoundedCornerShape(8.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
  ) {
    Box(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = time,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}