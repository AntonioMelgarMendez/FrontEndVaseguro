package com.VaSeguro.data.Entitys.DriverCode

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_code")
data class DriverCodeEntity(
    @PrimaryKey val id: Int = 0,
    val code: String
)