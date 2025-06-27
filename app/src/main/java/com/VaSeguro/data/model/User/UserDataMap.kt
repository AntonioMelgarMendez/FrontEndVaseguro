package com.VaSeguro.data.model.User

data class UserDataMap(
    val id: Int,
    val forename: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val profilePic: String? = null,
    val role_id: UserRole,
    val gender: String? = null,

)