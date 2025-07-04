package com.VaSeguro.data.remote

import com.VaSeguro.data.remote.Children.ChildrenService
import com.VaSeguro.data.remote.Vehicle.VehicleService
import com.VaSeguro.data.remote.Auth.AuthService
import com.VaSeguro.map.services.MapsApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.VaSeguro.BuildConfig
import com.VaSeguro.data.remote.Call.CallService
import com.VaSeguro.data.remote.Chat.ChatService
import com.VaSeguro.data.remote.Request.RequestService
import com.VaSeguro.data.remote.Route.RouteService
import com.VaSeguro.data.remote.Stops.StopsService
import com.VaSeguro.map.services.RoutesApiService
import com.VaSeguro.map.services.SavedRoutesService
import com.VaSeguro.map.services.StopPassengerService
import com.VaSeguro.map.services.StopRouteService
import com.agarcia.myfirstandroidapp.data.remote.interceptor.SmartAuthInterceptor

object RetrofitInstance {
    private const val BASE_URL = "https://sonoradinamita.live/api/"
    private const val MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"
    private const val ROUTES_BASE_URL = "https://routes.googleapis.com/"

    private fun getToken(): String {
        return BuildConfig.MAPS_API_KEY
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
        .addInterceptor(SmartAuthInterceptor(::getToken))
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

    private val routesRetrofit = Retrofit.Builder()
        .baseUrl(ROUTES_BASE_URL)
        .client(mapsClient)
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

//    val routeService: RouteService by lazy {
//        retrofit.create(RouteService::class.java)
//    }

    val chatService: ChatService by lazy {
        retrofit.create(ChatService::class.java)
    }

    val requestService: RequestService by lazy {
        retrofit.create(RequestService::class.java)
    }

    val mapsApiService: MapsApiService by lazy {
        mapsRetrofit.create(MapsApiService::class.java)
    }
    val routesApiService: RoutesApiService by lazy {
        routesRetrofit.create(RoutesApiService::class.java)
    }

    val routeService: RouteService by lazy {
        routesRetrofit.create(RouteService::class.java)
    }

    val savedRoutesSevcie: SavedRoutesService by lazy {
        retrofit.create(SavedRoutesService::class.java)
    }

    val stopPassengerService: StopPassengerService by lazy {
        retrofit.create(StopPassengerService::class.java)
    }

    val stopRouteService: StopRouteService by lazy {
        retrofit.create(StopRouteService::class.java)
    }

    val stopService: StopsService by lazy {
        retrofit.create(StopsService::class.java)
    }

    val chatApiService: ChatService by lazy {
        retrofit.create(ChatService::class.java)
    }
    val callService: CallService by lazy {
        retrofit.create(CallService::class.java)
    }
}