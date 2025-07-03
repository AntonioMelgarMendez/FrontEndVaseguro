package com.VaSeguro.data.model.Child

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class ChildMap(
    val id: Int,
    // fullName no viene de la API, se construye localmente con forenames + surnames
    // Por eso no tiene @SerializedName y se marca como transient para Gson
    @Transient
    @Expose(serialize = false, deserialize = false)
    val fullName: String = "", // Campo local, no viene de la API
    val forenames: String? = null, // Puede venir null de la API
    val surnames: String? = null, // Puede venir null de la API
    @SerializedName("birth_date")
    val birthDate: String? = null, // Puede venir null de la API
    val age: Int = 0,
    @SerializedName("driver_id")
    val driverId: Int,
    @SerializedName("parent_id")
    val parentId: Int,
    @SerializedName("medical_info")
    val medicalInfo: String? = null, // Puede venir null de la API
    @SerializedName("created_at")
    val createdAt: String? = null, // Puede venir null de la API
    @SerializedName("profile_pic")
    val profilePic: String? = null,
    val gender: String? = null // Puede venir null de la API
) {
    // Esta es la propiedad principal que siempre debes usar para obtener el nombre completo
    val calculatedFullName: String
        get() {
            // Si no, construir el nombre usando forenames + surnames de forma segura
            val first = forenames?.takeIf { it.isNotBlank() } ?: "Sin nombre"
            val last = surnames?.takeIf { it.isNotBlank() } ?: "Sin apellido"
            return "$first $last"
        }

    val calculatedAge: Int
        get() = if (age > 0) age else calculateAgeFromBirthDate(birthDate)

    // Propiedad de compatibilidad para código que usa 'birth'
    val birth: String
        get() = birthDate ?: ""

    private fun calculateAgeFromBirthDate(birthDate: String?): Int {
        return try {
            if (birthDate.isNullOrBlank() || birthDate.length < 4) return 0
            val year = birthDate.substring(0, 4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - year
        } catch (e: Exception) {
            0
        }
    }
}
