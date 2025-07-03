package com.VaSeguro.data.Entitys.Routes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val start_date: String,
    val end_date: String?,
    val vehicle_id: Int,
    val status_id: Int,
    val type_id: Int
)