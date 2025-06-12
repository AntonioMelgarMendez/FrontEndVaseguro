package com.VaSeguro.ui.components.Container

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    navItems: List<String>
) {
    // Mapa de iconos para cada opción de navegación
    val navIcons: Map<String, ImageVector> = mapOf(
        // Iconos para Administrador
        "Home" to Icons.Default.Home,
        "Usuarios" to Icons.Default.Person,
        "Paradas" to Icons.Default.Place,
        "Rutas" to Icons.Default.Map,
        "Aprove" to Icons.Default.Mail,
        "Autos" to Icons.Default.DirectionsCar,

        // Iconos para Usuario
        "Mapa" to Icons.Default.Map,
        "Historial" to Icons.Default.History,
        "Bus" to Icons.Default.DirectionsBus,
        "Hijos" to Icons.Default.Face,

        // Iconos para Conductor
        "Pasajeros" to Icons.Default.People,
        "Rutas" to Icons.Default.Route
    )

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = navIcons[item] ?: Icons.Default.Star,
                        contentDescription = item
                    )
                },
                label = { Text(item) }
            )
        }
    }
}