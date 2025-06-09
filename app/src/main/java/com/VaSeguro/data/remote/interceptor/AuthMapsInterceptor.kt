package com.agarcia.myfirstandroidapp.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthMapsInterceptor(private val apiKeyProvider: () -> String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val urlWithKey = originalUrl.newBuilder()
            .addQueryParameter("key", apiKeyProvider())
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(urlWithKey)
            .build()

        return chain.proceed(newRequest)
    }
}