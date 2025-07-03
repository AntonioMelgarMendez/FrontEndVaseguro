package com.VaSeguro.ui.components.Container.TopBarContainer

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.Dao.DriverCode.DriverCodeDao
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.RequestRepository.RequestRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TopBarViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val requestRepository: RequestRepository,
    private val driverCodeDao: DriverCodeDao
) : ViewModel() {
    var isConfigDialogOpen by mutableStateOf(false)
        private set
    var userProfilePic by mutableStateOf<String?>(null)
        private set
    var userRoleId by mutableStateOf<Int?>(null)
        private set
    var driverCode by mutableStateOf<String?>(null)
        private set
    var userForename by mutableStateOf<String?>(null)
        private set
    var userSurname by mutableStateOf<String?>(null)
        private set
    var isDriverCodeLoading by mutableStateOf(false)
        private set
    init {
        viewModelScope.launch {
            userPreferencesRepository.userDataFlow().collectLatest { user ->
                userProfilePic = user?.profile_pic?.takeIf { it.isNotBlank() }
                userRoleId = user?.role_id
                userForename = user?.forenames
                userSurname = user?.surnames
            }
        }
    }

    fun openConfigDialog() {
        isConfigDialogOpen = true
    }

    fun closeConfigDialog() {
        isConfigDialogOpen = false
    }

    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.clearUserData()
            onLogoutComplete()
        }
    }
    fun deleteAccount( context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = userPreferencesRepository.getUserData()!!.id
            val token = userPreferencesRepository.getAuthToken() ?: ""
            val success = authRepository.deleteAccount(userId, token)
            if (success) {
                userPreferencesRepository.clearUserData()
            }
            onResult(success)
        }
    }
    fun fetchDriverCode() {
        viewModelScope.launch {
            val token = userPreferencesRepository.getAuthToken() ?: return@launch
            val userId = userPreferencesRepository.getUserData()?.id ?: return@launch
            val result = requestRepository.getCode(token, userId)
            Log.d("TopBarViewModel", "fetchDriverCode: $result")
            driverCode = result.getOrNull()
        }
    }
    fun loadDriverCodeIfNeeded() {
        if (driverCode != null) return
        viewModelScope.launch {
            isDriverCodeLoading = true
            val cachedCode = driverCodeDao.getDriverCode()
            if (cachedCode != null) {
                driverCode = cachedCode
            } else {
                val token = userPreferencesRepository.getAuthToken() ?: return@launch
                val userId = userPreferencesRepository.getUserData()?.id ?: return@launch
                val result = requestRepository.getCode(token, userId)
                driverCode = result.getOrNull()
                if (driverCode != null) {
                    driverCodeDao.saveDriverCode(
                        com.VaSeguro.data.Entitys.DriverCode.DriverCodeEntity(code = driverCode!!)
                    )
                }
            }
            isDriverCodeLoading = false
        }
    }

}