
package com.VaSeguro.ui.screens.Driver.Chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.components.Chat.ChatBottomBar
import com.VaSeguro.ui.components.Chat.ChatMessagesList
import com.VaSeguro.ui.components.Chat.ChatTopBar
import com.VaSeguro.ui.navigations.ChildrenScreenNavigation
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  navController: NavController,
  id: String,
  viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
  val text by viewModel.text.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val listState = rememberLazyListState()
  val user by viewModel.user.collectAsState()
  val quickReplies = viewModel.quickReplies
  val isLoading by viewModel.isLoading.collectAsState()

  // Load user and chat, connect socket
  LaunchedEffect(id) {
    viewModel.loadUser(id)
    val currentUser = viewModel.userPreferencesRepository.getUserData()
    val currentUserId = currentUser?.id?.toString() ?: return@LaunchedEffect
    viewModel.loadChat(currentUserId, id)
    val token = viewModel.userPreferencesRepository.getAuthToken() ?: return@LaunchedEffect
    viewModel.connectSocket(currentUserId, token)
  }

  if (isLoading) {
    Scaffold(
      topBar = {
        ChatTopBar(
          user = UserData(
            id = "",
            forename = "Loading...",
            surname = "",
            email = "",
            phoneNumber = "",
            profilePic = "",
            role_id = UserRole(id = 0, role_name = "Unknown"),
            gender = ""
          ),
          onBackClick = { navController.navigate(ChildrenScreenNavigation) }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
          .wrapContentSize()
      ) {
        CircularProgressIndicator()
      }
    }
    return
  }

  if (user == null) {
    Scaffold(
      topBar = {
        ChatTopBar(
          user = UserData(
            id = "",
            forename = "Desconocido",
            surname = "",
            email = "",
            phoneNumber = "",
            profilePic = "",
            role_id = UserRole(id = 0, role_name = "Unknown"),
            gender = ""
          ),
          onBackClick = { navController.navigate(ChildrenScreenNavigation) }
        )
      }
    ) { innerPadding ->
      Text(
        text = "Usuario no encontrado",
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
          .wrapContentSize()
      )
    }
    return
  }

  Scaffold(
    bottomBar = {
      ChatBottomBar(
        quickReplies = quickReplies,
        text = text,
        onTextChange = viewModel::onTextChange,
        onSendClick = { viewModel.sendMessage(id) }
      )
    },
    topBar = {
      ChatTopBar(
        user = UserData(
          id = user!!.id.toString(),
          forename = user!!.forenames ?: "",
          surname = user!!.surnames ?: "",
          email = user!!.email ?: "",
          phoneNumber = user!!.phone_number ?: "",
          profilePic = user!!.profile_pic ?: "",
          role_id = UserRole(id = 0, role_name = "Unknown"),
          gender = user!!.gender ?: ""
        ),
        onBackClick = { navController.navigate(ChildrenScreenNavigation) }
      )
    },
  ) { innerPadding ->
    LaunchedEffect(messages.size) {
      listState.animateScrollToItem(messages.size)
    }
    ChatMessagesList(
      messages = messages,
      listState = listState,
      modifier = Modifier.padding(innerPadding)
    )
  }
}