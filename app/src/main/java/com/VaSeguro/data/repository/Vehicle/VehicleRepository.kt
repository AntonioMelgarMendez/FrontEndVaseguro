package com.VaSeguro.data.repository.Vehicle

import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleCreateRequest
import com.VaSeguro.data.model.Vehicle.VehicleUpdateRequest
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
  suspend fun getVehicles(): Flow<Resource<List<Vehicle>>>
  suspend fun getVehicleById(id: String): Flow<Resource<Vehicle>>
  suspend fun createVehicle(vehicle: VehicleCreateRequest): Flow<Resource<Vehicle>>
  suspend fun updateVehicle(id: String, vehicle: VehicleUpdateRequest): Flow<Resource<Vehicle>>
  suspend fun deleteVehicle(id: String): Flow<Resource<Unit>>
}
