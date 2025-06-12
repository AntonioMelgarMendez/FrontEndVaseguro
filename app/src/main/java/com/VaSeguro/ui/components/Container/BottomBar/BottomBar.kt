package com.VaSeguro.ui.components.Container.BottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text

@Composable
fun BottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    navItems: List<String>
) {
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = when (item) {
                            "Map" -> Icons.Default.Place
                            "History" -> Icons.Filled.Place
                            "Bus" -> Icons.Default.Star
                            "Children" -> Icons.Default.Face
                            else -> Icons.Default.Star
                        },
                        contentDescription = item
                    )
                },
                label = { Text(item) }
            )
        }
    }
}
