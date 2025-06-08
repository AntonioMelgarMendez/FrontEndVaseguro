
// MyApplication.kt
package com.VaSeguro

import android.app.Application
import com.VaSeguro.data.AppProvider

class MyApplication : Application() {
    val appProvider by lazy { AppProvider(this) }
}