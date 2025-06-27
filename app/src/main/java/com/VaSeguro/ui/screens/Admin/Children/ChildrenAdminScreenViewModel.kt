package com.VaSeguro.ui.screens.Admin.Children

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Auth.UserResponse
import com.VaSeguro.data.remote.Responses.ChildrenResponse
import com.VaSeguro.data.remote.Responses.toChild
import com.VaSeguro.data.remote.Responses.toChildrenResponse
import com.VaSeguro.data.repository.AuthRepository.AuthRepository
import com.VaSeguro.data.repository.Children.ChildrenRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ChildrenAdminScreenViewModel(
    private val childrenRepository: ChildrenRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children

    private val _allUsers = MutableStateFlow<List<UserResponse>>(emptyList())
    val allUsers: StateFlow<List<UserResponse>> = _allUsers

    val parents: List<UserResponse>
        get() = _allUsers.value.filter { it.role_id == 3 }

    val drivers: List<UserResponse>
        get() = _allUsers.value.filter { it.role_id == 4 }

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchUsersForRoles() {
        viewModelScope.launch {
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                _allUsers.value = authRepository.getAllUsers(token)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getDriverNameById(driverId: Int): String {
        return drivers.firstOrNull { it.id == driverId }
            ?.let { "${it.forenames} ${it.surnames}" } ?: "Sin asignar"
    }

    fun getParentNameById(parentId: Int): String {
        return parents.firstOrNull { it.id == parentId }
            ?.let { "${it.forenames} ${it.surnames}" } ?: "Desconocido"
    }

    fun fetchAllChildren() {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (_allUsers.value.isEmpty()) {
                    val token = userPreferencesRepository.getAuthToken().orEmpty()
                    _allUsers.value = authRepository.getAllUsers(token)
                }

                val token = userPreferencesRepository.getAuthToken().orEmpty()
                val childrenBackend: List<Children> = childrenRepository.getChildren(token)
                Log.d("ChildrenDebug", "Children fetched: ${childrenBackend.size}")

                val uiChildren = childrenBackend.map { child ->
                    child.toChildrenResponse().toChild(
                        parentName = getParentNameById(child.parent_id),
                        driverName = getDriverNameById(child.driver_id)
                    )
                }

                _children.value = uiChildren
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar niños: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleExpand(childId: String) {
        _expandedMap.update { map ->
            map.toMutableMap().apply {
                this[childId] = !(this[childId] ?: false)
            }
        }
    }

    fun setChecked(childId: String, checked: Boolean) {
        _checkedMap.update { map ->
            map.toMutableMap().apply {
                this[childId] = checked
            }
        }
    }

    fun deleteChild(childId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                childrenRepository.remove(childId.toString(), token)
                fetchAllChildren()
            } catch (e: Exception) {
                // TODO: handle error
            } finally {
                _loading.value = false
            }
        }
        _expandedMap.update { it - childId.toString() }
        _checkedMap.update { it - childId.toString() }
    }

    fun addChild(
        forenames: String,
        surnames: String,
        birth_date: String,
        medical_info: String,
        gender: String,
        parent_id: Int,
        driver_id: Int,
        profile_pic: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                childrenRepository.create(
                    forenames,
                    surnames,
                    birth_date,
                    medical_info,
                    gender,
                    parent_id,
                    driver_id,
                    profile_pic,
                    token
                )
                fetchAllChildren()
            } catch (e: Exception) {
                // TODO: handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateChild(
        id: String,
        child: Children,
        profilePic: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = userPreferencesRepository.getAuthToken().orEmpty()
                childrenRepository.update(id, child, profilePic, token)
                fetchAllChildren()
            } catch (e: Exception) {
                // TODO: handle error
            } finally {
                _loading.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                try {
                    val application = this[APPLICATION_KEY] as MyApplication
                    ChildrenAdminScreenViewModel(
                        application.appProvider.provideChildrenRepository(),
                        application.appProvider.provideUserPreferences(),
                        application.appProvider.provideAuthRepository()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}