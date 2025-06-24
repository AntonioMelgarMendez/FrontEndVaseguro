package com.VaSeguro.ui.screens.Admin.Users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class UsersAdminScreenViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchAllUsers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userResponses: List<UserResponse> = authRepository.getAllUsers(
                    userPreferencesRepository.getAuthToken().toString()
                )
                val userDataList = userResponses.map {
                    UserData(
                        id = it.id.toString(),
                        forename = it.forenames,
                        surname = it.surnames,
                        email = it.email,
                        phoneNumber = it.phone_number ?: "",
                        profilePic = it.profile_pic,
                        role_id = UserRole(0, ""),
                        gender = it.gender
                    )
                }
                _users.value = userDataList
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().toString()
                authRepository.deleteAccount(userId.toInt(), token)
                fetchAllUsers()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
        _expandedMap.update { it - userId }
        _checkedMap.update { it - userId }
    }

    fun updateUser(
        userId: String,
        forename: String,
        surname: String,
        email: String,
        phoneNumber: String,
        gender: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().toString()
                authRepository.updateUser(
                    userId = userId.toInt(),
                    forenames = forename,
                    surnames = surname,
                    email = email,
                    phone_number = phoneNumber,
                    gender = gender,
                    profile_pic = null,
                    token = token
                )
                fetchAllUsers()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

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

    fun addUser(
        forename: String,
        surname: String,
        email: String,
        password: String,
        phoneNumber: String,
        gender: String,
        roleId: Int = 1,
        profilePic: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                authRepository.register(
                    forenames = forename,
                    surnames = surname,
                    email = email,
                    password = password,
                    phone_number = phoneNumber,
                    gender = gender,
                    role_id = roleId,
                    profile_pic = profilePic
                )
                fetchAllUsers()
            } catch (e: Exception) {
            } finally {
                _loading.value = false
            }
        }
    }
}