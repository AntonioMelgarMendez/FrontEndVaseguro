package com.VaSeguro.ui.components.Forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.VaSeguro.ui.theme.SecunrayColorDark

@Composable
fun CustomOutlinedTextField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        label = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(BorderStroke(2.dp, SecunrayColorDark), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecunrayColorDark,
            unfocusedBorderColor = SecunrayColorDark,
            disabledBorderColor = Color.Transparent,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),

    )
}

