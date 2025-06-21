package com.VaSeguro.data.repository.RequestRepository

import com.VaSeguro.data.remote.Request.RequestService
import com.VaSeguro.data.remote.Request.RequestState

class RequestRepositoryImpl(
    private val requestService: RequestService
) : RequestRepository {

    override suspend fun sendRequest(token: String, userId: Int): Result<Unit> {
        val response = requestService.approveRequest(
            token = "Bearer $token",
            userId = userId,
            request = RequestState(isSuccessful = true)
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }

    override suspend fun rejectRequest(token: String, userId: Int): Result<Unit> {
        val response = requestService.deleteRequest(
            token = "Bearer $token",
            userId = userId
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }

    override suspend fun getCode(token: String, userId: Int): Result<String> {
        return try {
            val code = requestService.getRequestById("Bearer $token", userId)
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}