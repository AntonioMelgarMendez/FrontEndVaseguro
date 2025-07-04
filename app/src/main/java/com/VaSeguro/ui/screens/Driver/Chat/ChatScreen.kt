package com.VaSeguro.ui.screens.Driver.Chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.components.Chat.ChatBottomBar
import com.VaSeguro.ui.components.Chat.ChatMessagesList
import com.VaSeguro.ui.components.Chat.ChatTopBar
import com.VaSeguro.ui.navigations.ChildrenScreenNavigation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.ui.navigations.CallScreenNavigation
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
  val currentUserId by viewModel.currentUserId.collectAsState()
  val roomName = currentUserId?.let { getRoomName(it, id) } ?: ""
  var currentUser by remember { mutableStateOf<UserResponse?>(null) }

  LaunchedEffect(Unit) {
    currentUser = viewModel.userPreferencesRepository.getUserData()
    Log.d("ChatScreen", "Current user: $currentUser")
  }
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }
  var hasScrolledToBottom by remember { mutableStateOf(false) }
  LaunchedEffect(id) {
    viewModel.loadUser(id)
    val currentUser = viewModel.userPreferencesRepository.getUserData()
    val currentUserId = currentUser?.id?.toString() ?: return@LaunchedEffect
    viewModel.loadChat(currentUserId, id)
    val token = viewModel.userPreferencesRepository.getAuthToken() ?: return@LaunchedEffect
    viewModel.connectSocket(currentUserId, token)
    hasScrolledToBottom = false
  }

  // Only scroll to the bottom on initial load
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty() && !hasScrolledToBottom) {
      listState.scrollToItem(messages.lastIndex)
      hasScrolledToBottom = true
    }
  }

  // Load more messages when scrolled to the top
  LaunchedEffect(listState.firstVisibleItemIndex) {
    if (listState.firstVisibleItemIndex == 0 && messages.isNotEmpty()) {
      viewModel.loadMoreMessages()
    }
  }

  if (isLoading) {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {},
          navigationIcon = {
            IconButton(onClick = { navController.navigate(ChildrenScreenNavigation) }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator()
      }
    }
    return
  }

  if (user == null) {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {},
          navigationIcon = {
            IconButton(onClick = { navController.navigate(ChildrenScreenNavigation) }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
        onBackClick = { navController.navigate(ChildrenScreenNavigation) },
        onCallClick = {
          navController.navigate(
            CallScreenNavigation(
              roomName = roomName,
              id = id,
              personName = (user!!.forenames ?: "") + " " + (user!!.surnames ?: ""),
              personPhotoUrl = user!!.profile_pic,
              callerOneSignalId = currentUser?.onesignal_player_id ?: "",
              calleeOneSignalId = user!!.onesignal_player_id
            )
          )
        }
      )
    },
  ) { innerPadding ->
    ChatMessagesList(
      messages = messages,
      listState = listState,
      modifier = Modifier.padding(innerPadding)
    )
  }
}

fun getRoomName(userId1: String, userId2: String): String {
  return listOf(userId1, userId2).sorted().joinToString("_", prefix = "chat_")
}