package com.VaSeguro.data.model.Chat

import com.VaSeguro.data.Entitys.Message.MessageEntity

data class ChatMessage(
    val id: String,
    val sender_id: String,
    val receiver_id: String,
    val message: String,
    val created_at: String
)

fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        id = this.id.toLong(),
        content = this.message,
        isUser = this.sender_id == "user",  // Aquí debes adaptar la lógica para determinar si el mensaje es del usuario o no
        timestamp = this.created_at,
        senderId = this.sender_id,
        receiverId = this.receiver_id
    )
}
