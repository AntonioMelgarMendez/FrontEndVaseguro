package com.VaSeguro.data.Entitys.User

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val forename: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val profilePic: String? = null,
    val roleId: Int,
    val gender: String? = null,
    val status: String? = null
)