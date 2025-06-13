package com.VaSeguro.ui.screens.Start.SplashScren

import android.util.Base64
import org.json.JSONObject

public fun isTokenValid(token: String?): Boolean {
    if (token.isNullOrBlank()) return false
    val parts = token.split(".")
    if (parts.size != 3) return false
    return try {
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        val json = JSONObject(payload)
        val exp = json.optLong("exp", 0)
        val now = System.currentTimeMillis() / 1000
        exp > now
    } catch (e: Exception) {
        false
    }
}