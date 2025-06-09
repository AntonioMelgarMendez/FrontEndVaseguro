package com.VaSeguro.data.remote.Login


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users/")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
}