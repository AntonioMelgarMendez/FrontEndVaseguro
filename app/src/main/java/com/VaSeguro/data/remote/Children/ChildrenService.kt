package com.VaSeguro.data.remote.Children

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Auth.Login.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
// ChildrenService.kt
interface ChildrenService {
    @GET("children")
    suspend fun getChildren(@Header("Authorization") token: String): List<Children>

    @GET("children/{id}")
    suspend fun getChild(@Path("id") id: String, @Header("Authorization") token: String): Children

    @Multipart
    @POST("children")
    suspend fun create(
        @Part("forenames") forenames: RequestBody,
        @Part("surnames") surnames: RequestBody,
        @Part("medical_info") medicalInfo: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("parent_id") parentId: RequestBody,
        @Part("driver_id") driverId: RequestBody,
        @Part profilePic: MultipartBody.Part?,
        @Part("birth_date") birthDate: RequestBody,
        @Header("Authorization") token: String
    ): Children

    @Multipart
    @PUT("children/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Part("forenames") forenames: RequestBody,
        @Part("surnames") surnames: RequestBody,
        @Part("medical_info") medicalInfo: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("parent_id") parentId: RequestBody,
        @Part("driver_id") driverId: RequestBody,
        @Part profilePic: MultipartBody.Part?,
        @Part("birth_date") birthDate: RequestBody,
        @Header("Authorization") token: String
    ): Children
    @DELETE("children/{id}")
    suspend fun remove(@Path("id") id: String, @Header("Authorization") token: String)
}