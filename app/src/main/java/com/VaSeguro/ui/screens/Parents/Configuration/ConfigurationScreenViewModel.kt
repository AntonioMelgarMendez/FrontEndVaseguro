package com.VaSeguro.ui.screens.Parents.Configuration

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.Security.SecurityFormState
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class ConfigurationScreenViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private fun userResponseToUserData(user: UserResponse): UserData {
        return UserData(
            id = user.id.toString(),
            forename = user.forenames,
            surname = user.surnames,
            email = user.email,
            phoneNumber = user.phone_number ?: "",
            profilePic = user.profile_pic,
            role_id = UserRole(user.role_id, "User"),
            gender = user.gender ?: ""
        )
    }
    private var currentUser = UserData(
        id = "",
        forename = "",
        surname = "",
        email = "",
        phoneNumber = "",
        profilePic = null,
        role_id = UserRole(0, ""),
        gender = ""
    )
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    val originalUserData: UserData
        get() = currentUser

    val originalSecurityState = SecurityFormState()

    private val _userData = MutableStateFlow(currentUser)
    val userData: StateFlow<UserData> = _userData

    private val initialSecurity = SecurityFormState()
    private val _securityState = MutableStateFlow(initialSecurity)
    val securityState: StateFlow<SecurityFormState> = _securityState

    init {
        viewModelScope.launch {
            val user = userPreferencesRepository.getUserData()
            user?.let {
                val mappedUser = userResponseToUserData(it)
                currentUser = mappedUser
                _userData.value = mappedUser
            }
        }
    }

    fun onUserFieldChange(update: (UserData) -> UserData) {
        _userData.value = update(_userData.value)
    }

    fun onSecurityFieldChange(update: (SecurityFormState) -> SecurityFormState) {
        val updated = update(_securityState.value)
        _securityState.value = updated.copy(
            isMinLengthValid = updated.newPassword.length >= 8,
            isCaseValid = updated.newPassword.any { it.isUpperCase() } && updated.newPassword.any { it.isLowerCase() },
            isSpecialCharValid = updated.newPassword.any { !it.isLetterOrDigit() }
        )
    }

    @SuppressLint("UseKtx")
    fun onUpdateAccount(context: android.content.Context) {
        val updatedUser = _userData.value
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken() ?: ""
                val profilePicPart = updatedUser.profilePic?.let { uriString ->
                    if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                        val uri = uriString.toUri()
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bitmap == null) {
                            Log.d("No hay imagen", "No se pudo leer la imagen del URI: $uri")
                            null
                        } else {
                            var quality = 90
                            var bytes: ByteArray
                            do {
                                val outputStream = java.io.ByteArrayOutputStream()
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
                                bytes = outputStream.toByteArray()
                                quality -= 10
                            } while (bytes.size > 1_000_000 && quality > 10)
                            if (bytes.isNotEmpty()) {
                                val requestBody = okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
                                okhttp3.MultipartBody.Part.createFormData("profile_pic", "profile.jpg", requestBody)
                            } else null
                        }
                    } else {
                        null
                    }
                }

                val response = authRepository.updateUser(
                    userId = updatedUser.id.toInt(),
                    forenames = updatedUser.forename,
                    surnames = updatedUser.surname,
                    email = updatedUser.email,
                    phone_number = updatedUser.phoneNumber,
                    gender = updatedUser.gender.toString(),
                    profile_pic = profilePicPart,
                    token = token
                )
                userPreferencesRepository.saveUserData(response)
                currentUser = updatedUser
                Log.d("ConfigurationVM", "Account updated in backend: $response")
                _updateSuccess.value = true
            } catch (e: Exception) {
                Log.e("ConfigurationVM", "Failed to update account", e)
                _updateSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun onUpdatePassword() {
        val state = _securityState.value
        val isValid = state.newPassword == state.confirmPassword &&
                state.isMinLengthValid &&
                state.isCaseValid &&
                state.isSpecialCharValid

        if (isValid) {
            Log.d("ConfigurationVM", "Password updated successfully")
        } else {
            Log.d("ConfigurationVM", "Password validation failed")
        }
        _securityState.value = SecurityFormState()
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = null
    }
    fun onCancelChanges() {
        _userData.value = currentUser
        _securityState.value = initialSecurity
    }
    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken() ?: ""
                val userId = currentUser.id.toInt()
                val success = authRepository.changePassword(
                    userId = userId,
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    token = token
                )
                if (success) {
                    Log.d("ConfigurationVM", "Password changed successfully")
                    _updateSuccess.value = true
                } else {
                    Log.e("ConfigurationVM", "Failed to change password")
                    _updateSuccess.value = false
                }
            } catch (e: Exception) {
                Log.e("ConfigurationVM", "Failed to change password", e)
                _updateSuccess.value = false
            } finally {
                _isLoading.value = false
                _securityState.value = SecurityFormState()
            }
        }
    }
}