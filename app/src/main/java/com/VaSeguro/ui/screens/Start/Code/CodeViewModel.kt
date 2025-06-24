package com.VaSeguro.ui.screens.Start.Code.CodeViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.data.repository.RequestRepository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CodeViewModel(
    private val requestRepository: RequestRepository,
    private val context: Context,
) : ViewModel() {

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var driverId: Int? = null
        private set
    var driverProfilePic: String? = null
        private set

    fun onCodeChange(newCode: String) {
        if (newCode.length <= 6 && newCode.all { it.isLetterOrDigit() }) {
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

                val result = requestRepository.validateCode(code.value)
                if (result.isSuccess) {
                    val userData = result.getOrNull() as? Map<*, *>
                    val driverMap = userData?.get("driver") as? Map<*, *>
                    driverId = (driverMap?.get("id") as? Number)?.toInt() ?: driverMap?.get("id")?.toString()?.toIntOrNull()
                    driverProfilePic = driverMap?.get("profile_pic") as? String
                    if (driverId != null) {
                        DriverPrefs.saveDriverId(context, driverId!!)
                    }
                    Log.d("Driver_info", "Driver ID: $driverId, ProfilePic: $driverProfilePic")
                    onSuccess()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Código inválido"
                    _error.value = errorMsg
                    onError(errorMsg)
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