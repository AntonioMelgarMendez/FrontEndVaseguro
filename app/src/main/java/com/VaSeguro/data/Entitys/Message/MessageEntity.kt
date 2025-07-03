package com.VaSeguro.data.Entitys.Message

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val content: String,
    val isUser: Boolean = true,
    val timestamp: String,
    val senderId: String,  // ID del remitente
    val receiverId: String // ID del receptor
)
