package com.VaSeguro.data.repository.RequestRepository

import com.VaSeguro.data.model.User.UserData

interface RequestRepository {
    suspend fun sendRequest(
        token: String,
        userId: Int
    ): Result<Unit>
    suspend fun rejectRequest(
        token: String,
        userId: Int
    ): Result<Unit>
    suspend fun  getCode(
        token: String,
        userId: Int
    ): Result<String>
    suspend fun validateCode(code: String): Result<Map<String, Any>>
}