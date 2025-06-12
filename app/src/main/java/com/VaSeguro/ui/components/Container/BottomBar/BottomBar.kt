package com.VaSeguro.ui.components.Container.BottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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
                            "Mapa", "Map" -> Icons.Filled.Map
                            "Historial", "History" -> Icons.Filled.History
                            "Bus", "Mi Bus" -> Icons.Filled.DirectionsBus
                            "Hijos", "Clientes" -> Icons.Filled.Face
                            "Inicio" -> Icons.Filled.Home
                            "Rutas", "Mis Rutas" -> Icons.Filled.Route
                            "Paradas" -> Icons.Filled.Place
                            "Usuarios" -> Icons.Filled.People
                            "Vehiculos" -> Icons.Filled.DirectionsCar
                            else -> Icons.Filled.Star
                        },
                        contentDescription = item
                    )
                },
                label = { Text(item) }
            )
        }
    }
}