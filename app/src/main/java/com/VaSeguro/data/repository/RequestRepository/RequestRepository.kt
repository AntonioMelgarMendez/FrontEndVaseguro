package com.VaSeguro.data.repository.RequestRepository

interface RequestRepository {
    suspend fun sendRequest(
        token: String,
        userId: Int
    ): Result<Unit>
    suspend fun rejectRequest(
        token: String,
        userId: Int
    ): Result<Unit>
}