package com.VaSeguro.ui.screens.Start.Recovery.Code

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SixDigitCodeInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    val focusRequesters = List(6) { remember { FocusRequester() } }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        for (i in 0 until 6) {
            val char = code.getOrNull(i)?.toString() ?: ""
            OutlinedTextField(
                value = char,
                onValueChange = { value ->
                    if (value.length <= 1 && value.all { it.isDigit() }) {
                        val newCode = buildString {
                            append(code.padEnd(6, ' '))
                            setCharAt(i, value.getOrElse(0) { ' ' })
                        }.replace(" ", "")
                        onCodeChange(newCode)
                        if (value.isNotEmpty() && i < 5) {
                            focusRequesters[i + 1].requestFocus()
                        }
                    }
                    if (value.isEmpty() && i > 0) {
                        focusRequesters[i - 1].requestFocus()
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[i]),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                )
            )
        }
    }
}