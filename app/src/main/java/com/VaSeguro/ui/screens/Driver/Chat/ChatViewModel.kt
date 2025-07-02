package com.VaSeguro.ui.screens.Driver.Chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.TagFaces
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Chat.SendMessageRequest
import com.VaSeguro.data.model.Message.Message
import com.VaSeguro.data.model.QuickReply
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.ChatRepository.ChatRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
  private val chatRepository: ChatRepository,
  private val authRepository: AuthRepository,
  internal val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: StateFlow<List<Message>> = _messages

  private val _text = MutableStateFlow("")
  val text: StateFlow<String> = _text

  private val _user = MutableStateFlow<UserResponse?>(null)
  val user: StateFlow<UserResponse?> = _user

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val _currentUserId = MutableStateFlow<String?>(null)
  val currentUserId: StateFlow<String?> = _currentUserId

  private var allMessages: List<Message> = emptyList()
  private var visibleCount = 10

  // Prevent multiple socket/listener registrations
  private var isSocketConnected = false

  init {
    viewModelScope.launch {
      val user = userPreferencesRepository.getUserData()
      _currentUserId.value = user?.id?.toString()
    }
  }

  private fun formatTimestamp(raw: String): String {
    val formats = listOf(
      "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in formats) {
      try {
        val parser = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
        val date = parser.parse(raw)
        if (date != null) {
          val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
          return formatter.format(date)
        }
      } catch (_: Exception) { }
    }
    return raw
  }

  val quickReplies: List<QuickReply> = listOf(
    QuickReply(
      text = "I'm here!",
      icon = Icons.Filled.CheckCircle,
      onClick = { onTextChange("I'm here!") }
    ),
    QuickReply(
      text = "Wait a little!",
      icon = Icons.Filled.HourglassEmpty,
      onClick = { onTextChange("Wait a little!") }
    ),
    QuickReply(
      text = "Can't see you rn lol!",
      icon = Icons.Filled.VisibilityOff,
      onClick = { onTextChange("Can't see you rn lol!") }
    ),
    QuickReply(
      text = "Holaaaa",
      icon = Icons.Filled.TagFaces,
      onClick = { onTextChange("Holaaaa") }
    )
  )

  fun loadUser(userId: String) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val token = userPreferencesRepository.getAuthToken()
        if (token != null) {
          val user = authRepository.getUserById(userId.toInt(), token)
          _user.value = user
        } else {
          _user.value = null
        }
      } catch (e: Exception) {
        e.printStackTrace()
        _user.value = null
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun loadChat(user1Id: String, user2Id: String) {
    viewModelScope.launch {
      val token = userPreferencesRepository.getAuthToken() ?: return@launch
      try {
        val chat = chatRepository.getChatBetweenUsers(user1Id, user2Id, token)
        allMessages = chat.map {
          Message(
            id = it.id.toLong(),
            content = it.message,
            isUser = it.sender_id == user1Id,
            timestamp = formatTimestamp(it.created_at)
          )
        }
        updateVisibleMessages()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun updateVisibleMessages() {
    val fromIndex = (allMessages.size - visibleCount).coerceAtLeast(0)
    _messages.value = allMessages.subList(fromIndex, allMessages.size)
  }

  fun loadMoreMessages() {
    if (visibleCount < allMessages.size) {
      visibleCount += 10
      updateVisibleMessages()
    }
  }

  fun onTextChange(newText: String) {
    _text.value = newText
  }

  fun sendMessage(receiverId: String) {
    val currentText = _text.value.trim()
    if (currentText.isNotBlank()) {
      viewModelScope.launch {
        val token = userPreferencesRepository.getAuthToken() ?: return@launch
        try {
          val request = SendMessageRequest(
            receiver_id = receiverId,
            message = currentText,
            sender_id = userPreferencesRepository.getUserData()?.id.toString()
          )
          val sentMessage = chatRepository.sendMessage(request, token)
          _messages.value = _messages.value + Message(
            id = sentMessage.id.toLong(),
            content = sentMessage.message,
            isUser = true,
            timestamp = formatTimestamp(sentMessage.created_at)
          )
          _text.value = ""
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun connectSocket(userId: String, token: String) {
    if (isSocketConnected) return
    isSocketConnected = true
    chatRepository.connectWebSocket(userId, token)
    chatRepository.setOnMessageReceivedListener { chatMessage ->
      _messages.value = _messages.value + Message(
        id = chatMessage.id.toLong(),
        content = chatMessage.message,
        isUser = false,
        timestamp = formatTimestamp(chatMessage.created_at)
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    chatRepository.disconnectWebSocket()
    isSocketConnected = false
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        try {
          val application = this[APPLICATION_KEY] as MyApplication
          ChatViewModel(
            application.appProvider.provideChatRepository(),
            application.appProvider.provideAuthRepository(),
            application.appProvider.provideUserPreferences()
          )
        } catch (e: Exception) {
          e.printStackTrace()
          throw e
        }
      }
    }
  }
}