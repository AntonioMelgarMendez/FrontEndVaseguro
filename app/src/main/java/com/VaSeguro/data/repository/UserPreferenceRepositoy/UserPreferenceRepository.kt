package com.VaSeguro.data.repository.UserPreferenceRepository

import com.VaSeguro.data.remote.Login.UserResponse
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val isLinearLayout: Flow<Boolean>
    suspend fun saveLayoutPreference(isLinearLayout: Boolean)

    val rememberMe: Flow<Boolean>
    suspend fun saveRememberMePreference(remember: Boolean)
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun saveUserEmail(email: String)
    suspend fun getUserEmail(): String?
    suspend fun saveUserData(user: UserResponse)
    suspend fun getUserData(): UserResponse?
    suspend fun clearUserData()
}