package com.VaSeguro.data.repository.ChatRepository

import com.VaSeguro.data.model.Chat.ChatMessage
import com.VaSeguro.data.model.Chat.SendMessageRequest
import com.VaSeguro.data.remote.Chat.ChatService
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatRepositoryImpl(
  private val chatService: ChatService,
  private val webSocketUrl: String
) : ChatRepository {

  private var socket: Socket? = null
  private var onMessageReceived: ((ChatMessage) -> Unit)? = null

  private fun bearerToken(token: String) = "Bearer $token"

  override suspend fun getChatBetweenUsers(user1Id: String, user2Id: String, token: String): List<ChatMessage> {
    return chatService.getChatBetweenUsers(user1Id, user2Id, bearerToken(token))
  }

  override suspend fun sendMessage(request: SendMessageRequest, token: String): ChatMessage {
    return chatService.sendMessage(request, bearerToken(token))
  }

  override suspend fun deleteMessage(id: String, token: String) {
    chatService.deleteMessage(id, bearerToken(token))
  }

  override fun connectWebSocket(userId: String, token: String) {
    val opts = IO.Options()
    opts.query = "token=$token"
    socket = IO.socket(webSocketUrl, opts)

    socket?.on(Socket.EVENT_CONNECT) {
      socket?.emit("join", userId)
    }

    socket?.on("newMessage") { args ->
      val data = args[0] as JSONObject
      val message = ChatMessage(
        id = data.getLong("id").toString(),
        sender_id = data.getString("sender_id"),
        receiver_id = data.getString("receiver_id"),
        message = data.getString("message"),
        created_at = data.getString("created_at")
      )
      if (message.sender_id != userId) {
        onMessageReceived?.invoke(message)
      }
    }

    socket?.connect()
  }

  override fun disconnectWebSocket() {
    socket?.disconnect()
    socket = null
  }

  override fun setOnMessageReceivedListener(listener: (ChatMessage) -> Unit) {
    onMessageReceived = listener
  }
}