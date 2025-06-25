package com.VaSeguro.ui.screens.Driver.Chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.components.Chat.ChatBottomBar
import com.VaSeguro.ui.components.Chat.ChatTopBar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.ui.components.Chat.ChatMessagesList
import com.VaSeguro.ui.navigations.ChatScreenNavigation
import com.VaSeguro.ui.navigations.ChildrenScreenNavigation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
  viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
  val text by viewModel.text.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val listState = rememberLazyListState()
  val user by viewModel.user.collectAsState()
  val quickReplies = viewModel.quickReplies

  if (user == null) {
    Scaffold(
      topBar = {
        ChatTopBar(
          user = UserData(
            id = "0",
            forename = "Desconocido",
            surname = "",
            email = "",
            phoneNumber = "",
            profilePic = "",
            role_id = UserRole(id = 0, role_name = "Unknown"),
            gender = ""
          ),
          onBackClick = {navController.navigate(ChildrenScreenNavigation)}
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
        onSendClick = viewModel::sendMessage
      )
    },
    topBar = {
      ChatTopBar(
        user = user!!,
        onBackClick ={navController.navigate(ChildrenScreenNavigation)}
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

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
  ChatScreen(
    navController = NavController(context = LocalContext.current)
  )
}