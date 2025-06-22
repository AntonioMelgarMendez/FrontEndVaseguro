package com.VaSeguro.data.repository.VehicleRepository

import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import com.VaSeguro.data.remote.Vehicle.VehicleService
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class VehicleRepositoryImpl(
    private val vehicleService: VehicleService
) : VehicleRepository {

    override suspend fun getAllVehicles(): Flow<Resource<List<VehicleResponse>>> = flow {
        emit(Resource.Loading)
        try {
            val vehicles = vehicleService.getAllVehicles()
            emit(Resource.Success(vehicles))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener vehículos"))
        }
    }
    override suspend fun getVehicleById(id: Int, token: String): Flow<Resource<VehicleResponse>> = flow {
        emit(Resource.Loading)
        try {
            val vehicle = vehicleService.getVehicleById(id, "Bearer $token")
            emit(Resource.Success(vehicle))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener vehículo"))
        }
    }

    override suspend fun createVehicle(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        driver_id: Int,
        carPic: String?
    ): Flow<Resource<VehicleResponse>> = flow {
        emit(Resource.Loading)
        try {
            val vehicle = vehicleService.createVehicle(
                plate,
                model,
                brand,
                year,
                color,
                capacity,
                driver_id,
                carPic
            )
            emit(Resource.Success(vehicle))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al crear vehículo"))
        }
    }
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
    ): Flow<Resource<VehicleResponse>> = flow {
        emit(Resource.Loading)
        try {
            val updated = vehicleService.updateVehicle(
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
            emit(Resource.Success(updated))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al actualizar vehículo"))
        }
    }

    override suspend fun deleteVehicle(id: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            vehicleService.deleteVehicle(id)
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al eliminar vehículo"))
        }
    }
}