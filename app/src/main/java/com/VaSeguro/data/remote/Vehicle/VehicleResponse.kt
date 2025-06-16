package com.VaSeguro.data.remote.Vehicle

import com.VaSeguro.data.model.Vehicle.Vehicle

data class VehicleResponse(
    val id: Int,
    val plate: String,
    val model: String,
    val brand: String,
    val year: String,
    val color: String,
    val capacity: String,
    val driverId: Int,
    val carPicUrl: String?,
    val updated_at: String,
    val created_at: String,
    val driver_id: Int,
    val carPic: String? = null
)

fun VehicleResponse.toDomain(): Vehicle {
    return Vehicle(
        id = id.toString(),
        plate = plate,
        driver_id = driver_id.toString(),
        model = model,
        brand = brand,
        year = year,
        color = color,
        capacity = capacity,
        updated_at = updated_at,
        created_at = created_at,
        carPic = carPic.toString()
    )
}