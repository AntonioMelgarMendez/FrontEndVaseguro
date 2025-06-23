package com.VaSeguro.ui.components.Dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Un diálogo de confirmación reutilizable para diferentes acciones que requieren confirmación del usuario.
 *
 * @param isVisible Determina si el diálogo debe mostrarse o no
 * @param title Título del diálogo
 * @param message Mensaje de confirmación
 * @param confirmButtonText Texto del botón de confirmación
 * @param dismissButtonText Texto del botón para cerrar/cancelar el diálogo
 * @param onConfirm Función que se ejecutará cuando el usuario confirme la acción
 * @param onDismiss Función que se ejecutará cuando el usuario cierre o cancele el diálogo
 */
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}
