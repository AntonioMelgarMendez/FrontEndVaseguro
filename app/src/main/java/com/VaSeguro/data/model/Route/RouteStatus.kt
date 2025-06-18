package com.VaSeguro.data.model.Route

/**
 * Representa los estados posibles de una ruta en el sistema.
 * Se implementa como un enum para seguridad de tipos, pero mantiene
 * compatibilidad con código que lo usaba como data class.
 */
enum class RouteStatus(val id: String, val status: String) {
  NO_INIT("1", "Sin iniciar"),
  ON_PROGRESS("2", "En progreso"),
  STOPED("3", "Detenida"),
  FINISHED("4", "Finalizada");

  companion object {
    fun fromString(value: String): RouteStatus {
      return when (value.lowercase()) {
        "sin iniciar", "no iniciada", "pendiente", "active" -> NO_INIT
        "en progreso", "en curso", "activa" -> ON_PROGRESS
        "detenida", "pausada", "suspendida", "inactive" -> STOPED
        "finalizada", "completada", "terminada" -> FINISHED
        else -> NO_INIT
      }
    }

    fun fromId(id: String): RouteStatus {
      return values().find { it.id == id } ?: NO_INIT
    }

    // Soporte para IDs numéricos (compatibilidad con código anterior)
    fun fromId(id: Int): RouteStatus {
      return fromId(id.toString())
    }

    fun getAll(): List<RouteStatus> = values().toList()

    // Constructor operator para mantener compatibilidad con código existente
    operator fun invoke(id: String, status: String): RouteStatus {
      // Intentamos encontrar el enum por id primero
      val byId = values().find { it.id == id }
      if (byId != null) return byId

      // Si no encontramos por ID, intentamos por nombre de estado
      return values().find { it.status == status } ?: NO_INIT
    }
  }
}
