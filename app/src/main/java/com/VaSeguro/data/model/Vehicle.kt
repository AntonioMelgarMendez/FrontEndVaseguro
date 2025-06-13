package com.VaSeguro.data.model

import com.VaSeguro.data.model.User.UserData

data class Vehicle (
  val id: String,
  val plate: String,
  val model: String,
  val driver_id: UserData,
  val created_at:String
)