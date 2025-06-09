package com.VaSeguro.ui.components.Container.TopBarContainer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ConfigOption(
    text: String,
    icon: ImageVector,
    backgroundColor: Color = Color(red = 201, green = 191, blue = 196, alpha = 33),
    onClick: (() -> Unit)? = null,
) {
    TextButton(
        onClick = { onClick?.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(8.dp),

    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                icon,
                tint = Black,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text,
                    color = Black,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
