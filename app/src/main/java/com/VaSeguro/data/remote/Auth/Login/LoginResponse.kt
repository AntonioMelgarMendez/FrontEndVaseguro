package com.VaSeguro.data.remote.Auth.Login

import com.VaSeguro.data.remote.Auth.UserResponse


data class LoginResponse(
    val token: String,
    val user: UserResponse
)