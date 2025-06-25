package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.Geometry
import com.VaSeguro.map.data.Location
import com.VaSeguro.map.data.LocationAddress
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.Waypoint
import com.VaSeguro.map.request.RouteRequest
import com.VaSeguro.map.services.MapsApiService
import com.VaSeguro.map.services.RoutesApiService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.realtime
import kotlinx.datetime.Instant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : LocationRepository {

    override suspend fun updateLocation(
        driverId: Int,
        lat: Double,
        lon: Double
    ) {
//        supabaseClient.realtime
//        supabaseClient.from("locations").upsert(
//            mapOf(
//                "driverId" to driverId,
//                "latitude" to lat,
//                "longitude" to lon,
//            )
//        )
    }


}