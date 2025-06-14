package com.VaSeguro.ui.screens.Admin.Home



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// HomeAdminViewModel.kt
class HomeAdminViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _pendingUsers = MutableStateFlow<List<UserResponse>>(emptyList())
    val pendingUsers: StateFlow<List<UserResponse>> = _pendingUsers
    val isLoading: StateFlow<Boolean> = _isLoading

    val users: StateFlow<List<UserResponse>> = _users

    private val _totalAdmins = MutableStateFlow(0)
    val totalAdmins: StateFlow<Int> = _totalAdmins

    private val _totalDrivers = MutableStateFlow(0)
    val totalDrivers: StateFlow<Int> = _totalDrivers

    private val _totalParents = MutableStateFlow(0)
    val totalParents: StateFlow<Int> = _totalParents

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers

    private val _totalChildren = MutableStateFlow(8)
    val totalChildren: StateFlow<Int> = _totalChildren

    fun fetchUsers() {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            val users: List<UserResponse> = authRepository.getAllUsers(token)
            _users.value = users
            _totalAdmins.value = users.count { it.role_id == 2 }
            _totalParents.value = users.count { it.role_id == 3 }
            _totalDrivers.value = users.count { it.role_id == 4 }
            _totalUsers.value = _totalAdmins.value + _totalParents.value + _totalDrivers.value
            // _totalChildren.value remains hardcoded
        }
    }
    fun fetchUsersWithCodes() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            val usersWithCodes: List<UserResponse> = authRepository.getAllUsersWithCodes(token)
            _pendingUsers.value = usersWithCodes
            _isLoading.value = false
        }
    }
}