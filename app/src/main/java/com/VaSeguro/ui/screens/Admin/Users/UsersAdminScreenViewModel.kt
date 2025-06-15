package com.VaSeguro.ui.screens.Admin.Users

import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class UsersAdminScreenViewModel: ViewModel() {
    private val _users = MutableStateFlow(
        listOf(
            UserData(
                id = "u001",
                forename = "Rebeca",
                surname = "Torres",
                email = "rebeca@ejemplo.com",
                phoneNumber = "77778888",
                profilePic = null,
                role_id = UserRole(2, "Admin"),
                gender = "Female"
            ),
            UserData(
                id = "u002",
                forename = "Diego",
                surname = "Castro",
                email = "diego@ejemplo.com",
                phoneNumber = "66667777",
                profilePic = null,
                role_id = UserRole(1, "User"),
                gender = "Male"
            )
        )
    )
    val users: StateFlow<List<UserData>> = _users

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    fun toggleExpand(userId: String) {
        _expandedMap.update { current ->
            current.toMutableMap().apply {
                this[userId] = !(this[userId] ?: false)
            }
        }
    }

    fun setChecked(userId: String, checked: Boolean) {
        _checkedMap.update { current ->
            current.toMutableMap().apply {
                this[userId] = checked
            }
        }
    }

    fun deleteUser(userId: String) {
        _users.update { current ->
            current.filterNot { it.id == userId }
        }
        _expandedMap.update { it - userId }
        _checkedMap.update { it - userId }
    }

    fun addUser(
        forename: String,
        surname: String,
        email: String,
        phoneNumber: String,
        gender: String
    ) {
        val newUser = UserData(
            id = (1000..9999).random().toString(),
            forename = forename,
            surname = surname,
            email = email,
            phoneNumber = phoneNumber,
            profilePic = null,
            role_id = UserRole(1, "User"),
            gender = gender
        )
        _users.update { it + newUser }
    }
}