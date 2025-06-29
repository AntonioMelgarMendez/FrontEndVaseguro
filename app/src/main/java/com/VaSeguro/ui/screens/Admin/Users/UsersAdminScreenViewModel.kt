package com.VaSeguro.ui.screens.Admin.Users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.Dao.User.UserDao
import com.VaSeguro.data.Entitys.User.UserEntity
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userDao:UserDao
) : ViewModel() {
    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val CACHE_EXPIRATION_MS = 1 * 60 * 1000L

    fun fetchAllUsers() {
        viewModelScope.launch {
            _loading.value = true
            // 1. Leer de Room
            val cached = userDao.getAllUsers()
            if (cached.isNotEmpty()) {
                _users.value = cached.map { it.toUserData() }
            }

            // 2. Verificar expiración de caché
            val lastFetch = userPreferencesRepository.getLastUsersFetchTime()
            val now = System.currentTimeMillis()
            if (lastFetch != null && now - lastFetch < CACHE_EXPIRATION_MS) {
                _loading.value = false
                return@launch
            }

            // 3. Llamar a la API y actualizar Room
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                val userResponses = authRepository.getAllUsers(token)
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
                userDao.clearUsers()
                userDao.insertUsers(userResponses.map { it.toEntity() })
                userPreferencesRepository.setLastUsersFetchTime(now)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    private fun UserResponse.toEntity() = UserEntity(
        id = this.id,
        forename = this.forenames,
        surname = this.surnames,
        email = this.email,
        phoneNumber = this.phone_number ?: "",
        profilePic = this.profile_pic,
        roleId = this.role_id,
        gender = this.gender
    )

    // Conversión de UserEntity a UserData
    private fun UserEntity.toUserData() = UserData(
        id = this.id.toString(),
        forename = this.forename,
        surname = this.surname,
        email = this.email,
        phoneNumber = this.phoneNumber,
        profilePic = this.profilePic,
        role_id = UserRole(this.roleId, ""),
        gender = this.gender
    )
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