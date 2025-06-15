package com.VaSeguro.ui.components.Forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.VaSeguro.ui.theme.PrimaryColor

@Composable
fun SegmentedTabRow(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabTitles = listOf("Account", "Security")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Row(
            Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabTitles.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Color(0xFFEDEBFF) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = title,
                            color = if (selected) PrimaryColor else Color.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}