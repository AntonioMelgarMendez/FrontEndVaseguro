package com.VaSeguro.map.repository

import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.Geometry
import com.VaSeguro.map.data.Location
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.services.MapsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsApiRepositoryImpl(
    private val mapsApiService: MapsApiService
) : MapsApiRepository {

    override suspend fun getDirections(
        origin: String,
        destination: String,
        waypoints: String?
    ): List<Route> {
        return try {
            val response = mapsApiService.getDirections(origin, destination, waypoints)
            if (response.isSuccessful) {
                response.body()?.routes ?: emptyList()
            } else {
                emptyList() // O lanzar una excepción si prefieres
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

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