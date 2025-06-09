
package com.VaSeguro.ui.screens.Start.SignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPhoneChange(newPhone: String) {
        _phone.value = newPhone
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val forenames = _name.value.trim()
                val surnames = ""
                val response = authRepository.register(
                    forenames = forenames,
                    surnames = surnames,
                    email = _email.value,
                    password = _password.value,
                    phone_number = _phone.value,
                    gender = "M",
                    role_id = 2
                )
                if (response.token.isNotBlank()) {
                    onSuccess()
                } else {
                    _error.value = "Registro fallido"
                    onError("Registro fallido")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message ?: "Error desconocido"}"
                onError(e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }
}