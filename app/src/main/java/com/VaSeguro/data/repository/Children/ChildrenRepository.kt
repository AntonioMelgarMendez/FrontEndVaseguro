package com.VaSeguro.data.repository.Children

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Children.Children
import okhttp3.MultipartBody

interface ChildrenRepository {
    suspend fun getChildren(token: String): List<Children>
    suspend fun getChild(id: String,token: String): Children
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
    suspend fun update(id: String, child: Children, profilePic: MultipartBody.Part?,token: String): Children
    suspend fun remove(id: String, token: String)
}