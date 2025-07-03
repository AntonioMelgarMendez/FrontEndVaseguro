package com.VaSeguro.data.repository.DriverPrefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.driverDataStore by preferencesDataStore("driver_prefs")
object DriverPrefs {
    val DRIVER_ID = intPreferencesKey("driver_id")
    suspend fun saveDriverId(context: Context, id: Int) {
        context.driverDataStore.edit { it[DRIVER_ID] = id }
    }
    suspend fun getDriverId(context: Context): Int? {
        return context.driverDataStore.data.first()[DRIVER_ID]
    }
}