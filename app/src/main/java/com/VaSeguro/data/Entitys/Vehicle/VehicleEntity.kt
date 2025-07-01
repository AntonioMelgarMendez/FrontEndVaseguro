package com.VaSeguro.data.Entitys.Vehicle

import androidx.room.Entity
import androidx.room.PrimaryKey

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