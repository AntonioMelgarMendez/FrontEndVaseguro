
package com.VaSeguro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.VaSeguro.data.remote.RetrofitInstance
import com.VaSeguro.data.repository.AuthRepository.AuthRepositoryImpl
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepositoryImpl
import com.VaSeguro.data.repository.Vehicle.VehicleRepositoryImpl

private const val USER_PREFERENCE_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCE_NAME)

class AppProvider(context: Context) {
    // Inicialización directa (sin lazy)
    private val userPreferencesRepository = UserPreferencesRepositoryImpl(context.dataStore)
    private val authRepository = AuthRepositoryImpl(RetrofitInstance.authService)
    private val vehicleRespository = VehicleRepositoryImpl(RetrofitInstance.vehicleService)

    fun provideUserPreferences() = userPreferencesRepository
    fun provideAuthRepository() = authRepository
    fun provideVehicleRepository() = vehicleRespository
}