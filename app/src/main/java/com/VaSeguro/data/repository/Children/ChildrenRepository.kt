package com.VaSeguro.data.repository.Children

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Responses.ChildrenResponse
import okhttp3.MultipartBody

interface ChildrenRepository {
    suspend fun getChildren(): List<ChildrenResponse>
    suspend fun getChild(id: String): Children
    suspend fun create(
        forenames: String,
        surnames: String,
        birth_date: String,
        medical_info: String,
        gender: String,
        parent_id: Int,
        driver_id: Int,
        profile_pic: MultipartBody.Part?,
        token: String
    ): Children
    suspend fun update(id: String, child: Children, profilePic: MultipartBody.Part?): Children
    suspend fun remove(id: String)
}