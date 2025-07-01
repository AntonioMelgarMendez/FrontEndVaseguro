package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


interface StopPassengerRepository {

    /**
     * Obtiene todos los StopPassenger disponibles
     */
    fun getAllStopPassengers(driverId: Int): Flow<List<StopPassenger>>

    /**
     * Obtiene StopPassenger filtrados por tipo (HOME o INSTITUTION)
     */
    fun getStopPassengersByType(type: StopType): Flow<List<StopPassenger>>

    /**
     * Obtiene StopPassenger para un niño específico
     */
    fun getStopPassengersByChild(childId: Int): Flow<List<StopPassenger>>

    /**
     * Convierte un StopPassenger a LatLng para usar en el mapa
     */
    fun stopPassengerToLatLng(stopPassenger: StopPassenger): LatLng


    /**
     * NUEVA: Actualiza el estado de un StopRoute basado en un StopPassenger
     * @param stopPassengerId ID del StopPassenger que se está actualizando
     * @param routeId ID de la ruta actual
     * @param isCompleted Estado completado del StopPassenger
     * @param driverId ID del conductor actual
     */
    suspend fun updateStopRouteState(
        stopPassengerId: Int,
        routeId: Int,
        isCompleted: Boolean,
    ): Boolean

    /**
     * NUEVA: Obtiene el StopRoute asociado a un StopPassenger en una ruta específica
     * @param stopPassengerId ID del StopPassenger
     * @param routeId ID de la ruta
     */
    suspend fun getStopRouteByStopPassenger(
        stopPassengerId: Int,
        routeId: Int
    ): com.VaSeguro.data.model.Stop.StopRoute?

    /**
     * NUEVA: Notifica cambios de estado de StopPassenger al backend
     * @param stopPassengerUpdate Mapa con los datos del cambio de estado
     */
    suspend fun notifyStopPassengerStateChange(
        stopPassengerUpdate: Map<String, Any>
    ): Boolean
}
