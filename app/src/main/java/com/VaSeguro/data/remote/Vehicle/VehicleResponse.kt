package com.VaSeguro.data.remote.Vehicle

data class SimpleResponse(
    val success: Boolean,
    val message: String? = null
)

data class VehicleResponse(
    val id: Int,
    val plate: String,
    val model: String,
    val brand: String,
    val year: String,
    val color: String,
    val capacity: String,
    val driverId: Int,
    val carPicUrl: String?
)