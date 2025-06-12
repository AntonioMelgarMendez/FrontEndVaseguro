package com.VaSeguro.data.remote.Responses

import com.VaSeguro.data.model.Vehicle.Vehicle

data class VehicleResponse(
  val id: String,
  val plate: String,
  val driver_id: String,
  val model: String,
  val brand: String,
  val year: String,
  val color: String,
  val capacity: String,
  val updated_at: String,
  val created_at: String,
  val carPic: String,
)

fun VehicleResponse.toDomain(): Vehicle {
  return Vehicle(
    id = id,
    plate = plate,
    driver_id = driver_id,
    model = model,
    brand = brand,
    year = year,
    color = color,
    capacity = capacity,
    updated_at = updated_at,
    created_at = created_at,
    carPic = carPic
  )
}