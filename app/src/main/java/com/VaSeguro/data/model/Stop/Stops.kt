package com.VaSeguro.data.model.Stop


data class Stops(
    val driver_id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)
data class StopDto(
    val id: Int,
    val driver_id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)