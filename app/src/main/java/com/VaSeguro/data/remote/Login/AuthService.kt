package com.VaSeguro.data.remote.Login


import com.VaSeguro.data.remote.Login.Login.LoginRequest
import com.VaSeguro.data.remote.Login.Login.LoginResponse
import com.VaSeguro.data.remote.Login.Register.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users/")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("users/logout")
    suspend fun logout(): Unit


}