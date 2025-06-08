package com.VaSeguro.ui.screens.Start.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (_email.value == "user@example.com" && _password.value == "password") {
                onSuccess()
            } else {
                onError("Correo o contraseña incorrectos.")
            }
        }
    }
}