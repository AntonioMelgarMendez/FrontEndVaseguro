package com.VaSeguro.ui.components.Container.GeneralScaffold

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.remote.Login.UserResponse
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GeneralScaffoldViewModel(
    private val userPreferencesRepository: UserPreferencesRepositoryImpl
) : ViewModel() {
    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    init {
        viewModelScope.launch {
            _user.value = userPreferencesRepository.getUserData()
        }
    }
}