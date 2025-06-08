package com.VaSeguro.ui.screens.Start.SignUp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

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
        if (
            _name.value.isNotBlank() &&
            _email.value.isNotBlank() &&
            _phone.value.isNotBlank() &&
            _password.value.length >= 6
        ) {
            onSuccess()
        } else {
            onError("Todos los campos son obligatorios y la contraseña debe tener al menos 6 caracteres.")
        }
    }
}
