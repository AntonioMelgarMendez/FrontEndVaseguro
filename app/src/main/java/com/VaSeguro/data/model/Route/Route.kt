package com.VaSeguro.data.model.Route

import com.VaSeguro.data.model.Vehicle

data class Route (
  val id: String,
  val name: String,
  val start_date: String,
  val vehicle_id: Vehicle,
  val route_status: RouteStatus,
  val route_type: RouteType,
  val end_date: String,
)