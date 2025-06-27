package com.VaSeguro.data.remote.Responses

import com.VaSeguro.data.model.Child.Child

data class ChildrenResponse(
  val id: Int,
  val forenames: String,
  val surnames: String,
  val birth_date: String,
  val medical_info: String,
  val parent_id: Int,
  val driver_id: Int,
  val gender: String,
  val profile_pic: String? = null,
  val created_at: String? = null
)

fun calculateAge(birthDate: String): Int {
  return try {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val birth = java.time.LocalDate.parse(birthDate, formatter)
    val today = java.time.LocalDate.now()
    java.time.Period.between(birth, today).years
  } catch (e: Exception) {
    0
  }
}

fun ChildrenResponse.toChild(
  parentName: String,
  driverName: String
): Child {
  return Child(
    id = id,
    fullName = "$forenames $surnames",
    forenames = forenames,
    surnames = surnames,
    birth = birth_date,
    age = calculateAge(birth_date),
    driver = driverName,
    parent = parentName,
    medicalInfo = medical_info,
    createdAt = created_at ?: "N/A",
    profilePic = profile_pic
  )
}