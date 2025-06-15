package com.VaSeguro.ui.screens.Parents.Configuration

data class SecurityFormState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isMinLengthValid: Boolean = false,
    val isCaseValid: Boolean = false,
    val isSpecialCharValid: Boolean = false
)