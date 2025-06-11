package com.VaSeguro.map.repository

import com.VaSeguro.map.data.LocationAddress
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.Waypoint
import com.VaSeguro.map.request.RouteRequest
import com.VaSeguro.map.services.RoutesApiService
import com.google.android.gms.maps.model.LatLng

class RoutesApiRepositoryImpl(private val routesApiService: RoutesApiService): RoutesApiRepository {
    override suspend fun getDirections(
        origin: String,
        destination: String,
        waypoints: String?
    ): List<Route> {
        return try {
            // Parsear las coordenadas de origen y destino
            val (originLat, originLng) = origin.split(",").map { it.toDouble() }
            val (destLat, destLng) = destination.split(",").map { it.toDouble() }

            val request = RouteRequest(
                origin = Waypoint(
                    location = LocationAddress(
                        LatLng(originLat, originLng)
                    )
                ),
                destination = Waypoint(
                    location = LocationAddress(
                        LatLng(destLat, destLng)
                    )
                ),
                intermediates = waypoints?.split("|")?.map { point ->
                    val (lat, lng) = point.split(",").map { it.toDouble() }
                    Waypoint(
                        location = LocationAddress(
                            LatLng(lat, lng)
                        )
                    )
                } ?: emptyList()
            )

            val response = routesApiService.computeRoutes(request)

            if (response.isSuccessful) {
                response.body()?.routes ?: emptyList()
            } else {
                // Loggear el error
                println("Error: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}