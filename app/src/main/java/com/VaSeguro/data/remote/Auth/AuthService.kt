package com.VaSeguro.data.remote.Auth


import com.VaSeguro.data.remote.Auth.Login.LoginRequest
import com.VaSeguro.data.remote.Auth.Login.LoginResponse
import com.VaSeguro.data.remote.Auth.Register.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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
    @GET("users/")
    suspend fun getAllUsers(@Header("Authorization") token: String): List<UserResponse>
    @GET("register-codes/users")
    suspend fun getAllUsersWithCodes(@Header("Authorization") token: String): List<UserResponse>
    @Multipart
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Part("forenames") forenames: RequestBody,
        @Part("surnames") surnames: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part profile_pic: MultipartBody.Part?,
        @Header("Authorization") authHeader: String
    ): UserResponse

    @PUT("users/{id}/password")
    suspend fun changePassword(
        @Path("id") userId: Int,
        @Body body: Map<String, String>,
        @Header("Authorization") authHeader: String
    ): retrofit2.Response<okhttp3.ResponseBody>

    @DELETE("users/{id}")
    suspend fun deleteAccount(
        @Path("id") userId: Int,
        @Header("Authorization") authHeader: String
    ): retrofit2.Response<okhttp3.ResponseBody>
}