package com.VaSeguro.data.Entitys.Stops

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey val id: Int,
    val driver_id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)