package com.VaSeguro.ui.screens.Admin.Stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopDto
import com.VaSeguro.data.model.Stop.Stops
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs
import com.VaSeguro.data.repository.Stops.StopsRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.toString

class StopsAdminScreenViewModel(
    private val stopsRepository: StopsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _stops = MutableStateFlow<List<StopData>>(emptyList())
    val stops: StateFlow<List<StopData>> = _stops

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        fetchStops()
    }

    fun fetchStops() {
        viewModelScope.launch {
            _loading.value = true
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
            } catch (e: Exception) {
                _stops.value = emptyList()
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
                _stops.update { it.filterNot { stop -> stop.id.toString() == stopId } }
                _expandedMap.update { it - stopId }
                _checkedMap.update { it - stopId }
            } catch (_: Exception) { }
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
}