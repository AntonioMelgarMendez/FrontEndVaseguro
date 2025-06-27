package com.VaSeguro.map.Supabase

import com.VaSeguro.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds


object SupabaseModule {

    @OptIn(SupabaseInternal::class)
    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {

            // Configura el cliente HTTP
            httpEngine = CIO.create() // Usa el engine CIO manualmente
            httpConfig {
                install(WebSockets)
            }

            // Configura la serialización (opcional pero recomendado)
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })

            // Configura timeout de requests
            requestTimeout = 10.seconds

            // Instala módulos
            install(Postgrest)
            install(Storage)
            install(Realtime) {
                reconnectDelay = 7.seconds
            }
        }
    }

    val postgrest: Postgrest by lazy { supabaseClient.postgrest }
    val storage: Storage by lazy { supabaseClient.storage  }
    val realtime: Realtime by lazy { supabaseClient.realtime }
}