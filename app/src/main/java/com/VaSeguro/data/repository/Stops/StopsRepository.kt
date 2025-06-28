package com.VaSeguro.data.repository.Stops

import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.Stops

interface StopsRepository {
    suspend fun getAllStops(token: String): List<Stops>
    suspend fun getStopById(id: String, token: String): Stops
    suspend fun createStop(stop: Stops, token: String): Stops
    suspend fun updateStop(id: String, stop: Stops, token: String): Stops
    suspend fun deleteStop(id: String, token: String)
}