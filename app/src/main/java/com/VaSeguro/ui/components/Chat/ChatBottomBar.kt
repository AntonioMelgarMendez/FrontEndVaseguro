package com.VaSeguro.ui.components.Chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.QuickReply

@Composable
fun ChatBottomBar(
  quickReplies: List<QuickReply>,
  text: String,
  onTextChange: (String) -> Unit,
  onSendClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White)
      .padding(8.dp)
  ) {
    LazyRow(
      modifier = Modifier.padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(quickReplies) { reply ->
        OutlinedButton(
          onClick = reply.onClick,
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(1.dp, Color.Transparent),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFF2F2F2),
            contentColor = Color(0xFF645AFF)
          ),
          modifier = Modifier
            .background(Color(0xFFF2F2F2), RoundedCornerShape(24.dp))
        ) {
          Icon(
            imageVector = reply.icon,
            contentDescription = null,
            tint = Color(0xFF645AFF)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(reply.text, color = Color.DarkGray)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(Color(0xFFF2F2F2), RoundedCornerShape(24.dp))
          .padding(horizontal = 12.dp) // Remove vertical padding
      ) {
        TextField(
          value = text,
          onValueChange = onTextChange,
          placeholder = { Text("Escribe un mensaje", color = Color.Black) },
          maxLines = 1,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.White
          )
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      IconButton(onClick = onSendClick) {
        Icon(
          Icons.Default.Send,
          contentDescription = "Send",
          tint = Color(0xFF645AFF)
        )
      }
    }
  }
}