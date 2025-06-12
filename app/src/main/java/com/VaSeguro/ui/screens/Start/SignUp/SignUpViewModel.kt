
package com.VaSeguro.ui.screens.Start.SignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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
                val response = authRepository.register(
                    forenames = _name.value.trim(),
                    surnames = "", 
                    email = _email.value,
                    password = _password.value,
                    phone_number = _phone.value,
                    gender = "M",
                    role_id = 4
                )

                if (response.token.isNotBlank()) {
                    userPreferencesRepository.saveRegisteredUserData(
                        id = response.user.id,
                        forenames = response.user.forenames,
                        email = response.user.email,
                        phone = response.user.phone_number ?: _phone.value,
                        roleId = response.user.role_id,
                        token = response.token
                    )
                    val savedUser = userPreferencesRepository.getUserData()
                    if (savedUser != null) {
                        onSuccess()
                    } else {
                        _error.value = "Error al guardar los datos del usuario"
                        onError("Error al guardar los datos del usuario")
                    }
                } else {
                    _error.value = "Registro fallido: token vacío"
                    onError("Registro fallido")
                }
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400 -> "Datos inválidos"
                    409 -> "El usuario ya existe"
                    500 -> "Error del servidor"
                    else -> "Error de conexión (${e.code()})"
                }
                onError(_error.value ?: "Error desconocido")
            } catch (e: Exception) {
                _error.value = "Error: ${e.message ?: "Error desconocido"}"
                onError(e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }
}