package com.agarcia.myfirstandroidapp.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class SmartAuthInterceptor(private val apiKeyProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val requestBuilder = originalRequest.newBuilder()

        return when {
            originalUrl.host.contains("maps.googleapis.com") -> {
                // Legacy Maps API
                val urlWithKey = originalUrl.newBuilder()
                    .addQueryParameter("key", apiKeyProvider())
                    .build()

                chain.proceed(
                    requestBuilder
                        .url(urlWithKey)
                        .build()
                )
            }

            originalUrl.host.contains("routes.googleapis.com") -> {
                // Routes API v2
                chain.proceed(
                    requestBuilder
                        .addHeader("X-Goog-Api-Key", apiKeyProvider())
                        .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs")
                        .build()
                )
            }

            else -> chain.proceed(originalRequest) // Por si se conecta a otros servicios
        }
    }
}

