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

    @Query("DELETE FROM vehicles")
    suspend fun clearVehicles()
}