package com.VaSeguro.data.model.Vehicle

data class VehicleMap (
  val id: Int,
  val plate: String,
  val driver_id: String,
  val model: String,
  val brand: String,
  val year: String,
  val color: String,
  val capacity: String,
  val updated_at: String,
  val carPic: String,
  val created_at:String
)

fun Vehicle.toVehicleMap(): VehicleMap = VehicleMap(
  id = this.id.toInt(),
  plate = this.plate,
  driver_id = this.driver_id,
  model = this.model,
  brand = this.brand,
  year = this.year,
  color = this.color,
  capacity = this.capacity,
  updated_at = this.updated_at,
  carPic = this.carPic,
  created_at = this.created_at
)