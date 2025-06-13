package com.VaSeguro.ui.components.Container.TopBarContainer

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepositoryImpl
import kotlinx.coroutines.launch

class TopBarViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    var isConfigDialogOpen by mutableStateOf(false)
        private set

    fun openConfigDialog() {
        isConfigDialogOpen = true
    }

    fun closeConfigDialog() {
        isConfigDialogOpen = false
    }

    fun logout(context: Context, onLogout: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.clearUserData()
            onLogout()
        }
    }
}
