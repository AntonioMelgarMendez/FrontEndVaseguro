package com.VaSeguro.data.remote.Login.Login

import com.VaSeguro.data.remote.Login.UserResponse


data class LoginResponse(
    val token: String,
    val user: UserResponse
)