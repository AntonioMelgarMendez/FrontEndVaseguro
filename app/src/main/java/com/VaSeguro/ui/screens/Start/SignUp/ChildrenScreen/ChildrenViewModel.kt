import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.repository.Children.ChildrenRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class ChildrenViewModel(
    private val childrenRepository: ChildrenRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private fun compressImageToMultipart(context: Context, uri: android.net.Uri?, maxSizeBytes: Int = 1_000_000): MultipartBody.Part? {
        uri ?: return null
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmap == null) return null

        var quality = 90
        var bytes: ByteArray
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
            bytes = outputStream.toByteArray()
            quality -= 10
        } while (bytes.size > maxSizeBytes && quality > 10)

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
        return MultipartBody.Part.createFormData("profile_pic", "child_profile.jpg", requestBody)
    }

    fun createChild(
        context: Context,
        forenames: String,
        surnames: String,
        birthDate: String,
        medicalInfo: String,
        gender: String,
        driverId: Int,
        profilePicUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = userPreferencesRepository.getUserData()
                val parentId = user?.id ?: throw Exception("Parent not found")
                val profilePic = compressImageToMultipart(context, profilePicUri)
                childrenRepository.create(
                    forenames = forenames,
                    surnames = surnames,
                    birth_date = birthDate,
                    medical_info = medicalInfo,
                    gender = gender,
                    parent_id = parentId,
                    driver_id = driverId,
                    profile_pic = profilePic,
                    token = userPreferencesRepository.getAuthToken() ?: throw Exception("User not authenticated")
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}