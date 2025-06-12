package com.VaSeguro.ui.components.Container.GeneralScaffold

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GeneralScaffoldViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _role = MutableStateFlow(0)
    val role: StateFlow<Int> = _role

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            val user = userPreferencesRepository.getUserData()

            val gson = Gson()
            val userJson = gson.toJson(user)
            Log.d("UserData", "User JSON: $userJson")

            _role.value = user?.role_id ?: 0
        }
    }
}