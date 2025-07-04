package com.VaSeguro.data.repository.ChatRepository

import com.VaSeguro.data.Dao.Message.MessageDao
import com.VaSeguro.data.Entitys.Message.MessageEntity
import com.VaSeguro.data.model.Chat.ChatMessage
import com.VaSeguro.data.model.Chat.SendMessageRequest
import com.VaSeguro.data.model.Chat.toEntity
import com.VaSeguro.data.model.Message.toChatMessage
import com.VaSeguro.data.remote.Chat.ChatService
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatRepositoryImpl(
  private val chatService: ChatService,
  private val messageDao: MessageDao,
  private val webSocketUrl: String
) : ChatRepository {

  private var socket: Socket? = null
  private var onMessageReceived: ((ChatMessage) -> Unit)? = null

  private fun bearerToken(token: String) = "Bearer $token"

//  override suspend fun getChatBetweenUsers(user1Id: String, user2Id: String, token: String): List<ChatMessage> {
//    return chatService.getChatBetweenUsers(user1Id, user2Id, bearerToken(token))
//  }

  override suspend fun getChatBetweenUsers(user1Id: String, user2Id: String, token: String): List<ChatMessage> {
    // Primero intentamos obtener los mensajes desde Room
    val localMessages = messageDao.getMessagesBetweenUsers(user1Id, user2Id).map { it.toChatMessage() }

    if (localMessages.isNotEmpty()) {
      return localMessages
    } else {
      // Si no hay mensajes en Room, obtenemos desde la API
      val remoteMessages = chatService.getChatBetweenUsers(user1Id, user2Id, bearerToken(token))
      // Guardamos los mensajes en Room
      remoteMessages.forEach { chatMessage ->
        val messageEntity = chatMessage.toEntity()  // Convertimos el ChatMessage a MessageEntity
        messageDao.insertMessage(messageEntity)  // Insertar en Room
      }
      return remoteMessages
    }
  }

//  override suspend fun sendMessage(request: SendMessageRequest, token: String): ChatMessage {
//    return chatService.sendMessage(request, bearerToken(token))
//  }

  override suspend fun sendMessage(request: SendMessageRequest, token: String): ChatMessage {
    val sentMessage = chatService.sendMessage(request, bearerToken(token))

    // Guardamos el mensaje en Room
    val messageEntity = sentMessage.toEntity()  // Convertimos el ChatMessage a MessageEntity
    messageDao.insertMessage(messageEntity)

    return sentMessage
  }

//  override suspend fun deleteMessage(id: String, token: String) {
//    chatService.deleteMessage(id, bearerToken(token))
//  }

  override suspend fun deleteMessage(id: String, token: String) {
    // Eliminar el mensaje desde la API
    chatService.deleteMessage(id, bearerToken(token))

    // Eliminar el mensaje de Room
    messageDao.deleteMessageById(id.toLong())
  }

  // Método para insertar un mensaje en Room
  private suspend fun insertMessageInRoom(message: MessageEntity) {
    // Llamamos a la función suspend insertMessage del DAO dentro de una función suspend
    messageDao.insertMessage(message)
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

      // Guardamos el nuevo mensaje en Room
      val messageEntity = message.toEntity()
      CoroutineScope(Dispatchers.IO).launch {
        insertMessageInRoom(messageEntity)
      }

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