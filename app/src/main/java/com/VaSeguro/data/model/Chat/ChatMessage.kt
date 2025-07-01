package com.VaSeguro.data.model.Chat

data class ChatMessage(
    val id: String,
    val sender_id: String,
    val receiver_id: String,
    val message: String,
    val created_at: String
)
