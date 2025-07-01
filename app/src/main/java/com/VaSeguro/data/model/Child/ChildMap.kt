package com.VaSeguro.data.model.Child

import com.google.gson.annotations.SerializedName

data class ChildMap(
    val id: Int,
    val fullName: String = "",
    val forenames: String,
    val surnames: String,
    @SerializedName("birth_date")
    val birth: String,
    val age: Int = 0,
    @SerializedName("driver_id")
    val driverId: Int,
    @SerializedName("parent_id")
    val parentId: Int,
    @SerializedName("medical_info")
    val medicalInfo: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("profile_pic")
    val profilePic: String? = null,
    val gender: String = ""
) {
    // Propiedades calculadas para compatibilidad con código existente
    val calculatedFullName: String
        get() = if (fullName.isNotEmpty()) fullName else "$forenames $surnames"

    val calculatedAge: Int
        get() = if (age > 0) age else calculateAgeFromBirthDate(birth)

    private fun calculateAgeFromBirthDate(birthDate: String): Int {
        return try {
            val year = birthDate.substring(0, 4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - year
        } catch (e: Exception) {
            0
        }
    }
}
