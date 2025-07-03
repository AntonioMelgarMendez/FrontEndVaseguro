package com.VaSeguro.data.Entitys.Vehicle

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.VaSeguro.data.remote.Vehicle.VehicleResponse

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: Int,
    val plate: String,
    val driver_id: Int,
    val model: String,
    val brand: String,
    val year: String,
    val color: String,
    val capacity: String,
    val updated_at: String,
    val carPic: String,
    val created_at: String
)
fun VehicleEntity.toResponse(): VehicleResponse {
    return VehicleResponse(
        id = this.id,
        plate = this.plate,
        driverId = this.driver_id,
        model = this.model,
        brand = this.brand,
        year = this.year,
        color = this.color,
        capacity = this.capacity,
        carPic = this.carPic,
        update_at = this.updated_at,
        created_at = this.created_at
    )
}