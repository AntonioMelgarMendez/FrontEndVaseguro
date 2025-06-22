
package com.VaSeguro.data.remote.Vehicle

import com.VaSeguro.data.model.Vehicle.Vehicle
import com.google.gson.annotations.SerializedName

data class VehicleResponse(
    val id: Int,
    val plate: String,
    val model: String,
    val brand: String,
    val year: String,
    val color: String,
    val capacity: String,
    @SerializedName("driver_id")
    val driverId: Int,
    @SerializedName("car_pic")
    val carPic: String?,
    val update_at: String,
    val created_at: String
)

fun VehicleResponse.toDomain(): Vehicle {
    return Vehicle(
        id = id.toString(),
        plate = plate,
        driver_id = driverId.toString(),
        model = model,
        brand = brand,
        year = year,
        color = color,
        capacity = capacity,
        updated_at = update_at,
        created_at = created_at,
        carPic = carPic ?: ""
    )
}