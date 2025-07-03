package com.VaSeguro.data.model.Message

import com.VaSeguro.data.Entitys.Message.MessageEntity
import com.VaSeguro.data.model.Chat.ChatMessage

data class Message(
  val id: Long = System.currentTimeMillis(),
  val content: String,
  val isUser: Boolean = true,
  val timestamp: String
)

fun MessageEntity.toChatMessage(): ChatMessage {
  return ChatMessage(
    id = this.id.toString(),
    sender_id = this.senderId,
    receiver_id = this.receiverId,
    message = this.content,
    created_at = this.timestamp
  )
}