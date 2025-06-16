package com.VaSeguro.ui.screens.Admin.Home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoCard(
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    animateIcon: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (animateIcon) {
                var showIcon by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { showIcon = true }
                AnimatedVisibility(
                    visible = showIcon,
                    enter = fadeIn() + expandIn(),
                    exit = fadeOut()
                ) {
                    Icon(icon, contentDescription = label, tint = Color.DarkGray, modifier = Modifier.size(iconSize))
                }
            } else {
                Icon(icon, contentDescription = label, tint = Color.DarkGray, modifier = Modifier.size(iconSize))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
                Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
            }
        }
    }
}