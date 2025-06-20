package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Auth.AuthService
import com.VaSeguro.data.remote.Auth.Login.LoginRequest
import com.VaSeguro.data.remote.Auth.Login.LoginResponse
import com.VaSeguro.data.remote.Auth.Register.RegisterRequest
import com.VaSeguro.data.remote.Auth.UserResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResponse {
        return authService.login(LoginRequest(email, password))
    }

    override suspend fun register(
        forenames: String,
        surnames: String,
        email: String,
        password: String,
        phone_number: String,
        gender: String,
        role_id: Int,
        profile_pic: MultipartBody.Part?
    ): LoginResponse {
        return authService.register(
            RegisterRequest(
                forenames = forenames,
                surnames = surnames,
                email = email,
                password = password,
                phone_number = phone_number,
                gender = gender,
                role_id = role_id,
                profile_pic = profile_pic
            )
        )
    }
    override suspend fun logout(): Boolean {
        return try {
            authService.logout()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun registerMultipart(
        forenames: String,
        surnames: String,
        email: String,
        password: String,
        phone_number: String,
        gender: String,
        role_id: Int,
        profile_pic: MultipartBody.Part?
    ): LoginResponse {
        fun String.toRequestBody() = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), this)
        return authService.registerMultipart(
            forenames = forenames.toRequestBody(),
            surnames = surnames.toRequestBody(),
            email = email.toRequestBody(),
            password = password.toRequestBody(),
            phoneNumber = phone_number.toRequestBody(),
            gender = gender.toRequestBody(),
            roleId = role_id.toString().toRequestBody(),
            profile_pic = profile_pic
        )
    }
    override suspend fun getAllUsers(token: String): List<UserResponse> {
        return authService.getAllUsers("Bearer $token")
    }
    override suspend fun getAllUsersWithCodes(token: String): List<UserResponse> {
        return authService.getAllUsersWithCodes("Bearer $token")
    }
    override suspend fun updateUser(
        userId: Int,
        forenames: String,
        surnames: String,
        email: String,
        phone_number: String,
        gender: String,
        profile_pic: MultipartBody.Part?,
        token: String
    ): UserResponse {
        return authService.updateUser(
            userId = userId,
            forenames = forenames.toRequestBody(),
            surnames = surnames.toRequestBody(),
            email = email.toRequestBody(),
            phone_number = phone_number.toRequestBody(),
            gender = gender.toRequestBody(),
            profile_pic = profile_pic,
            authHeader = "Bearer $token"
        )
    }
    override suspend fun changePassword(
        userId: Int,
        oldPassword: String,
        newPassword: String,
        token: String
    ): Boolean {
        return try {
            val body = mapOf(
                "currentPassword" to oldPassword,
                "newPassword" to newPassword
            )
            val response = authService.changePassword(
                userId = userId,
                body = body,
                authHeader = "Bearer $token"
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }


}