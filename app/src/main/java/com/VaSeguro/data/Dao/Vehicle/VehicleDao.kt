package com.VaSeguro.data.Dao.Vehicle

import androidx.room.*
import com.VaSeguro.data.Entitys.Vehicle.VehicleEntity

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehicles(): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>)

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    suspend fun getVehicleById(vehicleId: Int): VehicleEntity?

    @Update  // Esto es para actualizar un vehículo
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles")
    suspend fun clearVehicles()

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: Int)  // Eliminar por ID
}