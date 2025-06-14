package com.VaSeguro.data.remote.Login.Register

import okhttp3.MultipartBody

data class RegisterRequest(
    val forenames: String,
    val surnames: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val gender: String,
    val role_id: Int,
    val profile_pic: MultipartBody.Part?
)