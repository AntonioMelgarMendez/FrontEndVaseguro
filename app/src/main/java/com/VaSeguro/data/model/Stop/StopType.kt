package com.VaSeguro.data.model.Stop

enum class StopType(val id: String, val type: String) {
    HOME("1", "Hogar"),
    INSTITUTION("2", "Institución");

    companion object {
        fun fromString(value: String): StopType {
            return when (value.lowercase()) {
                "hogar", "home", "casa", "house" -> HOME
                "institución", "institution", "escuela", "school" -> INSTITUTION
                else -> HOME
            }
        }

        fun fromId(id: String): StopType {
            return values().find { it.id == id } ?: HOME
        }

        fun getAll(): List<StopType> = values().toList()
    }
}
