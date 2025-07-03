package com.VaSeguro.data.remote.Request

import com.VaSeguro.data.model.User.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RequestService {
    @PATCH("register-codes/{id}/state")
    suspend fun approveRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: RequestState
    ): Response<Unit>

    @DELETE("register-codes/{id}")
    suspend fun deleteRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<Unit>
    
    @GET("register-codes/{id}")
    suspend fun getRequestById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): okhttp3.ResponseBody

    @POST("register-codes/validate")
    suspend fun validateCode(
        @Body body: Map<String, String>
    ): Response<okhttp3.ResponseBody>
}