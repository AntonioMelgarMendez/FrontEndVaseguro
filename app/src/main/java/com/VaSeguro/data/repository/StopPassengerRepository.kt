package com.VaSeguro.data.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para obtener datos de StopPassenger
 * Por el momento utiliza datos quemados
 */
class StopPassengerRepository {

    // Driver quemado para las rutas
    private val driver = Driver(
        id = 1,
        name = "Juan Pérez",
        phoneNumber = "7777-7777",
        address = "Santa Tecla, La Libertad",
        user_id = "driver_123"
    )

    // Children quemados para las paradas
    private val children = listOf(
        Child(
            id = 1,
            fullName = "Ana García",
            forenames = "Ana",
            surnames = "García",
            birth = "2016-05-10",
            age = 8,
            driver = "driver_123",
            parent = "parent_101",
            medicalInfo = "Sin alergias",
            createdAt = "2023-01-15",
            profilePic = null
        ),
        Child(
            id = 2,
            fullName = "Carlos López",
            forenames = "Carlos",
            surnames = "López",
            birth = "2014-08-20",
            age = 10,
            driver = "driver_123",
            parent = "parent_102",
            medicalInfo = "Alergia a mariscos",
            createdAt = "2023-01-15",
            profilePic = null
        ),
        Child(
            id = 3,
            fullName = "María Rodríguez",
            forenames = "María",
            surnames = "Rodríguez",
            birth = "2017-03-12",
            age = 7,
            driver = "driver_123",
            parent = "parent_103",
            medicalInfo = "Asma leve",
            createdAt = "2023-01-15",
            profilePic = null
        )
    )

    // Datos quemados de StopPassenger
    private val mockStopPassengerList = listOf(
        // Child 1
        StopPassenger(
            id = 1,
            stop = StopData(
                id = 1,
                name = "Casa 1",
                latitude = 13.719149334657295,
                longitude = -89.18715776408806
            ),
            child = children[0],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 2,
            stop = StopData(
                id = 2,
                name = "Metrocentro",
                latitude = 13.705982000097556,
                longitude = -89.2115589593364
            ),
            child = children[0],
            stopType = StopType.INSTITUTION
        ),

        // Child 2
        StopPassenger(
            id = 3,
            stop = StopData(
                id = 3,
                name = "Hogar Carlos López",
                latitude = 13.732546595398654,
                longitude = -89.2043012490677
            ),
            child = children[1],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 4,
            stop = StopData(
                id = 4,
                name = "Metropolis",
                latitude = 13.729798472776263,
                longitude = -89.21071148925459
            ),
            child = children[1],
            stopType = StopType.INSTITUTION
        ),

        // Child 3
        StopPassenger(
            id = 5,
            stop = StopData(
                id = 5,
                name = "Hogar María",
                latitude = 13.722552347893282,
                longitude = -89.22408033312668
            ),
            child = children[2],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 6,
            stop = StopData(
                id = 6,
                name = "Walmart",
                latitude = 13.736171873258105,
                longitude = -89.21656426992779
            ),
            child = children[2],
            stopType = StopType.INSTITUTION
        ),
        // Puntos 100% válidos en El Salvador para pruebas
        StopPassenger(
            id = 7,
            stop = StopData(
                id = 7,
                name = "Divino Salvador del Mundo",
                latitude = 13.6989,
                longitude = -89.2244
            ),
            child = children[0],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 8,
            stop = StopData(
                id = 8,
                name = "Plaza Futura",
                latitude = 13.7130,
                longitude = -89.2426
            ),
            child = children[1],
            stopType = StopType.INSTITUTION
        ),
        StopPassenger(
            id = 9,
            stop = StopData(
                id = 9,
                name = "UES",
                latitude = 13.7054,
                longitude = -89.2032
            ),
            child = children[2],
            stopType = StopType.INSTITUTION
        ),
        StopPassenger(
            id = 10,
            stop = StopData(
                id = 10,
                name = "Multiplaza",
                latitude = 13.6761,
                longitude = -89.2542
            ),
            child = children[0],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 11,
            stop = StopData(
                id = 11,
                name = "Aeropuerto",
                latitude = 13.4406,
                longitude = -89.0557
            ),
            child = children[1],
            stopType = StopType.INSTITUTION
        )
    ).distinctBy { Pair(it.stop.latitude, it.stop.longitude) }

    /**
     * Obtiene todos los StopPassenger disponibles
     */
    fun getAllStopPassengers(): Flow<List<StopPassenger>> = flow {
        emit(mockStopPassengerList)
    }

    /**
     * Obtiene StopPassenger filtrados por tipo (HOME o INSTITUTION)
     */
    fun getStopPassengersByType(type: StopType): Flow<List<StopPassenger>> = flow {
        emit(mockStopPassengerList.filter { it.stopType == type })
    }

    /**
     * Obtiene StopPassenger para un niño específico
     */
    fun getStopPassengersByChild(childId: Int): Flow<List<StopPassenger>> = flow {
        emit(mockStopPassengerList.filter { it.child.id == childId })
    }

    /**
     * Convierte un StopPassenger a LatLng para usar en el mapa
     */
    fun stopPassengerToLatLng(stopPassenger: StopPassenger): LatLng {
        return LatLng(stopPassenger.stop.latitude, stopPassenger.stop.longitude)
    }

    /**
     * Obtiene el driver asignado
     */
    fun getDriver(): Driver = driver
}
