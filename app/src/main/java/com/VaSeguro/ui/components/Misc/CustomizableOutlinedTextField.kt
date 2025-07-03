package com.VaSeguro.ui.components.Misc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.VaSeguro.ui.theme.PrimaryColor

@Composable
fun CustomizableOutlinedTextField(
value: TextFieldValue,
onValueChange: (TextFieldValue) -> Unit,
label: String,
modifier: Modifier = Modifier,
enabled: Boolean = true,
singleLine: Boolean = true
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = PrimaryColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = Color.Gray
        )
    )
}