package com.VaSeguro.ui.components.Chat

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.VaSeguro.data.model.Message.Message

@Composable
fun ChatMessagesList(
  messages: List<Message>,
  listState: LazyListState,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF7F7FA))
      .padding(horizontal = 8.dp, vertical = 8.dp)
  ) {
    items(messages) { msg ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
      ) {
        Column(
          horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start,
          modifier = Modifier
            .background(
              color = if (msg.isUser) Color(0xFF645AFF) else Color(0xFFDDDEE3),
              shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (msg.isUser) 0.dp else 16.dp,
                bottomStart = if (msg.isUser) 16.dp else 0.dp
              )
            )
            .padding(12.dp)
            .widthIn(max = 280.dp)
        ) {
          Text(
            text = msg.content,
            color = if (msg.isUser) Color.White else Color(0xFF232323),
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = msg.timestamp,
            color = if (msg.isUser) Color.White else Color.Gray,
            fontSize = 10.sp
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }
  }
}