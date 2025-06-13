package com.VaSeguro.ui.screens.Start.SplashScren

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.io.encoding.Base64

sealed class SplashUiState {
    object Loading : SplashUiState()
    object GoToStarting : SplashUiState()
    object GoToLogin : SplashUiState()
    object GoToHome : SplashUiState()
}

class SplashViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        checkUser()
    }

    private fun checkUser() {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken()
            val user = userPreferencesRepository.getUserData()
            Log.e("SplashViewModel", "Token: $token, User: $user")
            kotlinx.coroutines.delay(1000)
            when {
                isTokenValid(token) && user != null -> {
                    _uiState.value = SplashUiState.GoToHome
                }
                !isTokenValid(token) && user != null -> {
                    _uiState.value = SplashUiState.GoToLogin
                }
                else -> {
                    _uiState.value = SplashUiState.GoToStarting
                }
            }
        }
    }
}