package com.VaSeguro.data.repository.VehicleRepository

import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface VehicleRepository
{
    suspend fun getAllVehicles(token: String): Flow<Resource<List<VehicleResponse>>>
    suspend fun getVehicleById(id: Int, token: String): Flow<Resource<VehicleResponse>>
    suspend fun getVehicleById(vehicleId: Int): VehicleMap?
    suspend fun getAllVehicles(): List<VehicleMap>
    suspend fun getVehiclesByDriverId(driverId: Int): List<VehicleMap>
    suspend fun createVehicle(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driver_id: Int,
        carPic: String?
    ): Flow<Resource<VehicleResponse>>
    suspend fun updateVehicle(
        token: String,
        id: Int,
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        carPic: MultipartBody.Part?
    ): Flow<Resource<VehicleResponse>>
    suspend fun deleteVehicle(id: Int,token: String): Flow<Resource<Boolean>>
}