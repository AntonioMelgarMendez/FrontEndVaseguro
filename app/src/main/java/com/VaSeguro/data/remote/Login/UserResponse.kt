package com.VaSeguro.data.remote.Login


import java.util.Date

data class UserResponse(
    val id: Int,
    val forenames: String,
    val surnames: String,
    val created_at: String,
    val email: String,
    val password: String,
    val phone_number: String?,
    val gender: String?,
    val role_id: Int,
    val profile_pic: String?
)