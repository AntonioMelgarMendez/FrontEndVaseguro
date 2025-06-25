package com.VaSeguro.data.remote.Children

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Auth.Login.LoginResponse
import com.VaSeguro.data.remote.Responses.ChildrenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
interface ChildrenService {
    @GET("children")
    suspend fun getChildren(): List<ChildrenResponse>

    @GET("children/{id}")
    suspend fun getChild(@Path("id") id: String): Children

    @Multipart
    @POST("children/")
    suspend fun create(
        @Part("forenames") forenames: RequestBody,
        @Part("surnames") surnames: RequestBody,
        @Part("medical_info") medicalInfo: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("parent_id") parentId: RequestBody,
        @Part("driver_id") driverId: RequestBody,
        @Part profile_pic: MultipartBody.Part?,
        @Part("birth_date") birthDate: RequestBody,
        @Header("Authorization") authHeader: String
    ): Children

    @Multipart
    @PUT("children/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Part("data") data: Children,
        @Part profilePic: MultipartBody.Part?
    ): Children

    @DELETE("children/{id}")
    suspend fun remove(@Path("id") id: String)
}