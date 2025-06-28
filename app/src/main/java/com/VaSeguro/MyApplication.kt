package com.VaSeguro

import android.app.Application
import com.VaSeguro.data.AppProvider
import com.google.android.libraries.places.api.Places
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.VaSeguro.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    val appProvider by lazy { AppProvider(this) }

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY)
        }
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }
}