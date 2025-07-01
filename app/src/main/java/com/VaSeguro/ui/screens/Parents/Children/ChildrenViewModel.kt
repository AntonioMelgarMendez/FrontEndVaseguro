package com.VaSeguro.ui.screens.Parents.Children

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.model.Stop.Stops
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.repository.Children.ChildrenRepository
import com.VaSeguro.data.repository.DriverPrefs.DriverPrefs.getDriverId
import com.VaSeguro.data.repository.Stops.StopsRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class ChildrenViewModel(
  private val childrenRepository: ChildrenRepository,
  private val userPreferencesRepository: UserPreferencesRepository,
  private val stopsRepository: StopsRepository
) : ViewModel() {

  private val _children = MutableStateFlow<List<Children>>(emptyList())
  val children: StateFlow<List<Children>> = _children

  private val _drivers = MutableStateFlow<List<UserData>>(emptyList())
  val drivers: StateFlow<List<UserData>> = _drivers

  private val _parents = MutableStateFlow<List<UserData>>(emptyList())
  val parents: StateFlow<List<UserData>> = _parents

  private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

  private val _canEdit = MutableStateFlow(false)
  val canEdit: StateFlow<Boolean> = _canEdit

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error

  private var userId: String? = null
  private var userRole: Int? = null

  init {
    loadUserAndChildren()
  }
  private fun loadUserAndChildren() {
    viewModelScope.launch {
      _isLoading.value = true // Start loading
      val user = userPreferencesRepository.getUserData()
      val token = userPreferencesRepository.getAuthToken() ?: ""
      userId = user?.id?.toString()
      userRole = user?.role_id

      Log.d("ChildrenViewModel", "UserId: $userId, UserRole: $userRole, Token: $token")

      val allChildren = try {
        val backendChildren = childrenRepository.getChildren(token)
        Log.d("ChildrenViewModel", "Backend children: $backendChildren")
        backendChildren.map {
          Children(
            id = it.id,
            forenames = it.forenames,
            surnames = it.surnames,
            birth_date = it.birth_date,
            medical_info = it.medical_info,
            parent_id = it.parent_id,
            driver_id = it.driver_id,
            profile_pic = it.profile_pic,
            gender = it.gender
          )
        }
      } catch (e: Exception) {
        Log.e("ChildrenViewModel", "Error fetching children: ${e.message}", e)
        emptyList()
      }

      val filteredChildren = when (userRole) {
        3 -> { // Parent
          _canEdit.value = true
          allChildren.filter { it.parent_id.toString() == userId }
        }
        4 -> { // Driver
          _canEdit.value = false
          allChildren.filter { it.driver_id.toString() == userId }
        }
        else -> {
          _canEdit.value = false
          emptyList()
        }
      }

      Log.d("ChildrenViewModel", "Filtered children: $filteredChildren")
      _children.value = filteredChildren
      _isLoading.value = false // End loading
    }
  }

  fun toggleExpand(childId: String) {
    _expandedMap.update { current ->
      current.toMutableMap().apply {
        this[childId] = !(this[childId] ?: false)
      }
    }
  }

  fun deleteChild(context: Context, childId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null
      try {
        val token = userPreferencesRepository.getAuthToken() ?: throw Exception("User not authenticated")
        childrenRepository.remove(childId, token)
        if (_canEdit.value) {
          _children.update { list -> list.filterNot { it.id.toString() == childId } }
          _expandedMap.update { it - childId }
        }
        onSuccess()
      } catch (e: Exception) {
        _error.value = e.message
        onError(e.message ?: "Unknown error")
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun compressImageToMultipart(context: Context, uri: Uri?, maxSizeBytes: Int = 1_000_000): MultipartBody.Part? {
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

  fun addChild(
    context: Context,
    forenames: String,
    surnames: String,
    birthDate: String,
    medicalInfo: String,
    gender: String,
    profileImageUri: Uri?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null
      try {
        val driverId = getDriverId(context) ?: 0
        val user = userPreferencesRepository.getUserData()
        val parentId = user?.id ?: 0
        val token = userPreferencesRepository.getAuthToken() ?: throw Exception("User not authenticated")
        val profilePic = compressImageToMultipart(context, profileImageUri)

        val createdChild = childrenRepository.create(
          forenames = forenames,
          surnames = surnames,
          birth_date = birthDate,
          medical_info = medicalInfo,
          gender = gender,
          parent_id = parentId,
          driver_id = driverId,
          profile_pic = profilePic,
          token = token
        )
        if (_canEdit.value) {
          _children.update { current -> current + createdChild }
          _expandedMap.update { current -> current + (createdChild.id.toString() to false) }
        }
        onSuccess()
      } catch (e: Exception) {
        _error.value = e.message
        onError(e.message ?: "Unknown error")
      } finally {
        _isLoading.value = false
      }
    }
  }
  fun updateChild(
    context: Context,
    updatedChild: Children,
    profileImageUri: Uri?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null
      try {
        val driverId = getDriverId(context) ?: 0
        val user = userPreferencesRepository.getUserData()
        val parentId = user?.id ?: 0
        val token = userPreferencesRepository.getAuthToken() ?: throw Exception("User not authenticated")
        val profilePic = compressImageToMultipart(context, profileImageUri)
        val result = childrenRepository.update(
          id = updatedChild.id.toString(),
          child = updatedChild.copy(
            driver_id = driverId,
            parent_id = parentId
          ),
          profilePic = profilePic,
          token = token
        )

        if (_canEdit.value) {
          _children.update { list ->
            list.map { if (it.id == result.id) result else it }
          }
        }
        onSuccess()
      } catch (e: Exception) {
        _error.value = e.message
        onError(e.message ?: "Unknown error")
      } finally {
        _isLoading.value = false
      }
    }
  }
  fun upsertStopsForChild(
    childId: Int,
    driverId: Int,
    pickupName: String,
    pickupLat: Double,
    pickupLng: Double,
    schoolName: String,
    schoolLat: Double,
    schoolLng: Double
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val token = userPreferencesRepository.getAuthToken().orEmpty()
        val allStops = stopsRepository.getAllStops(token)
        val existingPickup = allStops.find { it.driver_id == driverId && it.name == "Home"+childId }
        val pickupStop = Stops(
          driver_id = driverId,
          name = "Home"+childId,
          latitude = pickupLat,
          longitude = pickupLng
        )
        if (existingPickup != null) {
          stopsRepository.updateStop(existingPickup.id.toString(), pickupStop, token)
        } else {
          stopsRepository.createStop(pickupStop, token)
        }
        val existingSchool = allStops.find { it.driver_id == driverId && it.name == "School"+childId }
        val schoolStop = Stops(
          driver_id = driverId,
          name = "School"+childId,
          latitude = schoolLat,
          longitude = schoolLng
        )
        if (existingSchool != null) {
          stopsRepository.updateStop(existingSchool.id.toString(), schoolStop, token)
        } else {
          stopsRepository.createStop(schoolStop, token)
        }
      } catch (e: Exception) {
        _error.value = "Error saving stops: ${e.localizedMessage ?: "Unknown error"}"
      } finally {
        _isLoading.value = false
      }
    }
  }

}