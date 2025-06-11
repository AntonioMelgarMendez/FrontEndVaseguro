package com.VaSeguro.ui.screens.Admin.Users

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UsersAdminScreenViewModel: ViewModel() {
    val role1 = UserRole(2, "User")

    private val _users = MutableStateFlow(
        listOf(

            UserData("U01", "Luis", "Ramírez", "luis@example.com", "71551122", role_id = role1 ,gender = "Male"),
            UserData("U02", "Laura", "Gómez", "laura@example.com", "71443300", role_id = role1, gender = "Female"),
        )
    )
    val users: StateFlow<List<UserData>> = _users

    fun addUser(
        forename: String,
        surname: String,
        email: String,
        phone: String,
        gender: String
    ) {
        val newUser = UserData(
            id = System.currentTimeMillis().toString().takeLast(5),
            forename = forename,
            surname = surname,
            email = email,
            phoneNumber = phone,
            gender = gender,
            profilePic = null,
            role_id = role1
        )
        _users.value = _users.value + newUser
    }

    fun deleteUser(id: String) {
        _users.value = _users.value.filterNot { it.id == id }
    }
}