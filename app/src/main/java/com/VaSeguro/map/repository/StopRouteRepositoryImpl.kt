package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Driver.Driver
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopRoute
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.data.model.StopPassenger.StopPassenger
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para obtener datos de StopPassenger
 * Por el momento utiliza datos quemados
 */
class StopRouteRepositoryImpl: StopRouteRepository {

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
        ChildMap(
            id = 1,
            fullName = "Ana García",
            forenames = "Ana",
            surnames = "García",
            birth = "2016-05-10",
            age = 8,
            driverId = 1,
            parentId = 1,
            medicalInfo = "Sin alergias",
            createdAt = "2023-01-15",
            profilePic = null
        ),
        ChildMap(
            id = 2,
            fullName = "Carlos López",
            forenames = "Carlos",
            surnames = "López",
            birth = "2014-08-20",
            age = 10,
            driverId = 1,
            parentId = 2,
            medicalInfo = "Alergia a mariscos",
            createdAt = "2023-01-15",
            profilePic = null
        ),
        ChildMap(
            id = 3,
            fullName = "María Rodríguez",
            forenames = "María",
            surnames = "Rodríguez",
            birth = "2017-03-12",
            age = 7,
            driverId = 1,
            parentId = 3,
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
                latitude = 13.727814745719943,
                longitude = -89.20637232229953
            ),
            child = children[0],
            stopType = StopType.HOME
        ),
        StopPassenger(
            id = 2,
            stop = StopData(
                id = 2,
                name = "UCA",
                latitude = 13.68114721119151,
                longitude = -89.2360115785806
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
                name = "Little sisa picsa picsa",
                latitude = 13.68401102231903,
                longitude = -89.23577743059397
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

    // Datos quemados de StopRoute basados en mockStopPassengerList
    private val mockStopRoutes = listOf(
        // Routes for Child 1 (Ana García)
        StopRoute(
            id = 1,
            stopPassenger = mockStopPassengerList.find { it.id == 1 }!!,
            order = 1,
            state = true
        ),
        StopRoute(
            id = 2,
            stopPassenger = mockStopPassengerList.find { it.id == 2 }!!,
            order = 2,
            state = true
        ),
        StopRoute(
            id = 7,
            stopPassenger = mockStopPassengerList.find { it.id == 7 }!!,
            order = 3,
            state = false
        ),
        StopRoute(
            id = 10,
            stopPassenger = mockStopPassengerList.find { it.id == 10 }!!,
            order = 4,
            state = true
        ),

        // Routes for Child 2 (Carlos López)
        StopRoute(
            id = 3,
            stopPassenger = mockStopPassengerList.find { it.id == 3 }!!,
            order = 1,
            state = true
        ),
        StopRoute(
            id = 4,
            stopPassenger = mockStopPassengerList.find { it.id == 4 }!!,
            order = 2,
            state = true
        ),
        StopRoute(
            id = 8,
            stopPassenger = mockStopPassengerList.find { it.id == 8 }!!,
            order = 3,
            state = false
        ),
        StopRoute(
            id = 11,
            stopPassenger = mockStopPassengerList.find { it.id == 11 }!!,
            order = 4,
            state = true
        ),

        // Routes for Child 3 (María Rodríguez)
        StopRoute(
            id = 5,
            stopPassenger = mockStopPassengerList.find { it.id == 5 }!!,
            order = 1,
            state = true
        ),
        StopRoute(
            id = 6,
            stopPassenger = mockStopPassengerList.find { it.id == 6 }!!,
            order = 2,
            state = true
        ),
        StopRoute(
            id = 9,
            stopPassenger = mockStopPassengerList.find { it.id == 9 }!!,
            order = 3,
            state = false
        )
    )

    override fun getStopRoutesByChild(childId: Int): Flow<List<StopRoute>> = flow {
        // Filtrar las rutas por el ID del niño
        val childRoutes = mockStopRoutes.filter { stopRoute ->
            stopRoute.stopPassenger.child.id == childId
        }.sortedBy { it.order }

        emit(childRoutes)
    }
}
