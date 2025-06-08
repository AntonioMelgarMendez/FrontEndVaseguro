
package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Login.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse
}