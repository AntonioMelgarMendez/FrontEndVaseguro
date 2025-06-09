package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Login.AuthService
import com.VaSeguro.data.remote.Login.LoginRequest
import com.VaSeguro.data.remote.Login.LoginResponse
import com.VaSeguro.data.remote.Login.RegisterRequest

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
        role_id: Int
    ): LoginResponse {
        return authService.register(
            RegisterRequest(
                forenames = forenames,
                surnames = surnames,
                email = email,
                password = password,
                phone_number = phone_number,
                gender = gender,
                role_id = role_id
            )
        )
    }
}