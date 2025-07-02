package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.Call.StartCallRequest
import com.VaSeguro.data.remote.RetrofitInstance
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {
    var callId by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun createCall(callerId: String, calleeId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                Log.d("CallViewModel", "Creating call with callerId: $callerId, calleeId: $calleeId")
                val response = RetrofitInstance.callService.createCall(callerId, calleeId)
                callId = response.id
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
}