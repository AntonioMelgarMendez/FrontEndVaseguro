package com.VaSeguro.ui.screens.Start.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val rememberMe = userPreferencesRepository.rememberMe
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    init {
        loadRememberedEmail()
    }

    private fun loadRememberedEmail() {
        viewModelScope.launch {
            if (rememberMe.first()) {
                _email.value = userPreferencesRepository.getUserEmail() ?: ""
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _error.value = "empty_fields"
                onError("empty_fields")
                _isLoading.value = false
                return@launch
            }

            try {
                val response = authRepository.login(_email.value, _password.value)
                if (response.token.isNotBlank()) {
                    userPreferencesRepository.saveAuthToken(response.token)
                    userPreferencesRepository.saveUserData(response.user)

                    if (rememberMe.value) {
                        userPreferencesRepository.saveUserEmail(response.user.email)
                        userPreferencesRepository.saveRememberMePreference(true)
                    }

                    onSuccess()
                } else {
                    _error.value = "Respuesta inválida del servidor"
                    onError("invalid_response")
                }
            } catch (e: HttpException) {
                val code = e.code().toString()
                _error.value = code
                onError(code)
            } catch (e: Exception) {
                _error.value = "network_error"
                onError("network_error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearUserData()
        }
    }

    fun setRememberMe(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveRememberMePreference(value)
        }
    }
    fun setError(errorCode: String?) {
        _error.value = errorCode
    }
    fun clearFields() {
        _email.value = ""
        _password.value = ""
        _error.value = null
    }
}