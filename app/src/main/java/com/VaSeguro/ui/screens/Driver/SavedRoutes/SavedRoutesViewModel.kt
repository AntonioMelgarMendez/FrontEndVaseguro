package com.VaSeguro.ui.screens.Driver.SavedRoutes

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.helpers.Resource
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.giphy.sdk.core.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SavedRoutesViewModel(
    private val savedRoutesRepository: SavedRoutesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) : ViewModel() {

    private val _savedRoutes = MutableStateFlow<List<RoutesData>>(emptyList())
    val savedRoutes: StateFlow<List<RoutesData>> = _savedRoutes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSavedRoutes()
    }

    fun loadSavedRoutes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val user = userPreferencesRepository.getUserData()
                val driverId = user?.id

                if (driverId != null) {
                    Log.d("SAVED_ROUTES", "Cargando rutas para driver ID: $driverId")

                    val routes = savedRoutesRepository.getAllRoutes(driverId)
                    _savedRoutes.value = routes

                    Log.d("SAVED_ROUTES", "Rutas cargadas: ${routes.size}")
                } else {
                    Log.e("SAVED_ROUTES", "No se pudo obtener el driver ID")
                    _errorMessage.value = "Error: No se pudo obtener información del conductor"
                }

            } catch (e: Exception) {
                Log.e("SAVED_ROUTES", "Error al cargar rutas: ${e.message}")
                _errorMessage.value = "Error al cargar las rutas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            try {
                val success = savedRoutesRepository.deleteRoute(routeId.toInt())
                if (success) {
                    _errorMessage.value = "Ruta eliminada correctamente"
                    // Recargar la lista después de eliminar
                    loadSavedRoutes()
                } else {
                    _errorMessage.value = "Error al eliminar la ruta"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar la ruta: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Calcula la duración de la ruta en formato legible
     */
    fun calculateRouteDuration(route: RoutesData): String {
        if (route.end_date==null || route.end_date.isEmpty()) return "No completada"

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val startDate = dateFormat.parse(route.start_date) ?: return "Error en fecha"
            val endDate = dateFormat.parse(route.end_date) ?: return "Error en fecha"

            val durationMs = endDate.time - startDate.time

            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60

            return when {
                hours > 0 -> "$hours h $minutes min"
                minutes > 0 -> "$minutes min"
                else -> "${TimeUnit.MILLISECONDS.toSeconds(durationMs)} seg"
            }
        } catch (e: Exception) {
            return "Error en fecha"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MyApplication
                SavedRoutesViewModel(
                    savedRoutesRepository = application.appProvider.provideSavedRoutesRepository(),
                    userPreferencesRepository = application.appProvider.provideUserPreferences(),
                    context = application.applicationContext
                )
            }
        }

    }
}
