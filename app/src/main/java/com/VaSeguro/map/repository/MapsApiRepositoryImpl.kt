package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.Destination
import com.VaSeguro.map.data.Geometry
import com.VaSeguro.map.data.Location
import com.VaSeguro.map.data.LocationAddress
import com.VaSeguro.map.data.Origin
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.Waypoint
import com.VaSeguro.map.request.RouteRequest
import com.VaSeguro.map.services.MapsApiService
import com.VaSeguro.map.services.RoutesApiService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsApiRepositoryImpl(
    private val mapsApiService: MapsApiService
) : MapsApiRepository {



    override suspend fun searchPlaces(
        query: String
    ): List<PlaceResult> {
        return try {
            val response = mapsApiService.searchPlaces(query)
            if (response.isSuccessful) {
                response.body()?.results?.map { apiResult ->
                    PlaceResult(
                        name = apiResult.name,
                        address = apiResult.formatted_address,
                        geometry = Geometry(
                            Location(
                                lat = apiResult.geometry.location.lat,
                                lng = apiResult.geometry.location.lng
                            )
                        )
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }


    }
}