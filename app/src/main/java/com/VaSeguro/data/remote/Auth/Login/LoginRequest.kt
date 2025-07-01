package com.VaSeguro.data.remote.Auth.Login

data class LoginRequest(
    val email: String,
    val password: String,
    val onesignal_player_id: String? = null // Optional field for OneSignal player ID
)