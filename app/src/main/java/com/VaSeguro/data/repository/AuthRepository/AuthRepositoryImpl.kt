// AuthRepositoryImpl.kt
package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Login.AuthService
import com.VaSeguro.data.remote.Login.LoginRequest

class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(email: String, password: String) =
        authService.login(LoginRequest(email, password))
    
}