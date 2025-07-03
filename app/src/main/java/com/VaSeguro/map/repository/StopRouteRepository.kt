package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


interface StopRouteRepository {

    fun getStopRoutesByChild(childId: Int): Flow<List<StopRoute>>

    // NUEVO: Método para obtener StopRoutes de forma directa (sin Flow) para evitar problemas de concurrencia
    suspend fun getStopRoutesByChildDirect(childId: Int): List<StopRoute>
}
