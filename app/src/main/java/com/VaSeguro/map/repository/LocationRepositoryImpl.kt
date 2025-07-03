package com.VaSeguro.map.repository

import com.VaSeguro.data.model.Route.RouteStatus
import com.VaSeguro.map.data.ApiPlaceResult
import com.VaSeguro.map.data.Geometry
import com.VaSeguro.map.data.Location
import com.VaSeguro.map.data.LocationAddress
import com.VaSeguro.map.data.PlaceResult
import com.VaSeguro.map.data.Route
import com.VaSeguro.map.data.Waypoint
import com.VaSeguro.map.data.driver
import com.VaSeguro.map.request.RouteRequest
import com.VaSeguro.map.services.MapsApiService
import com.VaSeguro.map.services.RoutesApiService
import com.google.android.gms.maps.model.LatLng
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import okhttp3.Dispatcher
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LocationRepositoryImpl(
    private val client: SupabaseClient,
) : LocationRepository {

    // StateFlow para mantener la última ubicación del conductor conocida
    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()

    // Canal de tiempo real para suscripciones de Supabase
    private var realtimeChannel: RealtimeChannel? = null

    // Scope para operaciones asincrónicas
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun updateLocation(
        driverId: Int,
        lat: Double,
        lon: Double
    ) {
        // Crear objeto serializable con valores por defecto seguros
        val locationUpdate = LocationDriverAddress(
            driver_id = driverId,
            latitude = lat,
            longitude = lon,
            updated_at = System.now().toString(),
            encoded_polyline = null,
            route_active = false,
            route_progress = 0.0f,
            current_segment = 0,
            route_status = null
        )

        client.postgrest.from("location").upsert(locationUpdate)
    }

    // MODIFICADO: Se asegura que el valor routeActive se actualice correctamente
    override suspend fun updateLocationWithRoute(
        driverId: Int,
        lat: Double,
        lon: Double,
        encodedPolyline: String?,
        routeActive: Boolean,
        routeProgress: Float,
        currentSegment: Int,
        routeStatus: Int?
    ) {
        // Asegurarse de que los valores sean consistentes
        // Si encodedPolyline es null o routeProgress es 0, routeActive debe ser false
        val effectiveRouteActive = if (encodedPolyline == null || routeProgress == 0.0f) false else routeActive

        // NUEVO: Forzar values explícitos cuando se está limpiando la ruta
        val finalRouteActive = if (encodedPolyline == null && routeProgress == 0.0f && currentSegment == 0) {
            false // Forzar false cuando está limpiando
        } else {
            effectiveRouteActive
        }

        // NUEVO: Detectar si esta es una operación de limpieza
        val isClearingRoute = encodedPolyline == null && routeProgress == 0.0f && currentSegment == 0 && routeStatus == RouteStatus.NO_INIT.id

        val locationUpdate = LocationDriverAddress(
            driver_id = driverId,
            latitude = lat,
            longitude = lon,
            updated_at = System.now().toString(),
            encoded_polyline = encodedPolyline,
            route_active = finalRouteActive, // Usamos el valor final
            route_progress = routeProgress,
            current_segment = currentSegment,
            route_status = if (routeStatus != null) RouteStatus.fromId(routeStatus).status else null
        )

        try {
            // Añadir log detallado para depuración
            println("=== ACTUALIZANDO UBICACIÓN CON RUTA ===")
            println("Driver ID: $driverId")
            println("Lat: $lat, Lon: $lon")
            println("Encoded Polyline: ${encodedPolyline ?: "NULL"}")
            println("Route Active (original): $routeActive")
            println("Route Active (effective): $effectiveRouteActive")
            println("Route Active (final): $finalRouteActive")
            println("Route Progress: $routeProgress")
            println("Current Segment: $currentSegment")
            println("Route Status ID: $routeStatus")
            println("Route Status String: ${locationUpdate.route_status}")
            println("Es operación de limpieza: $isClearingRoute")
            println("Objeto completo a enviar: $locationUpdate")
            println("=======================================")

            // NUEVO: Para operaciones de limpieza, usar update() en lugar de upsert()
            val result = if (isClearingRoute) {
                println("DEBUG_CLEAR: Usando UPDATE para operación de limpieza")

                // Primero verificar que el registro existe
                val existingRecord = try {
                    client.postgrest.from("location")
                        .select {
                            filter {
                                eq("driver_id", driverId)
                            }
                        }.decodeSingleOrNull<LocationDriverAddress>()
                } catch (e: Exception) {
                    println("DEBUG_CLEAR: Error al verificar registro existente: ${e.message}")
                    null
                }

                if (existingRecord != null) {
                    println("DEBUG_CLEAR: Registro existente encontrado: $existingRecord")

                    // NUEVO: Intentar múltiples estrategias para asegurar la actualización
                    try {
                        // Estrategia 1: UPDATE específico
                        println("DEBUG_CLEAR: Intentando UPDATE específico...")
                        val updateResult = client.postgrest.from("location")
                            .update(locationUpdate) {
                                filter {
                                    eq("driver_id", driverId)
                                }
                            }
                        println("DEBUG_CLEAR: UPDATE específico completado")

                        // Verificar inmediatamente si funcionó
                        delay(200)
                        val postUpdateCheck = client.postgrest.from("location")
                            .select {
                                filter {
                                    eq("driver_id", driverId)
                                }
                            }.decodeSingle<LocationDriverAddress>()

                        println("DEBUG_CLEAR: Verificación inmediata post-UPDATE: $postUpdateCheck")

                        // Si aún no coinciden los valores, intentar DELETE + INSERT
                        val valuesStillNotMatch = postUpdateCheck.route_active != finalRouteActive ||
                                postUpdateCheck.current_segment != currentSegment ||
                                postUpdateCheck.route_progress != routeProgress ||
                                postUpdateCheck.encoded_polyline != encodedPolyline

                        if (valuesStillNotMatch) {
                            println("DEBUG_CLEAR: UPDATE falló, intentando DELETE + INSERT...")

                            // Estrategia 2: DELETE + INSERT
                            client.postgrest.from("location")
                                .delete {
                                    filter {
                                        eq("driver_id", driverId)
                                    }
                                }

                            delay(200)

                            client.postgrest.from("location").insert(locationUpdate)
                            println("DEBUG_CLEAR: DELETE + INSERT completado")
                        }

                        updateResult

                    } catch (e: Exception) {
                        println("DEBUG_CLEAR: Error en UPDATE específico: ${e.message}")
                        println("DEBUG_CLEAR: Intentando fallback con UPSERT...")
                        client.postgrest.from("location").upsert(locationUpdate)
                    }
                } else {
                    println("DEBUG_CLEAR: No se encontró registro existente, usando INSERT")
                    client.postgrest.from("location").insert(locationUpdate)
                }
            } else {
                println("DEBUG_NORMAL: Usando UPSERT para operación normal")
                client.postgrest.from("location").upsert(locationUpdate)
            }

            println("=== RESULTADO DE ACTUALIZACIÓN ===")
            println("Operación completada exitosamente")
            println("Tipo de operación: ${if (isClearingRoute) "UPDATE" else "UPSERT"}")
            println("Data enviada: $locationUpdate")

            // NUEVO: Intentar recuperar el registro actualizado para verificar
            try {
                delay(500) // Esperar medio segundo
                val verificationRecord = client.postgrest.from("location")
                    .select {
                        filter {
                            eq("driver_id", driverId)
                        }
                    }.decodeSingle<LocationDriverAddress>()

                println("=== VERIFICACIÓN POST-ACTUALIZACIÓN ===")
                println("Registro verificado: $verificationRecord")
                println("route_active en DB: ${verificationRecord.route_active}")
                println("current_segment en DB: ${verificationRecord.current_segment}")
                println("route_progress en DB: ${verificationRecord.route_progress}")
                println("encoded_polyline en DB: ${verificationRecord.encoded_polyline ?: "NULL"}")
                println("route_status en DB: ${verificationRecord.route_status ?: "NULL"}")

                // Verificar si los valores coinciden con lo que intentamos guardar
                val valuesMatch = verificationRecord.route_active == finalRouteActive &&
                        verificationRecord.current_segment == currentSegment &&
                        verificationRecord.route_progress == routeProgress &&
                        verificationRecord.encoded_polyline == encodedPolyline

                println("¿Los valores coinciden?: $valuesMatch")
                if (!valuesMatch) {
                    println("⚠️ ADVERTENCIA: Los valores en la base de datos NO coinciden con los enviados!")
                    println("Expected: route_active=$finalRouteActive, current_segment=$currentSegment, route_progress=$routeProgress")
                    println("Actual: route_active=${verificationRecord.route_active}, current_segment=${verificationRecord.current_segment}, route_progress=${verificationRecord.route_progress}")
                }
                println("==========================================")

            } catch (e: Exception) {
                println("ERROR en verificación post-actualización: ${e.message}")
            }

            println("===================================")

        } catch (e: Exception) {
            println("=== ERROR EN ACTUALIZACIÓN ===")
            println("Error al actualizar ubicación con ruta: ${e.message}")
            println("Stack trace completo:")
            e.printStackTrace()

            // NUEVO: Información adicional del error
            println("Detalles del error:")
            println("- Tipo de error: ${e.javaClass.simpleName}")
            println("- Mensaje: ${e.localizedMessage}")
            println("- Causa: ${e.cause?.message ?: "N/A"}")

            if (e.message?.contains("JSON", ignoreCase = true) == true) {
                println("🔍 Posible problema de serialización JSON")
                println("Objeto que causó el problema: $locationUpdate")
            }

            println("===============================")
            throw e // Re-lanzar la excepción para que el llamador pueda manejarla
        }
    }

    override suspend fun getDriverLocation(driverId: Int): LocationDriverAddress {
        return withContext(Dispatchers.IO) {
            client.postgrest
                .from("location")
                .select {
                    filter {
                        eq("driver_id", driverId)
                    }
                }.decodeSingle<LocationDriverAddress>()
        }
    }

    // NUEVO: Método para obtener ubicación completa con información de ruta
    override suspend fun getDriverLocationWithRoute(driverId: Int): LocationDriverAddress {
        return getDriverLocation(driverId) // Ambos métodos son iguales ahora
    }

    override fun subscribeToDriverLocationUpdates(driverId: Int): Flow<LatLng> = callbackFlow {
        // Cerrar canal anterior si existe
        unsubscribeFromLocationUpdates()

        try {
            // Obtener la ubicación inicial
            val initialLocation = getDriverLocation(driverId)
            _driverLocation.value = LatLng(initialLocation.latitude, initialLocation.longitude)
            send(LatLng(initialLocation.latitude, initialLocation.longitude)) // Enviar ubicación inicial al flow

            // Crear un nuevo canal de suscripción usando el realtime adecuadamente
            realtimeChannel = client.channel("driver_location_updates_$driverId")

            // Configurar la suscripción al canal
            val changes = realtimeChannel!!.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "location"
                filter("driver_id", FilterOperator.EQ, driverId)
            }

            // Job para manejar cambios
            val job = changes.onEach { update ->
                try {
                    // Extraer los datos actualizados del registro
                    val record = update.record
                    if (record != null) {
                        // Convertir el registro a LatLng explícitamente
                        val latitude = record["latitude"].toString().toDoubleOrNull() ?: 0.0
                        val longitude = record["longitude"].toString().toDoubleOrNull() ?: 0.0
                        val newLocation = LatLng(latitude, longitude)

                        // Log para debug
                        println("Nueva ubicación recibida: $latitude, $longitude")

                        // Actualizar el StateFlow y enviar la nueva ubicación
                        _driverLocation.value = newLocation
                        send(newLocation)
                    }
                } catch (e: Exception) {
                    println("Error procesando actualización: ${e.message}")
                    e.printStackTrace()
                }
            }.launchIn(coroutineScope)

            // Activar la suscripción explícitamente
            realtimeChannel?.subscribe()
            println("Suscripción activada para driver_id: $driverId")

            // Cancelar la suscripción cuando se cierre el Flow
            awaitClose {
                println("Cerrando suscripción a actualizaciones de ubicación")
                job.cancel()
                unsubscribeFromLocationUpdates()
            }
        } catch (e: Exception) {
            println("Error al suscribirse a actualizaciones de ubicación: ${e.message}")
            e.printStackTrace()
            close(e)
        }
    }

    // NUEVO: Método para suscribirse a cambios completos (ubicación + ruta)
    override fun subscribeToDriverLocationAndRouteUpdates(driverId: Int): Flow<LocationDriverAddress> = callbackFlow {
        // Cerrar canal anterior si existe
        unsubscribeFromLocationUpdates()

        try {
            // Obtener los datos iniciales completos
            val initialData = getDriverLocationWithRoute(driverId)
            send(initialData) // Enviar datos iniciales al flow

            // Crear un nuevo canal de suscripción
            realtimeChannel = client.channel("driver_location_route_updates_$driverId")

            // Configurar la suscripción al canal
            val changes = realtimeChannel!!.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "location"
                filter("driver_id", FilterOperator.EQ, driverId)
            }

            // Job para manejar cambios
            val job = changes.onEach { update ->
                try {
                    val record = update.record
                    if (record != null) {
                        // CORREGIDO: Manejo seguro de valores nulos
                        val updatedData = LocationDriverAddress(
                            driver_id = record["driver_id"]?.toString()?.toIntOrNull() ?: driverId,
                            latitude = record["latitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            longitude = record["longitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            updated_at = record["updated_at"]?.toString() ?: "",
                            encoded_polyline = record["encoded_polyline"]?.toString(), // null es válido
                            route_active = record["route_active"]?.toString()?.toBooleanStrictOrNull() ?: false,
                            route_progress = record["route_progress"]?.toString()?.toFloatOrNull() ?: 0.0f,
                            current_segment = record["current_segment"]?.toString()?.toIntOrNull() ?: 0,
                            route_status = record["route_status"]?.toString() // Dejar como String, no convertir a Int
                        )

                        println("Datos completos recibidos: $updatedData")
                        send(updatedData)
                    }
                } catch (e: Exception) {
                    println("Error procesando actualización completa: ${e.message}")
                    e.printStackTrace()
                    // NUEVO: No cerrar el flow por errores de parsing, solo loggear
                }
            }.launchIn(coroutineScope)

            // Activar la suscripción
            realtimeChannel?.subscribe()
            println("Suscripción completa activada para driver_id: $driverId")

            awaitClose {
                println("Cerrando suscripción completa")
                job.cancel()
                unsubscribeFromLocationUpdates()
            }
        } catch (e: Exception) {
            println("Error al suscribirse a actualizaciones completas: ${e.message}")
            close(e)
        }
    }

    override fun unsubscribeFromLocationUpdates() {
        realtimeChannel?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    it.unsubscribe()
                } catch (e: Exception) {
                    println("Error al cancelar suscripción: ${e.message}")
                }
            }
        }
        realtimeChannel = null
    }
}

// Clase para deserializar la respuesta de la ubicación
@Serializable
data class LocationDriverAddress(
    val driver_id: Int,
    val latitude: Double,
    val longitude: Double,
    val updated_at: String,
    val encoded_polyline: String? = null,
    val route_active: Boolean = false,
    val route_progress: Float = 0.0f,
    val current_segment: Int = 0,
    val route_status: String? = null
)
