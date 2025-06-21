package com.VaSeguro.ui.screens.Driver.Chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Message.Message
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.repository.ChatRepository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(
  private val chatRepository: ChatRepository
) : ViewModel() {

  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: StateFlow<List<Message>> = _messages

  private val _text = MutableStateFlow("")
  val text: StateFlow<String> = _text

  private val _user = MutableStateFlow<UserData?>(null)
  val user: StateFlow<UserData?> = _user

  val quickReplies: List<QuickReply> = listOf(
    QuickReply("I'm here!") { onTextChange("I'm here!") },
    QuickReply("Wait a little!") { onTextChange("Wait a little!") },
    QuickReply("Can't see you rn lol!") { onTextChange("Can't see you rn lol!") },
    QuickReply("Holaaaa") { onTextChange("Holaaaa") }
  )

  init {
    loadUser()
  }

  private fun loadUser() {
    val loadedUser = UserData(
      id = "1",
      forename = "Walter",
      surname = "Ramirez",
      email = "walter@example.com",
      phoneNumber = "+50312345678",
      profilePic = "https://static.wikia.nocookie.net/rhythmheaven/images/8/84/Super_Monkey.png/revision/latest?cb=20161030151552",
      role_id = UserRole(id = 2, role_name = "Parent"),
      gender = "male"
    )
    _user.value = loadedUser
  }

  fun onTextChange(newText: String) {
    _text.value = newText
  }

  fun sendMessage() {
    val currentText = _text.value.trim()
    if (currentText.isNotBlank()) {
      val newMessage = Message(
        id = System.currentTimeMillis(),
        content = currentText,
        isUser = true,
        timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
      )
      _messages.value = _messages.value + newMessage
      _text.value = ""
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        try {
          val application = this[APPLICATION_KEY] as MyApplication
          ChatViewModel(
            application.appProvider.provideChatRepository()
          )
        } catch (e: Exception) {
          e.printStackTrace()
          throw e
        }
      }
    }
  }
}