package com.VaSeguro.data.repository.ChatRepository

import com.VaSeguro.data.remote.Chat.ChatService

class ChatRepositoryImpl (
  private val chatService: ChatService
) : ChatRepository{
}