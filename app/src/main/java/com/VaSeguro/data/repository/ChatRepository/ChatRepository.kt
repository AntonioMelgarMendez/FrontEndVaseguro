package com.VaSeguro.data.repository.ChatRepository

import com.VaSeguro.data.model.Chat.ChatMessage
import com.VaSeguro.data.model.Chat.SendMessageRequest

interface ChatRepository {
    suspend fun getChatBetweenUsers(user1Id: String, user2Id: String, token: String): List<ChatMessage>
    suspend fun sendMessage(request: SendMessageRequest, token: String): ChatMessage
    suspend fun deleteMessage(id: String, token: String)
    fun connectWebSocket(userId: String, token: String)
    fun disconnectWebSocket()
    fun setOnMessageReceivedListener(listener: (ChatMessage) -> Unit)
}

