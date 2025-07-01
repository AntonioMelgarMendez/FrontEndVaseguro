package com.VaSeguro.data.repository.Children

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Children.ChildrenService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.toString

// ChildrenRepositoryImpl.kt
class ChildrenRepositoryImpl(
  private val childrenService: ChildrenService
) : ChildrenRepository {
  override suspend fun getChildren(token: String) =
    childrenService.getChildren("Bearer $token")

  override suspend fun getChild(id: String, token: String) =
    childrenService.getChild(id, "Bearer $token")

  override suspend fun create(
    forenames: String,
    surnames: String,
    birth_date: String,
    medical_info: String,
    gender: String,
    parent_id: Int,
    driver_id: Int,
    profile_pic: MultipartBody.Part?,
    token: String
  ): Children = childrenService.create(
    forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
    surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
    medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
    gender.toRequestBody("text/plain".toMediaTypeOrNull()),
    parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
    driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
    profile_pic,
    birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
    "Bearer $token"
  )

  override suspend fun update(
    id: String,
    child: Children,
    profilePic: MultipartBody.Part?,
    token: String
  ): Children = childrenService.update(
    id = id,
    forenames = child.forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
    surnames = child.surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
    medicalInfo = child.medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
    gender = child.gender.toRequestBody("text/plain".toMediaTypeOrNull()),
    parentId = child.parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
    driverId = child.driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
    profilePic = profilePic,
    birthDate = child.birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
    token = "Bearer $token"
  )

  override suspend fun remove(id: String, token: String) =
    childrenService.remove(id, "Bearer $token")
}