package com.VaSeguro.data.remote

import com.VaSeguro.data.remote.Login.AuthService
import com.VaSeguro.map.services.MapsApiService
import com.agarcia.myfirstandroidapp.data.remote.interceptor.AuthMapsInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://sonoradinamita.live/api/"
    private const val MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"

    private fun getToken(): String {
        return "apikey"
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    private val mapsClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(AuthMapsInterceptor(::getToken))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val mapsRetrofit = Retrofit.Builder()
        .baseUrl(MAPS_BASE_URL)
        .client(mapsClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val mapsApiService: MapsApiService by lazy {
        mapsRetrofit.create(MapsApiService::class.java)
    }
}