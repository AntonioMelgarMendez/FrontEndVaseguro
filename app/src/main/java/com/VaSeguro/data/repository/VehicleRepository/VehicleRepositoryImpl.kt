package com.VaSeguro.data.repository.VehicleRepository

import com.VaSeguro.data.Dao.Vehicle.VehicleDao
import com.VaSeguro.data.Entitys.Vehicle.VehicleEntity
import com.VaSeguro.data.model.Vehicle.VehicleMap
import com.VaSeguro.data.remote.Vehicle.VehicleResponse
import com.VaSeguro.data.remote.Vehicle.VehicleService
import com.VaSeguro.data.remote.Vehicle.toEntity
import com.VaSeguro.data.remote.Vehicle.toResponse
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class VehicleRepositoryImpl(
    private val vehicleDao: VehicleDao,
    private val vehicleService: VehicleService
) : VehicleRepository {

//    override suspend fun getAllVehicles(token: String): Flow<Resource<List<VehicleResponse>>> = flow {
//        emit(Resource.Loading)
//        try {
//            val vehicles = vehicleService.getAllVehicles("Bearer $token")
//            emit(Resource.Success(vehicles))
//        } catch (e: Exception) {
//            emit(Resource.Error(e.message ?: "Error al obtener vehículos"))
//        }
//    }

    override suspend fun getAllVehicles(token: String): Flow<Resource<List<VehicleResponse>>> = flow {
        emit(Resource.Loading) // Emitir estado de carga

        try {
            // Intentar obtener vehículos de la base de datos
            val localVehicles = vehicleDao.getAllVehicles().map { it.toResponse() }

            // Si los vehículos locales están disponibles, los emitimos como éxito
            if (localVehicles.isNotEmpty()) {
                emit(Resource.Success(localVehicles))
            } else {
                // Si no hay vehículos en la base de datos, intentamos obtenerlos desde la API
                val remoteVehicles = vehicleService.getAllVehicles("Bearer $token")

                // Guardamos los vehículos obtenidos en la base de datos local
                vehicleDao.insertVehicles(remoteVehicles.map { it.toEntity() })

                // Emitimos el resultado de la API como éxito
                emit(Resource.Success(remoteVehicles))
            }
        } catch (e: Exception) {
            // Si ocurre algún error, emitimos el estado de error
            emit(Resource.Error(e.message ?: "Error al obtener vehículos"))
        }
    }



//    override suspend fun getVehicleById(id: Int, token: String): Flow<Resource<VehicleResponse>> = flow {
//        emit(Resource.Loading)
//        try {
//            val vehicle = vehicleService.getVehicleById(id, "Bearer $token")
//            emit(Resource.Success(vehicle))
//        } catch (e: Exception) {
//            emit(Resource.Error(e.message ?: "Error al obtener vehículo"))
//        }
//    }

    override suspend fun getVehicleById(id: Int, token: String): Flow<Resource<VehicleResponse>> = flow {
        emit(Resource.Loading) // Emitir estado de carga

        try {
            // Intentamos obtener el vehículo de la base de datos local (Room)
            val localVehicle = vehicleDao.getVehicleById(id)?.toResponse()

            if (localVehicle != null) {
                emit(Resource.Success(localVehicle))
            } else {
                // Si no está en la base de datos, obtenemos el vehículo de la API
                val remoteVehicle = vehicleService.getVehicleById(id, "Bearer $token")
                // Guardamos el vehículo en la base de datos local (Room)
                vehicleDao.insertVehicle(remoteVehicle.toEntity())
                emit(Resource.Success(remoteVehicle))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener el vehículo"))
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
        token: String,
        id: Int,
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        carPic: MultipartBody.Part?
    ): Flow<Resource<VehicleResponse>> = flow {
        emit(Resource.Loading)
        try {
            // Realizamos la actualización en la API
            val updated = vehicleService.updateVehicle(
                "Bearer $token",
                id,
                plate.toRequestBody("text/plain".toMediaTypeOrNull()),
                model.toRequestBody("text/plain".toMediaTypeOrNull()),
                brand.toRequestBody("text/plain".toMediaTypeOrNull()),
                year.toRequestBody("text/plain".toMediaTypeOrNull()),
                color.toRequestBody("text/plain".toMediaTypeOrNull()),
                capacity.toRequestBody("text/plain".toMediaTypeOrNull()),
                carPic
            )

            // Actualizar el vehículo también en Room (base de datos local)
            val updatedVehicleEntity = updated.toEntity()  // Convertimos de VehicleResponse a VehicleEntity
            vehicleDao.updateVehicle(updatedVehicleEntity)  // Actualizar en Room

            emit(Resource.Success(updated))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al actualizar vehículo"))
        }
    }

    override suspend fun deleteVehicle(id: Int, token: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            // Eliminar el vehículo de la API
            vehicleService.deleteVehicle("Bearer $token", id)

            // Eliminar el vehículo también en Room (base de datos local)
            vehicleDao.deleteVehicle(id)  // Eliminar en Room

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al eliminar vehículo"))
        }
    }

    override suspend fun updateVehicleInRoom(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)  // Actualiza el vehículo en la base de datos local (Room)
    }

    private val mockVehicles = listOf(
        VehicleMap(
            id = 1,
            plate = "ABC123",
            driver_id = 1,
            model = "Honda CR-V 2021",
            brand = "Honda",
            year = "2021",
            color = "Blue",
            capacity = "15",
            updated_at = "2025-06-28T08:00:00",
            carPic = "https://example.com/honda_crv_2021.jpg",
            created_at = "2025-06-15T10:00:00"
        ),
        VehicleMap(
            id = 2,
            plate = "P987654",
            driver_id = 1,
            model = "Toyota Hiace 2020",
            brand = "Toyota",
            year = "2020",
            color = "White",
            capacity = "20",
            updated_at = "2025-06-16T09:00:00",
            carPic = "https://example.com/toyota_hiace_2020.jpg",
            created_at = "2025-06-16T09:00:00"
        ),
        VehicleMap(
            id = 3,
            plate = "XYZ789",
            driver_id = 2,
            model = "Ford Transit 2022",
            brand = "Ford",
            year = "2022",
            color = "Red",
            capacity = "18",
            updated_at = "2025-06-20T11:30:00",
            carPic = "https://example.com/ford_transit_2022.jpg",
            created_at = "2025-06-20T11:30:00"
        )
    )
    override suspend fun getVehicleById(vehicleId: Int): VehicleMap? {
        return mockVehicles.find { it.id == vehicleId }
    }

    override suspend fun getAllVehicles(): List<VehicleMap> {
        return mockVehicles
    }

    override suspend fun getVehiclesByDriverId(driverId: Int): List<VehicleMap> {
        return mockVehicles.filter { it.driver_id == driverId }
    }
}