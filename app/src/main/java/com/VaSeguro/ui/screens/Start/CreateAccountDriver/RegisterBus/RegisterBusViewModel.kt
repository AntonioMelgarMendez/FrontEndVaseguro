package com.VaSeguro.ui.screens.Start.CreateAccountDriver.RegisterBus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepository
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import kotlin.compareTo

class RegisterBusViewModel(private val vehicleRepository: VehicleRepository,  private val userPreferencesRepository: UserPreferencesRepository ) : ViewModel() {
    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl

    private val _isImageLoading = MutableStateFlow(false)
    val isImageLoading: StateFlow<Boolean> = _isImageLoading
    private val _isRegisterLoading = MutableStateFlow(false)
    val isRegisterLoading: StateFlow<Boolean> = _isRegisterLoading


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val fancyboxGif = "https://cdnjs.cloudflare.com/ajax/libs/fancybox/2.1.5/fancybox_loading.gif"

    fun fetchCarImage(brand: String, model: String) {
        viewModelScope.launch {
            _isImageLoading.value = true
            _error.value = null
            try {
                val searchTerm = "$brand $model".trim().replace(" ", "+")
                val url = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://www.carimagery.com/api.asmx/GetImageUrl?searchTerm=$searchTerm")
                        .build()
                    val response = client.newCall(request).execute()
                    val xml = response.body?.string()
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val parser = factory.newPullParser()
                    parser.setInput(xml?.reader())
                    var result: String? = null
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "string") {
                            result = parser.nextText()?.trim()
                            break
                        }
                        eventType = parser.next()
                    }
                    result
                }
                Log.d("RegisterBusViewModel", "Fetched image URL: $url")

                val isGifOrLoading = withContext(Dispatchers.IO) {
                    if (url.isNullOrBlank()) return@withContext true
                    if (url == fancyboxGif) return@withContext true
                    val client = OkHttpClient()
                    val headRequest = Request.Builder()
                        .url(url)
                        .head()
                        .build()
                    val headResponse = client.newCall(headRequest).execute()
                    val contentType = headResponse.header("Content-Type") ?: ""
                    headResponse.close()
                    contentType.contains("gif", ignoreCase = true)
                }

                if (url.isNullOrBlank() || isGifOrLoading) {
                    _imageUrl.value = null
                } else {
                    _imageUrl.value = url
                }
            } catch (e: Exception) {
                Log.e("RegisterBusViewModel", "Error fetching image", e)
                _imageUrl.value = null
                _error.value = "Could not fetch image"
            } finally {
                _isImageLoading.value = false
            }
        }
    }

    fun registerBus(
        plate: String,
        model: String,
        brand: String,
        year: String,
        color: String,
        capacity: String,
        carPicUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isRegisterLoading.value = true
            try {
                val driverId = userPreferencesRepository.getUserData()?.id ?: run {
                    onError("No se pudo obtener la información del conductor")
                    return@launch
                }

                vehicleRepository.createVehicle(
                    plate, model, brand, year, color, capacity, driverId, carPicUrl // Pass URL
                ).collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> _isRegisterLoading.value = true
                        is Resource.Success -> {
                            val newVehicle = resource.data
                            if (newVehicle != null && newVehicle.id > 0) {
                                onSuccess()
                            } else {
                                onError("Vehicle creation failed")
                            }
                            _isRegisterLoading.value = false
                        }
                        is Resource.Error -> {
                            onError(resource.message ?: "Error al registrar el vehículo")
                            _isRegisterLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error registering bus")
            } finally {
                _isRegisterLoading.value = false
            }
        }
    }
}