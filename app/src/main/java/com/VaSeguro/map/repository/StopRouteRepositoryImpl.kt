package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.map.services.StopRouteService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para obtener datos de StopPassenger
 * Por el momento utiliza datos quemados
 */
class StopRouteRepositoryImpl(
    private val stopRouteService: StopRouteService,
    private val userPreferencesRepository: UserPreferencesRepository
): StopRouteRepository {


    override fun getStopRoutesByChild(childId: Int): Flow<List<StopRoute>> = flow {
        try {
            val token = userPreferencesRepository.getAuthToken()

            val authHeader = "Bearer $token"
            val response = stopRouteService.getStopRoutesActiveByChild(childId, authHeader)

            if (!response.isSuccessful) {
                throw Exception("Error HTTP ${response.code()}: ${response.message()}")
            }

            val apiStopRoutes = response.body() ?: emptyList()

            val stopRoutes = apiStopRoutes.map { apiResponse ->
                val stopPassenger = StopPassenger(
                    id = apiResponse.stops_passengers.id,
                    stop = StopData(
                        id = apiResponse.stops_passengers.stop_id,
                        name = "Parada ${apiResponse.stops_passengers.stop_id}",
                        latitude = 0.0, // TODO: Obtener coordenadas reales
                        longitude = 0.0
                    ),
                    child = ChildMap(
                        id = apiResponse.stops_passengers.child_id,
                        forenames = "Child",
                        surnames = "${apiResponse.stops_passengers.child_id}",
                        birth = "2020-01-01",
                        driverId = 1,
                        parentId = 1,
                        medicalInfo = "",
                        createdAt = apiResponse.created_at,
                        profilePic = null
                    ),
                    stop_id = apiResponse.stops_passengers.stop_id,
                    type_id = apiResponse.stops_passengers.type_id,
                    child_id = apiResponse.stops_passengers.child_id
                )

                StopRoute(
                    id = apiResponse.id,
                    stopPassenger = stopPassenger,
                    order = apiResponse.order ?: 0,
                    state = apiResponse.state
                )
            }.sortedBy { it.order }

            emit(stopRoutes)
            println("✅ StopRoutes cargados desde API para child $childId: ${stopRoutes.size} paradas")
        } catch (e: Exception) {
            println("❌ Error API para child $childId: ${e.message}")
            emit(emptyList())
        }
    }
}
