package com.VaSeguro.data.remote.Responses

data class UserDataResponse(
  val id: String,
  val forename: String,
  val surname: String,
  val email: String,
  val phoneNumber: String,
  val profilePic: String? = null,
  val gender: String? = null,
  val role: String
)