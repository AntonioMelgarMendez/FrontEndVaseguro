package com.VaSeguro.data.model.Children

import com.VaSeguro.data.model.Child.Child

data class Children(
    val forenames: String,
    val surnames: String,
    val birth_date: String,
    val medical_info: String,
    val parent_id: Int,
    val driver_id: Int,
    val gender: String,
    val profile_pic: String? = null,

)

fun Children.toChild(
    id: Int,
    parentName: String,
    driverName: String,
    createdAt: String = "N/A"
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
        createdAt = createdAt,
        profilePic = profile_pic
    )
}

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
