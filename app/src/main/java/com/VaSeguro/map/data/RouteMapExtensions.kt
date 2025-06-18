package com.VaSeguro.map.data

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.model.Vehicle
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

/**
 * Extensiones para convertir entre Route (mapa) y RoutesData (base de datos)
 */

/**
 * Convierte un objeto Route (del mapa) a RoutesData (entidad de base de datos)
 * Añade la información básica de la ruta, generando un nuevo ID si no se proporciona
 */

val driverRole = UserRole(
    id = 1,
    role_name = "Driver"
)

val driver = UserData(
    id = "USR-001",
    forename = "Carlos",
    surname = "Ramírez",
    email = "carlos.ramirez@example.com",
    phoneNumber = "+50312345678",
    profilePic = null,
    role_id = driverRole,
    gender = "Male"
)

val burnedVehicle = Vehicle(
    id = "VEH-002",
    plate = "P987654",
    model = "Toyota Hiace 2020",
    driver_id = driver,
    created_at = "2025-06-16T09:00:00"
)

fun Route.toRoutesData(
    id: String = UUID.randomUUID().toString(),
    name: String = this.getRouteName(),
    vehicleId: String = "",
    startDate: String = "",
    endDate: String = "",
    statusId: Int = 1, // Por defecto, estado "programada"
    typeId: Int = 1,   // Por defecto, tipo "escolar"
    stopRoutes: List<StopRoute> = emptyList()
): RoutesData {
    return RoutesData(
        id = id,
        name = name,
        start_date = startDate,
        vehicle_id = burnedVehicle,
        status_id = RouteStatus.fromId(statusId),
        type_id = RouteType.fromId(typeId),
        end_date = endDate,
        stopRoute = stopRoutes
    )
}

/**
 * Actualiza un objeto RoutesData existente con la información de un Route
 * Mantiene la información original y solo actualiza los campos relacionados con la ruta
 */
fun RoutesData.updateFromRoute(route: Route): RoutesData {
    // Mantenemos los datos originales y actualizamos solo lo necesario
    return this.copy(
        // Los campos que queramos actualizar
        name = route.getRouteName() // Por ejemplo, actualizar solo el nombre
    )
}

/**
 * Crea un objeto StopRoute a partir de información básica
 * Esta es una función auxiliar para crear objetos StopRoute con datos temporales
 * que luego serán reemplazados con datos reales
 */
fun createTemporaryStopRoute(
    id: Int,
    stopData: StopData,
    child: Child,
    stopType: StopType,
    order: Int,
    state: Boolean = true
): StopRoute {
    // Creamos primero el StopPassenger
    val stopPassenger = StopPassenger(
        id = id,
        stop = stopData,
        child = child,
        stopType = stopType
    )

    // Creamos el StopRoute usando el StopPassenger
    return StopRoute(
        id = id,
        stopPassenger = stopPassenger,
        order = order,
        state = state
    )
}

/**
 * Convierte los segmentos de una ruta en puntos de parada temporales
 * Nota: Estos son datos parciales y tendrán que ser completados con información real de niños
 */
fun Route.createTemporaryStopData(): List<StopData> {
    val stops = mutableListOf<StopData>()

    if (segments.isNotEmpty()) {
        // Crear parada para cada punto en los segmentos
        segments.forEachIndexed { index, segment ->
            // Punto de inicio (excepto para el primer segmento que ya se incluye como fin del anterior)
            if (index == 0) {
                stops.add(
                    StopData(
                        id = index * 2,
                        latitude = segment.startPoint.latitude,
                        longitude = segment.startPoint.longitude,
                        name = segment.startPointName.ifEmpty { "Punto ${index + 1}" },
                    )
                )
            }

            // Punto de fin
            stops.add(
                StopData(
                    id = index * 2 + 1,
                    latitude = segment.endPoint.latitude,
                    longitude = segment.endPoint.longitude,
                    name = segment.endPointName.ifEmpty { "Punto ${index + 2}" },
                )
            )
        }
    }

    return stops
}

/**
 * Convierte una lista de StopRoute en puntos para construir un objeto Route
 * Útil para reconstruir la ruta desde la base de datos
 */
fun List<StopRoute>.toRoutePoints(): List<RoutePoint> {
    return this.sortedBy { stopRoute ->
        this.indexOf(stopRoute) // Opcional si ya está ordenado
    }.map { stopRoute ->
        RoutePoint(
            location = LatLng(
                stopRoute.stopPassenger.stop.latitude,
                stopRoute.stopPassenger.stop.longitude
            ),
            name = stopRoute.stopPassenger.stop.name
        )
    }
}

/**
 * Obtiene el nombre de estado según su ID
 */
private fun getStatusName(statusId: Int): String {
    return when (statusId) {
        1 -> "Programada"
        2 -> "En Progreso"
        3 -> "Completada"
        4 -> "Cancelada"
        else -> "Desconocido"
    }
}

/**
 * Obtiene el nombre de tipo según su ID
 */
private fun getTypeName(typeId: Int): String {
    return when (typeId) {
        1 -> "Escolar"
        2 -> "Especial"
        3 -> "Turística"
        else -> "Otro"
    }
}
