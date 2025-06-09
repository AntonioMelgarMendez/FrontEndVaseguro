
package com.VaSeguro.data.repository.AuthRepository

import com.VaSeguro.data.remote.Login.Login.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse

    suspend fun register(
        forenames: String,
        surnames: String,
        email: String,
        password: String,
        phone_number: String,
        gender: String,
        role_id: Int
    ): LoginResponse
    suspend fun  logout(): Boolean


}