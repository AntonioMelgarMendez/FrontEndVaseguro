package com.VaSeguro.data.model.Vehicle

data class VehicleCreateRequest(
  val plate: String,
  val driverId: String,
  val model: String,
  val brand: String,
  val year: String,
  val color: String,
  val capacity: String,
  val carPic: String
)