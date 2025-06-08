package com.VaSeguro.data.remote.Login


data class LoginResponse(
    val token: String,
    val user: UserResponse
)