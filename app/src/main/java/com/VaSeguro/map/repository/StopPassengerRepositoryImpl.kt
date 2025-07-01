package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.map.services.StopPassengerService
import com.VaSeguro.map.services.StopRouteService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para obtener datos de StopPassenger
 * Actualizado para usar el servicio real del servidor
 */
class StopPassengerRepositoryImpl(
    private val stopPassengerService: StopPassengerService,
    private val userPreferencesRepository: UserPreferencesRepository,
): StopPassengerRepository {

    private suspend fun getAuthHeader(): String =
        "Bearer ${userPreferencesRepository.getAuthToken().orEmpty()}"

    /**
     * Obtiene todos los StopPassenger disponibles desde el servidor
     * Con fallback a datos locales en caso de error
     */
    override fun getAllStopPassengers(driverId: Int): Flow<List<StopPassenger>> = flow {
        try {
            println("DEBUG: Intentando obtener StopPassengers del servidor para driver: $driverId")
            val response = stopPassengerService.getStopPassengersByDriver(driverId, authHeader = getAuthHeader())

            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!
                println("DEBUG: Datos obtenidos del servidor: ${serverData.size} StopPassengers")
                emit(serverData)
            } else {
                println("DEBUG: API call failed with code: ${response.code()}, using mock data")
            }
        } catch (e: Exception) {
            println("DEBUG: Error al obtener StopPassengers del servidor: ${e.message}")
            e.printStackTrace()

        }
    }

    /**
     * Obtiene StopPassenger filtrados por tipo
     */
    override fun getStopPassengersByType(type: StopType): Flow<List<StopPassenger>> = flow {
        getAllStopPassengers(1).collect { stopPassengers ->
            emit(stopPassengers.filter { it.stopType == type })
        }
    }


    /**
     * Obtiene StopPassenger para un niño específico
     */
    override fun getStopPassengersByChild(childId: Int): Flow<List<StopPassenger>> = flow {
        getAllStopPassengers(1).collect { stopPassengers ->
            emit(stopPassengers.filter { it.child.id == childId })
        }
    }

    /**
     * Convierte un StopPassenger a LatLng para usar en el mapa
     */
    override fun stopPassengerToLatLng(stopPassenger: StopPassenger): LatLng {
        return LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
    }



    override suspend fun updateStopRouteState(
        stopPassengerId: Int,
        routeId: Int,
        isCompleted: Boolean,
    ): Boolean {
        return try {
            println("DEBUG: Actualizando StopRoute - StopPassengerId: $stopPassengerId, RouteId: $routeId, Estado: $isCompleted")

            // Crear el body del request con el estado
            val stateUpdate = mapOf("state" to isCompleted)

            // Hacer la llamada al servicio PUT
            val response = stopPassengerService.updateStopRouteState(
                stopPassengerId = stopPassengerId,
                stopRouteId = routeId,
                authHeader = getAuthHeader(),
                stateUpdate = stateUpdate
            )

            if (response.isSuccessful && response.body() != null) {
                val updatedStopRoute = response.body()!!
                println("DEBUG: StopRoute actualizado exitosamente: ${updatedStopRoute.id}, Estado: ${updatedStopRoute.state}")
                true
            } else {
                println("DEBUG: Error en la respuesta del servidor - Código: ${response.code()}, Mensaje: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            println("DEBUG: Error al actualizar StopRoute: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun getStopRouteByStopPassenger(
        stopPassengerId: Int,
        routeId: Int
    ): StopRoute? {
        // TODO: Implementar llamada al servicio
        return null
    }

    override suspend fun notifyStopPassengerStateChange(stopPassengerUpdate: Map<String, Any>): Boolean {
        // TODO: Implementar notificación al servicio
        return true
    }
}
