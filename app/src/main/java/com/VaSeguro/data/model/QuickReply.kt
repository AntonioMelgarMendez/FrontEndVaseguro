package com.VaSeguro.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class QuickReply(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)