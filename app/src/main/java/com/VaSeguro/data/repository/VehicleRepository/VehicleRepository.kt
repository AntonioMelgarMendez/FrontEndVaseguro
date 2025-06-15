package com.VaSeguro.data.repository.VehicleRepository

import com.VaSeguro.data.remote.Vehicle.SimpleResponse
import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface VehicleRepository
{
    suspend fun getAllVehicles(): List<VehicleResponse>
    suspend fun getVehicleById(id: Int): VehicleResponse
    suspend fun createVehicle(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driver_id: Int,
        carPic: MultipartBody.Part?
    ): VehicleResponse
    suspend fun updateVehicle(
        id: Int,
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driverId: Int,
        carPic: MultipartBody.Part?
    ): VehicleResponse
    suspend fun deleteVehicle(id: Int): Boolean
}