
package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Auth.Login.LoginResponse
import com.VaSeguro.data.remote.Auth.UserResponse
import okhttp3.MultipartBody

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse

    suspend fun register(
        forenames: String,
        surnames: String,
        email: String,
        password: String,
        phone_number: String,
        gender: String,
        role_id: Int,
        profile_pic: MultipartBody.Part?
    ): LoginResponse

    suspend fun registerMultipart(
        forenames: String,
        surnames: String,
        email: String,
        password: String,
        phone_number: String,
        gender: String,
        role_id: Int,
        profile_pic: MultipartBody.Part?
    ): LoginResponse
    suspend fun  logout(): Boolean

    suspend fun getAllUsers(token: String): List<UserResponse>

    suspend fun getAllUsersWithCodes(token: String): List<UserResponse>
    suspend fun updateUser(
        userId: Int,
        forenames: String,
        surnames: String,
        email: String,
        phone_number: String,
        gender: String,
        profile_pic: MultipartBody.Part?,
        token: String
    ): UserResponse
    suspend fun  changePassword(
        userId: Int,
        oldPassword: String,
        newPassword: String,
        token: String
    ): Boolean
    suspend fun  deleteAccount(
        userId: Int,
        token: String
    ): Boolean
    suspend fun recoverPassword(email: String): Result<Unit>
    suspend fun verifyResetCode(email: String, code: String, newPassword: String): Result<Unit>
   suspend fun getUserById(
        userId: Int,
        token: String
    ): UserResponse

}