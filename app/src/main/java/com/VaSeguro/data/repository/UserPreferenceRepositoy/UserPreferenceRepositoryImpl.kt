package com.VaSeguro.data.repository.UserPreferenceRepository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.VaSeguro.data.remote.Auth.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.text.clear
import kotlin.text.get
import kotlin.text.set

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {


    private companion object {
        val IS_LINEAR_LAYOUT = booleanPreferencesKey("IS_LINEAR_LAYOUT")
        val REMEMBER_ME = booleanPreferencesKey("REMEMBER_ME")
        val AUTH_TOKEN = stringPreferencesKey("AUTH_TOKEN")
        val USER_EMAIL = stringPreferencesKey("USER_EMAIL")
        val USER_ID = intPreferencesKey("USER_ID")
        val USER_FORENAMES = stringPreferencesKey("USER_FORENAMES")
        val USER_SURNAMES = stringPreferencesKey("USER_SURNAMES")
        val USER_PHONE = stringPreferencesKey("USER_PHONE")
        val USER_GENDER = stringPreferencesKey("USER_GENDER")
        val USER_ROLE = intPreferencesKey("USER_ROLE")
        val USER_PROFILE_PIC = stringPreferencesKey("USER_PROFILE_PIC")
        val USER_CREATED_AT = stringPreferencesKey("USER_CREATED_AT")
        val LAST_USERS_FETCH_TIME = longPreferencesKey("LAST_USERS_FETCH_TIME")
        val LAST_ROUTES_FETCH_TIME = longPreferencesKey("LAST_ROUTES_FETCH_TIME")
        val LAST_STOPS_FETCH_TIME = longPreferencesKey("LAST_STOPS_FETCH_TIME")
        val LAST_VEHICLES_FETCH_TIME = longPreferencesKey("LAST_VEHICLES_FETCH_TIME")
        val USER_ONESIGNAL_PLAYER_ID = stringPreferencesKey("USER_ONESIGNAL_PLAYER_ID")
    }

    override val isLinearLayout: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[IS_LINEAR_LAYOUT] ?: true
        }

    override suspend fun saveLayoutPreference(isLinearLayout: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LINEAR_LAYOUT] = isLinearLayout
        }
    }

    override val rememberMe: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[REMEMBER_ME] ?: false
        }

    override suspend fun saveRememberMePreference(remember: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_ME] = remember
        }
    }

    override suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    override suspend fun getAuthToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN]
        }.firstOrNull()
    }

    override suspend fun saveUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    override suspend fun getUserEmail(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL]
        }.firstOrNull()
    }
    override suspend fun saveUserData(user: UserResponse) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_FORENAMES] = user.forenames
            preferences[USER_SURNAMES] = user.surnames
            preferences[USER_EMAIL] = user.email
            preferences[USER_PHONE] = user.phone_number ?: ""
            preferences[USER_GENDER] = user.gender ?: ""
            preferences[USER_ROLE] = user.role_id
            preferences[USER_PROFILE_PIC] = user.profile_pic ?: ""
            preferences[USER_CREATED_AT] = user.created_at
            preferences[USER_ONESIGNAL_PLAYER_ID] = user.onesignal_player_id ?: ""
        }
    }

    override suspend fun getUserData(): UserResponse? {
        return dataStore.data.map { preferences ->
            try {
                UserResponse(
                    id = preferences[USER_ID] ?: return@map null,
                    forenames = preferences[USER_FORENAMES] ?: return@map null,
                    surnames = preferences[USER_SURNAMES] ?: return@map null,
                    email = preferences[USER_EMAIL] ?: return@map null,
                    password = "", // No guardamos la contraseña
                    phone_number = preferences[USER_PHONE],
                    gender = preferences[USER_GENDER],
                    role_id = preferences[USER_ROLE] ?: 0,
                    profile_pic = preferences[USER_PROFILE_PIC],
                    created_at = preferences[USER_CREATED_AT] ?: ""
                    , onesignal_player_id = preferences[USER_ONESIGNAL_PLAYER_ID] ?: null
                )
            } catch (e: Exception) {
                null
            }
        }.firstOrNull()
    }

    override suspend fun userDataFlow(): Flow<UserResponse?> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                try {
                    UserResponse(
                        id = preferences[USER_ID] ?: return@map null,
                        forenames = preferences[USER_FORENAMES] ?: return@map null,
                        surnames = preferences[USER_SURNAMES] ?: return@map null,
                        email = preferences[USER_EMAIL] ?: return@map null,
                        password = "",
                        phone_number = preferences[USER_PHONE],
                        gender = preferences[USER_GENDER],
                        role_id = preferences[USER_ROLE] ?: 0,
                        profile_pic = preferences[USER_PROFILE_PIC],
                        created_at = preferences[USER_CREATED_AT] ?: ""
                        , onesignal_player_id = preferences[USER_ONESIGNAL_PLAYER_ID] ?: null
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    override suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    override suspend fun getLastUsersFetchTime(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_USERS_FETCH_TIME]
        }.firstOrNull()
    }

    override suspend fun setLastUsersFetchTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_USERS_FETCH_TIME] = time
        }
    }
    override suspend fun getLastRoutesFetchTime(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_ROUTES_FETCH_TIME]
        }.firstOrNull()
    }

    override suspend fun setLastRoutesFetchTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_ROUTES_FETCH_TIME] = time
        }
    }
    override suspend fun getLastStopsFetchTime(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_STOPS_FETCH_TIME]
        }.firstOrNull()
    }

    override suspend fun setLastStopsFetchTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_STOPS_FETCH_TIME] = time
        }
    }
    override suspend fun getLastVehiclesFetchTime(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_VEHICLES_FETCH_TIME]
        }.firstOrNull()
    }

    override suspend fun setLastVehiclesFetchTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_VEHICLES_FETCH_TIME] = time
        }
    }




}