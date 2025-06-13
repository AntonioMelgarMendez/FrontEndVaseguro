package com.VaSeguro.data.remote.Login.Register

data class RegisterRequest(
    val forenames: String,
    val surnames: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val gender: String,
    val role_id: Int
)