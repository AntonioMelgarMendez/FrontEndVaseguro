package com.VaSeguro.ui.screens.Driver.SavedRoutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.map.repository.SavedRoutesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SavedRoutesViewModel(
    private val savedRoutesRepository: SavedRoutesRepositoryImpl
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
        _isLoading.value = true
        viewModelScope.launch {
            try {
                savedRoutesRepository.savedRoutes.collect { routes ->
                    _savedRoutes.value = routes
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar las rutas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            try {
                savedRoutesRepository.deleteRoute(routeId.toInt())
                _errorMessage.value = "Ruta eliminada correctamente"
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
                )
            }
        }

    }
}
