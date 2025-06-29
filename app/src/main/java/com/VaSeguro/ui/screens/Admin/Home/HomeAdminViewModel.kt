package com.VaSeguro.ui.screens.Admin.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.RequestRepository.RequestRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.Dao.User.UserDao
import com.VaSeguro.data.Entitys.User.UserEntity
import com.VaSeguro.data.repository.Children.ChildrenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class HomeAdminViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val requestRepository: RequestRepository,
    private val userDao: UserDao,
    private val childrenRepository: ChildrenRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _pendingUsers = MutableStateFlow<List<UserResponse>>(emptyList())
    val pendingUsers: StateFlow<List<UserResponse>> = _pendingUsers
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    val users: StateFlow<List<UserResponse>> = _users

    private val _totalAdmins = MutableStateFlow(0)
    val totalAdmins: StateFlow<Int> = _totalAdmins

    private val _totalDrivers = MutableStateFlow(0)
    val totalDrivers: StateFlow<Int> = _totalDrivers

    private val _totalParents = MutableStateFlow(0)
    val totalParents: StateFlow<Int> = _totalParents

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers

    private val _totalChildren = MutableStateFlow(0)
    val totalChildren: StateFlow<Int> = _totalChildren

    private val CACHE_EXPIRATION_MS = 2 * 60 * 1000L // 2 minutes

    fun fetchChildrenCount() {
        viewModelScope.launch {
            try {
                val token = userPreferencesRepository.getAuthToken() ?: return@launch
                val childrenList = childrenRepository.getChildren(token)
                _totalChildren.value = childrenList.size
            } catch (e: Exception) {
                _totalChildren.value = 0
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            val cached = userDao.getAllUsers()
            var userResponses: List<UserResponse> = emptyList()
            if (cached.isNotEmpty()) {
                userResponses = cached.map { it.toUserResponse() }
            } else {
                val token = userPreferencesRepository.getAuthToken() ?: return@launch
                userResponses = authRepository.getAllUsers(token)
                userDao.clearUsers()
                userDao.insertUsers(userResponses.map { it.toEntity() })
                userPreferencesRepository.setLastUsersFetchTime(System.currentTimeMillis())
            }

            // Fetch children count in the same coroutine
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            val childrenList = try {
                childrenRepository.getChildren(token)
            } catch (e: Exception) {
                emptyList()
            }

            // Update all state at once
            _users.value = userResponses
            _totalAdmins.value = userResponses.count { it.role_id == 2 }
            _totalParents.value = userResponses.count { it.role_id == 3 }
            _totalDrivers.value = userResponses.count { it.role_id == 4 }
            _totalUsers.value = _totalAdmins.value + _totalParents.value + _totalDrivers.value
            _totalChildren.value = childrenList.size
        }
    }

    fun fetchUsersWithCodes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = userPreferencesRepository.getAuthToken() ?: return@launch
                val usersWithCodes = authRepository.getAllUsersWithCodes(token)
                _pendingUsers.value = usersWithCodes
                _isLoading.value = false
            } catch (e: IOException) {
                _errorMessage.value = "No internet connection"
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred"
                _isLoading.value = false
            }
        }
    }

    fun approveUser(userId: Int) {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            requestRepository.sendRequest(token, userId)
            fetchUsersWithCodes()
        }
    }

    fun rejectUser(userId: Int) {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            requestRepository.rejectRequest(token, userId)
            fetchUsersWithCodes()
        }
    }

    private fun UserResponse.toEntity() = UserEntity(
        id = this.id,
        forename = this.forenames,
        surname = this.surnames,
        email = this.email,
        phoneNumber = this.phone_number.toString(),
        profilePic = this.profile_pic,
        roleId = this.role_id,
        gender = this.gender
    )

    private fun UserEntity.toUserResponse() = UserResponse(
        id = this.id,
        forenames = this.forename,
        surnames = this.surname,
        email = this.email,
        phone_number = this.phoneNumber,
        profile_pic = this.profilePic,
        role_id = this.roleId,
        gender = this.gender,
        created_at = "",
        password = "",
    )
}