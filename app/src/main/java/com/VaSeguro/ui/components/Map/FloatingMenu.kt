package com.VaSeguro.ui.components.Map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FloatingMenu(
    modifier: Modifier = Modifier,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column (
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AnimatedVisibility (visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton (onClick = { onOptionSelected(1) }) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Agregar punto")
                }
                FloatingActionButton(onClick = { onOptionSelected(2) }) {
                    Icon(Icons.Default.Map, contentDescription = "Planear ruta")
                }
                FloatingActionButton(onClick = { onOptionSelected(3) }) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar cámara")
                }
                FloatingActionButton(onClick = { onOptionSelected(4) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar ruta")
                }
            }
        }

        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = "Menú"
            )
        }
    }
}