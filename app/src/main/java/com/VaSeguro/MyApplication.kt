package com.VaSeguro

import android.app.Application
import com.VaSeguro.data.AppProvider
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {
    val appProvider by lazy { AppProvider(this) }

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY)
        }
    }
}