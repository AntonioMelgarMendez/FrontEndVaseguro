package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.Dao.Stops.StopDao
import com.VaSeguro.data.Entitys.Stops.StopEntity
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopDto
import com.VaSeguro.data.model.Stop.Stops
import com.VaSeguro.data.repository.Stops.StopsRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopsAdminScreenViewModel(
    private val stopsRepository: StopsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val stopsDao: StopDao
) : ViewModel() {

    private val _stops = MutableStateFlow<List<StopData>>(emptyList())
    val stops: StateFlow<List<StopData>> = _stops

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val CACHE_EXPIRATION_MS = 1 * 60 * 1000L

    init {
        fetchStops()
    }

    fun fetchStops() {
        viewModelScope.launch {
            _loading.value = true
            // 1. Read from Room
            val cached = stopsDao.getAllStops()
            if (cached.isNotEmpty()) {
                _stops.value = cached.map { it.toStopData() }
            }
            // 2. Check cache expiration
            val lastFetch = userPreferencesRepository.getLastStopsFetchTime()
            val now = System.currentTimeMillis()
            if (lastFetch != null && now - lastFetch < CACHE_EXPIRATION_MS) {
                _loading.value = false
                return@launch
            }
            // 3. Fetch from API and update Room
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                val stopDtos: List<StopDto> = stopsRepository.getAllStops(token)
                val stopDataList = stopDtos.map { dto ->
                    StopData(
                        id = dto.id,
                        name = dto.name,
                        latitude = dto.latitude,
                        longitude = dto.longitude
                    )
                }
                _stops.value = stopDataList
                stopsDao.clearStops()
                stopsDao.insertStops(stopDtos.map { it.toEntity() })
                userPreferencesRepository.setLastStopsFetchTime(now)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteStop(stopId: String) {
        viewModelScope.launch {
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                stopsRepository.deleteStop(stopId, token)
                fetchStops()
            } catch (_: Exception) { }
            _expandedMap.update { it - stopId }
            _checkedMap.update { it - stopId }
        }
    }

    fun editStop(stop: StopData, driverId: Int?) {
        viewModelScope.launch {
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                val stops = Stops(
                    driver_id = driverId ?: 0,
                    name = stop.name,
                    latitude = stop.latitude,
                    longitude = stop.longitude
                )
                stopsRepository.updateStop(stop.id.toString(), stops, token)
                fetchStops()
            } catch (_: Exception) { }
        }
    }

    fun toggleExpand(stopId: String) {
        _expandedMap.update { map ->
            map.toMutableMap().apply {
                this[stopId] = !(this[stopId] ?: false)
            }
        }
    }

    fun setChecked(stopId: String, checked: Boolean) {
        _checkedMap.update { map ->
            map.toMutableMap().apply {
                this[stopId] = checked
            }
        }
    }

    // Conversion helpers
    private fun StopDto.toEntity() = StopEntity(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        driver_id = this.driver_id,
    )

    private fun StopEntity.toStopData() = StopData(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude
    )
}