package com.VaSeguro.ui.screens.Admin.Children

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.VaSeguro.data.model.Child.Child
import kotlinx.coroutines.flow.update

class ChildrenAdminScreenViewModel : ViewModel() {
    private val _children = MutableStateFlow(
        listOf(
            Child(
                id = 1,
                fullName = "Andrea Castillo",
                forenames = "Andrea",
                surnames = "Castillo",
                birth = "2012-05-10",
                age = 12,
                driver = "Carlos Reyes",
                parent = "Diana Castillo",
                medicalInfo = "None",
                createdAt = "10/05/2024 09:15",
                profilePic = null
            ),
            Child(
                id = 2,
                fullName = "Samuel Méndez",
                forenames = "Samuel",
                surnames = "Méndez",
                birth = "2014-08-22",
                age = 10,
                driver = "Luis Hernández",
                parent = "Roberto Méndez",
                medicalInfo = "Allergy to peanuts",
                createdAt = "11/05/2024 11:45",
                profilePic = null
            )
        )
    )
    val children: StateFlow<List<Child>> = _children

    private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

    private val _checkedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedMap: StateFlow<Map<String, Boolean>> = _checkedMap

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
        _children.update { it.filterNot { child -> child.id == childId } }
        _expandedMap.update { it - childId.toString() }
        _checkedMap.update { it - childId.toString() }
    }

    fun addChild(child: Child) {
        _children.update { it + child }
    }
}