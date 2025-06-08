package com.VaSeguro.ui.screens.Start.Starting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.TripData
import com.VaSeguro.data.model.TripSlide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartingViewModel : ViewModel() {
    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    private val _slides = MutableStateFlow(TripData.slides)
    val slides: StateFlow<List<TripSlide>> = _slides.asStateFlow()

    fun nextSlide() {
        viewModelScope.launch {
            _currentSlideIndex.value = (_currentSlideIndex.value + 1) % _slides.value.size
        }
    }

    fun previousSlide() {
        viewModelScope.launch {
            _currentSlideIndex.value = if (_currentSlideIndex.value == 0) {
                _slides.value.size - 1
            } else {
                _currentSlideIndex.value - 1
            }
        }
    }

    fun goToSlide(index: Int) {
        viewModelScope.launch {
            _currentSlideIndex.value = index
        }
    }
}