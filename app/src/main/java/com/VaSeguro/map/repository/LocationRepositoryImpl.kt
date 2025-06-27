package com.VaSeguro.map.repository

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
        // Crear objeto serializable en lugar de usar mapOf
        val locationUpdate = LocationDriverAddress(
            driver_id = driverId,
            latitude = lat,
            longitude = lon,
            updated_at = System.now().toString()
        )

        // Usar insert con objeto serializable
        client.postgrest.from("location").upsert(locationUpdate)
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
    val updated_at: String
)
