package com.VaSeguro.ui.screens.Start.Recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var email by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    fun onEmailChange(newEmail: String) { email = newEmail }

    fun recoverPassword() {
        viewModelScope.launch {
            isLoading = true
            error = null
            message = null
            try {
                authRepository.recoverPassword(email)
                message = "Si el correo existe, se ha enviado un enlace de recuperación."
            } catch (e: Exception) {
                error = "No se pudo enviar el correo de recuperación."
            } finally {
                isLoading = false
            }
        }
    }
}