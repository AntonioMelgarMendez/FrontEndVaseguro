package com.VaSeguro.data.model.Route

/**
 * Representa los tipos de rutas disponibles en el sistema.
 * Se implementa como un enum para seguridad de tipos, pero mantiene
 * compatibilidad con código que lo usaba como data class.
 */
enum class RouteType(val id: Int, val type: String) {
    INBOUND(1, "Ida"),
    OUTBOUND(2, "Vuelta"),
    SPECIAL(3, "Especial");


    companion object {
        fun fromString(value: String): RouteType {
            return when (value.lowercase()) {
                "ida", "inicio", "matutina", "long distance" -> INBOUND
                "vuelta", "regreso", "retorno", "short distance" -> OUTBOUND
                "especial", "special", "evento" -> SPECIAL
                else -> INBOUND
            }
        }

        fun fromId(id: Int): RouteType {
            return values().find { it.id == id } ?: INBOUND
        }



        fun getAll(): List<RouteType> = values().toList()

        // Constructor operator para mantener compatibilidad con código existente
        operator fun invoke(id: Int, type: String): RouteType {
            // Intentamos encontrar el enum por id primero
            val byId = values().find { it.id == id }
            if (byId != null) return byId

            // Si no encontramos por ID, intentamos por tipo
            return values().find { it.type == type } ?: INBOUND
        }
    }
}
