package com.VaSeguro.ui.screens.Start.Code.CodeViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onCodeChange(newCode: String) {
        if (newCode.length <= 6 && newCode.all { it.isDigit() }) {
            _code.value = newCode
        }
    }

    fun verifyCode(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (code.value.length != 6) {
                    _error.value = "El código debe tener 6 dígitos"
                    return@launch
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.message ?: "Error desconocido"}"
                onError(_error.value!!)
            } finally {
                _isLoading.value = false
            }
        }
    }
}