package com.VaSeguro.data.remote.Request

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path

interface RequestService {
    @PATCH("register-codes/{id}/state")
    suspend fun approveRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body request: RequestState
    ): Response<Unit>

    @DELETE("register-codes/{id}")
    suspend fun deleteRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<Unit>
}