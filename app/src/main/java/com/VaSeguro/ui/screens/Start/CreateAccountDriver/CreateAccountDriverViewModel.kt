package com.VaSeguro.ui.screens.Start.CreateAccountDriver

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class CreateAccountDriverViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _surname = MutableStateFlow("")
    val surname: StateFlow<String> = _surname

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    fun onNameChange(newName: String) { _name.value = newName }
    fun onSurnameChange(newSurname: String) { _surname.value = newSurname }
    fun onGenderChange(newGender: String) { _gender.value = newGender }
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPhoneChange(newPhone: String) { _phone.value = newPhone }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onImageChange(newUri: Uri?) { _imageUri.value = newUri }

    private fun compressImageToMaxSize(context: Context, uri: Uri, maxSizeBytes: Int = 1_000_000): ByteArray? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap == null) return null

        var quality = 90
        var bytes: ByteArray
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            bytes = outputStream.toByteArray()
            quality -= 10
        } while (bytes.size > maxSizeBytes && quality > 10)
        return bytes
    }

    fun register(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val forenames = _name.value.trim()
                val surnames = _surname.value.trim()
                val gender = _gender.value

                val imageUri = _imageUri.value
                val profilePicPart = imageUri?.let { uri ->
                    val bytes = compressImageToMaxSize(context, uri, maxSizeBytes = 1_000_000)
                    if (bytes != null && bytes.isNotEmpty()) {
                        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
                        MultipartBody.Part.createFormData("profile_pic", "profile.jpg", requestBody)
                    } else {
                        Log.d("No hay imagen", "No se pudo leer la imagen del URI: $uri")
                        null
                    }
                }

                val response = authRepository.registerMultipart(
                    forenames = forenames,
                    surnames = surnames,
                    email = _email.value,
                    password = _password.value,
                    phone_number = _phone.value,
                    gender = gender,
                    profile_pic = profilePicPart,
                    role_id = 4
                )
                if (response.token.isNotBlank()) {
                    preferencesRepository.saveAuthToken(response.token)
                    preferencesRepository.saveUserData(response.user)
                    onSuccess()
                } else {
                    _error.value = "Registro fallido"
                    onError("Registro fallido")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message ?: "Error desconocido"}"
                onError(e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }
}