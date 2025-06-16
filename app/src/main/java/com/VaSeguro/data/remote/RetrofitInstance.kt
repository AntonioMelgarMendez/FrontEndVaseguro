package com.VaSeguro.data.remote

import com.VaSeguro.data.remote.Children.ChildrenService
import com.VaSeguro.data.remote.Login.AuthService
import com.VaSeguro.data.remote.Vehicle.VehicleService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://sonoradinamita.live/api/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val vehicleService: VehicleService by lazy {
        retrofit.create(VehicleService::class.java)
    }

    val childrenService: ChildrenService by lazy {
        retrofit.create(ChildrenService::class.java)
    }
}