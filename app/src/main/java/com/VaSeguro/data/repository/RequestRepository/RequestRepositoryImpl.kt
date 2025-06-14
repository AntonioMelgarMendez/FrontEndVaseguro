package com.VaSeguro.data.repository.RequestRepository

import com.VaSeguro.data.remote.Request.RequestService
import com.VaSeguro.data.remote.Request.RequestState

class RequestRepositoryImpl(
    private val requestService: RequestService
) : RequestRepository {

    override suspend fun sendRequest(token: String, userId: String): Result<Unit> {
        val response = requestService.approveRequest(
            token = token,
            userId = userId,
            request = RequestState(isSuccessful = true)
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }

    override suspend fun rejectRequest(token: String, userId: String): Result<Unit> {
        val response = requestService.deleteRequest(
            token = token,
            userId = userId
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }
}