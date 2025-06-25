package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.data.model.Route.RouteType
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class SavedRoutesRepositoryImpl: SavedRoutesRepository {
    // Utilizar el repositorio de StopPassenger para obtener los datos consistentes
    private val stopPassengerRepository = StopPassengerRepositoryImpl()

    private val _savedRoutes = MutableStateFlow<List<RoutesData>>(emptyList())
    val savedRoutes: Flow<List<RoutesData>> = _savedRoutes

    init {
        // Inicializar con las rutas de ejemplo
        runBlocking {
            _savedRoutes.value = createMockRoutes()
        }
    }

    override fun addRoute(route: RoutesData) {
        _savedRoutes.update { currentList ->
            currentList + route
        }
    }

    override fun updateRoute(route: RoutesData) {
        _savedRoutes.update { currentList ->
            currentList.map {
                if (it.id == route.id) route else it
            }
        }
    }

    override fun deleteRoute(routeId: Int) {
        _savedRoutes.update { currentList ->
            currentList.filter { it.id != routeId }
        }
    }

    override fun getRoute(routeId: Int): Flow<RoutesData?> {
        return _savedRoutes.map { routes ->
            routes.find { it.id == routeId }
        }
    }

    override suspend fun createMockRoutes(): List<RoutesData> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = Date()

        // Obtener los StopPassenger del repositorio utilizando .first() para convertir el Flow en una lista
        val stopPassengers = stopPassengerRepository.getAllStopPassengers().first()

        // Agrupar las paradas por niño para crear rutas coherentes
        val child1Stops = stopPassengers.filter { it.child.id == 1 }
        val child2Stops = stopPassengers.filter { it.child.id == 2 }
        val child3Stops = stopPassengers.filter { it.child.id == 3 }

        val driverRole = UserRole(
            id = 2,
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

        val burnedVehicle = VehicleMap(
            id = 2,
            plate = "P987654",
            model = "Toyota Hiace 2020",
            driver_id = driver.id,
            created_at = "2025-06-16T09:00:00",
            brand = "Toyota",
            year = "2020",
            color = "White",
            capacity = "20",
            updated_at = "2025-06-16T09:00:00",
            carPic = "https://example.com/toyota_hiace_2020.jpg"
        )

        // Crear rutas con diferentes combinaciones de paradas
        return listOf(
            // Ruta 1: Mañana - Recogida de niños para llevarlos a la escuela
            RoutesData(
                id = Random.nextInt(1, 9999),
                name = "Ruta Escolar Mañana",
                start_date = dateFormat.format(Date(now.time - 24 * 60 * 60 * 1000)), // ayer
                vehicle_id = burnedVehicle,
                status_id = RouteStatus.NO_INIT,
                type_id = RouteType.INBOUND,
                end_date = dateFormat.format(Date(now.time - 23 * 60 * 60 * 1000)),
                stopRoute = listOf(
                    // Primero recogemos a los niños de sus casas
                    StopRoute(1, findStopByTypeAndChild("HOME", child1Stops), 1, true),
                    StopRoute(2, findStopByTypeAndChild("HOME", child2Stops), 2, true),
                    StopRoute(3, findStopByTypeAndChild("HOME", child3Stops), 3, true),
                    // Luego los dejamos en sus respectivas instituciones
                    StopRoute(4, findStopByTypeAndChild("INSTITUTION", child1Stops), 4, true),
                    StopRoute(5, findStopByTypeAndChild("INSTITUTION", child2Stops), 5, true),
                    StopRoute(6, findStopByTypeAndChild("INSTITUTION", child3Stops), 6, true)
                )
            ),

            // Ruta 2: Tarde - Recoger niños de la escuela y llevarlos a casa
            RoutesData(
                id = Random.nextInt(1, 9999),
                name = "Ruta Escolar Tarde",
                start_date = dateFormat.format(now), // hoy
                vehicle_id = burnedVehicle,
                status_id = RouteStatus.FINISHED,
                type_id = RouteType.INBOUND,
                end_date = "",
                stopRoute = listOf(
                    // Primero recogemos a los niños de sus instituciones
                    StopRoute(7, findStopByTypeAndChild("INSTITUTION", child1Stops), 1, true),
                    StopRoute(8, findStopByTypeAndChild("INSTITUTION", child2Stops), 2, true),
                    StopRoute(9, findStopByTypeAndChild("INSTITUTION", child3Stops), 3, true),
                    // Luego los llevamos a sus casas
                    StopRoute(10, findStopByTypeAndChild("HOME", child1Stops), 4, true),
                    StopRoute(11, findStopByTypeAndChild("HOME", child2Stops), 5, true),
                    StopRoute(12, findStopByTypeAndChild("HOME", child3Stops), 6, true)
                )
            ),

            // Ruta 3: Especial - Solo para el niño 1 y 2
            RoutesData(
                id = Random.nextInt(1, 9999),
                name = "Ruta Especial Centro",
                start_date = dateFormat.format(Date(now.time + 24 * 60 * 60 * 1000)), // mañana
                vehicle_id = burnedVehicle,
                status_id = RouteStatus.NO_INIT,
                type_id = RouteType.INBOUND,
                end_date = "",
                stopRoute = listOf(
                    // Solo incluimos al niño 1 y 2
                    StopRoute(13, findStopByTypeAndChild("HOME", child1Stops), 1, true),
                    StopRoute(14, findStopByTypeAndChild("HOME", child2Stops), 2, true),
                    StopRoute(15, findStopByTypeAndChild("INSTITUTION", child1Stops), 3, true),
                    StopRoute(16, findStopByTypeAndChild("INSTITUTION", child2Stops), 4, true)
                )
            )
        )
    }

    // Función helper para encontrar un StopPassenger por tipo y lista de stops de un niño
    override fun findStopByTypeAndChild(type: String, childStops: List<StopPassenger>): StopPassenger {
        return childStops.firstOrNull { it.stopType.name == type }
            ?: throw IllegalStateException("No se encontró parada de tipo $type para el niño")
    }
}
