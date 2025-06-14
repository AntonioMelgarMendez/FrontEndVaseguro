package com.VaSeguro.data.remote.Login


import com.VaSeguro.data.remote.Login.Login.LoginRequest
import com.VaSeguro.data.remote.Login.Login.LoginResponse
import com.VaSeguro.data.remote.Login.Register.RegisterRequest
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
    @Multipart
    @POST("users/")
    suspend fun registerMultipart(
        @Part("forenames") forenames: RequestBody,
        @Part("surnames") surnames: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("role_id") roleId: RequestBody,
        @Part profile_pic: MultipartBody.Part?
    ): LoginResponse

    @POST("users/logout")
    suspend fun logout(): Unit


}