package com.VaSeguro.data.repository.VehicleRepository

import com.VaSeguro.data.remote.Vehicle.SimpleResponse
import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import com.VaSeguro.data.remote.Vehicle.VehicleService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class VehicleRepositoryImpl(
    private val vehicleService: VehicleService
) : VehicleRepository {

    override suspend fun getAllVehicles(): List<VehicleResponse> =
        vehicleService.getAllVehicles()

    override suspend fun getVehicleById(id: Int): VehicleResponse =
        vehicleService.getVehicleById(id)

    override suspend fun createVehicle(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driver_id: Int,
        carPic: MultipartBody.Part?
    ): VehicleResponse =
        vehicleService.createVehicle(
            plate.toRequestBody("text/plain".toMediaTypeOrNull()),
            model.toRequestBody("text/plain".toMediaTypeOrNull()),
            brand.toRequestBody("text/plain".toMediaTypeOrNull()),
            year.toRequestBody("text/plain".toMediaTypeOrNull()),
            color.toRequestBody("text/plain".toMediaTypeOrNull()),
            capacity.toRequestBody("text/plain".toMediaTypeOrNull()),
            driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            carPic
        )

    override suspend fun updateVehicle(
        id: Int,
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driverId: Int,
        carPic: MultipartBody.Part?
    ): VehicleResponse =
        vehicleService.updateVehicle(
            id,
            plate.toRequestBody("text/plain".toMediaTypeOrNull()),
            model.toRequestBody("text/plain".toMediaTypeOrNull()),
            brand.toRequestBody("text/plain".toMediaTypeOrNull()),
            year.toRequestBody("text/plain".toMediaTypeOrNull()),
            color.toRequestBody("text/plain".toMediaTypeOrNull()),
            capacity.toRequestBody("text/plain".toMediaTypeOrNull()),
            driverId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            carPic
        )

    override suspend fun deleteVehicle(id: Int): Boolean {
        vehicleService.deleteVehicle(id)
        return true
    }
}