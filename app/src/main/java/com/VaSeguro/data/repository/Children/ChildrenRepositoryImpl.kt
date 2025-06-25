package com.VaSeguro.data.repository.Children

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Children.ChildrenService
import com.VaSeguro.data.remote.Responses.ChildrenResponse
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChildrenRepositoryImpl(
  private val childrenService: ChildrenService,
  private val userPreferencesRepository: UserPreferencesRepository
) : ChildrenRepository {
  override suspend fun getChildren(): List<ChildrenResponse> {
    val token = userPreferencesRepository.getAuthToken().orEmpty()
    return childrenService.getChildren("Bearer $token")
  }

  override suspend fun getChild(id: String): Children {
    val token = userPreferencesRepository.getAuthToken().orEmpty()
    return childrenService.getChild(id, "Bearer $token")
  }

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

  override suspend fun update(id: String, child: Children, profilePic: MultipartBody.Part?): Children {
    val token = userPreferencesRepository.getAuthToken().orEmpty()
    return childrenService.update(id, child, profilePic, "Bearer $token")
  }

  override suspend fun remove(id: String) {
    val token = userPreferencesRepository.getAuthToken().orEmpty()
    return childrenService.remove(id, "Bearer $token")
  }
}