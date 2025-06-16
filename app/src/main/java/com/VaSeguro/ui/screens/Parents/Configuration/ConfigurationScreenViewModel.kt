package com.VaSeguro.ui.screens.Parents.Configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import com.VaSeguro.data.model.Security.SecurityFormState
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConfigurationScreenViewModel : ViewModel() {
    private var currentUser = UserData(
        id = "001",
        forename = "Juan",
        surname = "Pérez",
        email = "juan.perez@example.com",
        phoneNumber = "123456789",
        profilePic = null,
        role_id = UserRole(1, "User"),
        gender = "Male"
    )

    val originalUserData: UserData
        get() = currentUser

    val originalSecurityState = SecurityFormState()

    private val _userData = MutableStateFlow(currentUser)
    val userData: StateFlow<UserData> = _userData

    private val initialSecurity = SecurityFormState()
    private val _securityState = MutableStateFlow(initialSecurity)
    val securityState: StateFlow<SecurityFormState> = _securityState

    fun onUserFieldChange(update: (UserData) -> UserData) {
        _userData.value = update(_userData.value)
    }

    fun onSecurityFieldChange(update: (SecurityFormState) -> SecurityFormState) {
        val updated = update(_securityState.value)
        _securityState.value = updated.copy(
            isMinLengthValid = updated.newPassword.length >= 8,
            isCaseValid = updated.newPassword.any { it.isUpperCase() } && updated.newPassword.any { it.isLowerCase() },
            isSpecialCharValid = updated.newPassword.any { !it.isLetterOrDigit() }
        )
    }

    fun onUpdateAccount() {
        currentUser = _userData.value
        Log.d("ConfigurationVM", "Account updated: $currentUser")
    }

    fun onUpdatePassword() {
        val state = _securityState.value
        if (state.newPassword == state.confirmPassword &&
            state.isMinLengthValid &&
            state.isCaseValid &&
            state.isSpecialCharValid
        ) {
            Log.d("ConfigurationVM", "Password updated successfully")
        } else {
            Log.d("ConfigurationVM", "Password validation failed")
        }
    }

    fun onCancelChanges() {
        _userData.value = currentUser
        _securityState.value = initialSecurity
    }
}