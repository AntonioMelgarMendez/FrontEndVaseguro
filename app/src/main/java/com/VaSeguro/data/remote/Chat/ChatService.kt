package com.VaSeguro.data.remote.Chat

import com.VaSeguro.data.model.Chat.ChatMessage
import com.VaSeguro.data.model.Chat.SendMessageRequest

import retrofit2.http.*

interface ChatService {
    @GET("chat/{user1Id}/{user2Id}")
    suspend fun getChatBetweenUsers(
        @Path("user1Id") user1Id: String,
        @Path("user2Id") user2Id: String,
        @Header("Authorization") token: String
    ): List<ChatMessage>

    @POST("chat/")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("Authorization") token: String
    ): ChatMessage

    @DELETE("chat/{id}")
    suspend fun deleteMessage(
        @Path("id") id: String,
        @Header("Authorization") token: String
    )
}