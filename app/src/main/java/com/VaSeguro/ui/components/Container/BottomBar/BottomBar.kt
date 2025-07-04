package com.VaSeguro.ui.components.Container.BottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    navItems: List<String>
) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            navItems.forEach { item ->
                NavigationBarItem(
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    icon = {
                        Icon(
                            imageVector = when (item) {
                                "Mapa", "Map" -> Icons.Filled.Map
                                "Historial", "History" -> Icons.Filled.History
                                "Bus", "Mi Bus" -> Icons.Filled.DirectionsBus
                                "Hijo", "Hijos", "Clientes" -> Icons.Filled.Face
                                "Inicio" -> Icons.Filled.Home
                                "Rutas", "Mis Rutas" -> Icons.Filled.Route
                                "Rutas" -> Icons.Filled.Bookmark // <-- Add this line
                                "Paradas" -> Icons.Filled.Place
                                "Usuarios" -> Icons.Filled.People
                                "Buses", "Vehiculos" -> Icons.Filled.DirectionsCar
                                else -> Icons.Filled.Star
                            },
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF6C63FF),
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray
                    )
                )
            }
        }
    }
}