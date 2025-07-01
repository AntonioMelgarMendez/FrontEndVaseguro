package com.VaSeguro.data.model.Chat

data class SendMessageRequest(
    val sender_id: String,
    val receiver_id: String,
    val message: String
)