package com.VaSeguro.ui.screens.Admin.Account

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.screens.Admin.Children.ChildrenAdminScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountAdminScreenViewModel : ViewModel() {

    private val _account = MutableStateFlow(
        UserData(
            id = "U01",
            forename = "María",
            surname = "López",
            email = "maria.lopez@email.com",
            phoneNumber = "7740-1234",
            profilePic = null,
            role_id = UserRole(id = 1, role_name = "Administrador"),
            gender = "Femenino"
        )
    )
    val account: StateFlow<UserData> = _account.asStateFlow()

    fun updateAccount(newData: UserData) {
        _account.value = newData
    }
}

