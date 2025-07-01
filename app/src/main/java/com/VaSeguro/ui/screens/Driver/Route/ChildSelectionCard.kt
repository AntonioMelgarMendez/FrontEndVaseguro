package com.VaSeguro.ui.screens.Driver.Route

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap

@Composable
fun ChildSelectionCard(
    child: ChildMap,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    isEnabled: Boolean = true
) {
    // Haptic feedback para mejorar experiencia táctil
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = isEnabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSelection()
                }
            )
            .alpha(if (isEnabled) 1f else 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono o avatar con estado visual de selección
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del niño
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = child.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Información adicional como ID o grupo
                Text(
                    text = "ID: ${child.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Icono explícito de selección para mejor feedback visual
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Seleccionado",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isEnabled) 1f else 0.5f)
                    )
                }
            }
        }
    }
}
