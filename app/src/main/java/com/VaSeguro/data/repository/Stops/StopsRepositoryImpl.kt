package com.VaSeguro.data.repository.Stops

import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopDto
import com.VaSeguro.data.model.Stop.Stops
import com.VaSeguro.data.remote.Stops.StopsService

class StopsRepositoryImpl(
    private val stopsService: StopsService
) : StopsRepository {
    override suspend fun getAllStops(token: String): List<StopDto> =
        stopsService.getAllStops(token)

    override suspend fun getStopById(id: String, token: String): Stops =
        stopsService.getStopById(id, token)

    override suspend fun createStop(stop: Stops, token: String): Stops =
        stopsService.createStop(stop, token)

    override suspend fun updateStop(id: String, stop: Stops, token: String): Stops =
        stopsService.updateStop(id, stop, token)

    override suspend fun deleteStop(id: String, token: String) =
        stopsService.deleteStop(id, token)
}