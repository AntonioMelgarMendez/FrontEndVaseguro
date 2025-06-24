package com.VaSeguro.ui.screens.Start.Recovery.Code

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.launch

class EmailCodeViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {
    var step by mutableStateOf(0)
    var email by mutableStateOf("")
    var code by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun sendEmail() {
        isLoading = true
        error = null
        viewModelScope.launch {
            val result = authRepository.recoverPassword(email)
            if (result.isSuccess) {
                step = 1
            } else {
                error = result.exceptionOrNull()?.message ?: "Error sending email"
            }
            isLoading = false
        }
    }

    fun verifyCode() {
        isLoading = true
        error = null
        viewModelScope.launch {
            if (code.length == 6) {
                step = 2
            } else {
                error = "Invalid code"
            }
            isLoading = false
        }
    }

    fun resetPassword() {
        isLoading = true
        error = null
        viewModelScope.launch {
            val result = authRepository.verifyResetCode(email, code, password)
            if (result.isSuccess) {
                step = 3
            } else {
                error = result.exceptionOrNull()?.message ?: "Password reset failed"
            }
            isLoading = false
        }
    }
}