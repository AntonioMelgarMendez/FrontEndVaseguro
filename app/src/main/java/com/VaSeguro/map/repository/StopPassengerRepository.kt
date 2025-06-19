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
    fun getAllStopPassengers(): Flow<List<StopPassenger>>

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
     * Obtiene el driver asignado
     */
    fun getDriver(): Driver
}
