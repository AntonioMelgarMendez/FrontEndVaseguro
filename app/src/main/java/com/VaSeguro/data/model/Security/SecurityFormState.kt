package com.VaSeguro.data.model.Security

data class SecurityFormState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isMinLengthValid: Boolean = false,
    val isCaseValid: Boolean = false,
    val isSpecialCharValid: Boolean = false
)
