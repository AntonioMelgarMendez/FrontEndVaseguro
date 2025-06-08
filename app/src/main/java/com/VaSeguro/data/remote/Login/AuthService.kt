package com.VaSeguro.data.remote.Login


import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}