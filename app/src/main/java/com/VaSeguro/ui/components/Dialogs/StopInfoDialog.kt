package com.VaSeguro.ui.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.Route.RouteType

@Composable
fun StopInfoDialog(
    isVisible: Boolean,
    stopData: StopData?,
    stopPassengers: List<StopPassenger>,
    currentStopStates: Map<Int, Boolean>,
    onDismiss: () -> Unit,
    onStateChanged: (Int, Boolean) -> Unit,
    routeType: RouteType = RouteType.INBOUND // Parámetro por defecto para el tipo de ruta
) {
    if (isVisible && stopData != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Encabezado con nombre de la parada
                    Text(
                        text = stopData.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Información de coordenadas
                    Text(
                        text = "Coordenadas: ${stopData.latitude}, ${stopData.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Separador
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Título de la lista
                    Text(
                        text = "Niños asociados a esta parada:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Lista de niños asociados a esta parada
                    if (stopPassengers.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(stopPassengers) { stopPassenger ->
                                val isCompleted = currentStopStates[stopPassenger.id] ?: false

                                ChildStopItem(
                                    stopPassenger = stopPassenger,
                                    isCompleted = isCompleted,
                                    onStateChanged = { newState ->
                                        onStateChanged(stopPassenger.id, newState)
                                    },
                                    routeType = routeType
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No hay niños asociados a esta parada",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }

                    // Separador
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Botón para cerrar el diálogo
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(text = "Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
fun ChildStopItem(
    stopPassenger: StopPassenger,
    isCompleted: Boolean,
    onStateChanged: (Boolean) -> Unit,
    routeType: RouteType = RouteType.INBOUND // Parámetro por defecto para el tipo de ruta
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del niño
                Text(
                    text = stopPassenger.child.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Tipo de parada basado en el tipo de ruta
                Text(
                    text = when {
                        // Si es ruta INBOUND: HOME -> recoger, INSTITUTION -> dejar
                        routeType == RouteType.INBOUND && stopPassenger.stopType == StopType.HOME ->
                            "Recoger en casa"
                        routeType == RouteType.INBOUND && stopPassenger.stopType == StopType.INSTITUTION ->
                            "Dejar en escuela"
                        // Si es ruta OUTBOUND: INSTITUTION -> recoger, HOME -> dejar
                        routeType == RouteType.OUTBOUND && stopPassenger.stopType == StopType.INSTITUTION ->
                            "Recoger en escuela"
                        routeType == RouteType.OUTBOUND && stopPassenger.stopType == StopType.HOME ->
                            "Dejar en casa"
                        // Caso por defecto
                        else -> "Parada"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Estado actual
                Text(
                    text = if (isCompleted) "Completado" else "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            // Switch para marcar como completado
            Switch(
                checked = isCompleted,
                onCheckedChange = onStateChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
