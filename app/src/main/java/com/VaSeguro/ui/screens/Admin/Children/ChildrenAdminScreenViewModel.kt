package com.VaSeguro.ui.screens.Admin.Children

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.VaSeguro.data.model.Child.Child

class ChildrenAdminScreenViewModel : ViewModel() {
    private val _children = MutableStateFlow(
        listOf(
            Child(12451, "Charlie Brown", "Charlie Roberto", "Brown Portillo", "01/01/2015", 10, "Juan Mendoza", "Carlos Portillo", "Alérgico al maní", "17/04/2025 14:01"),
            Child(12452, "Daniel Hawkins", "Daniel", "Hawkins", "03/03/2015", 10, "Pedro Torres", "Laura Gómez", "", "17/04/2025 14:02"),
        )
    )
    val children: StateFlow<List<Child>> = _children

    fun addChild(
        forenames: String,
        surnames: String,
        birth: String,
        medicalInfo: String,
        parent: String,
        driver: String
    ) {
        val fullName = "$forenames $surnames"
        // Generar un ID numérico basado en timestamp
        val newId = (System.currentTimeMillis() % 100000).toInt()
        val newChild = Child(
            id = newId,
            fullName = fullName,
            forenames = forenames,
            surnames = surnames,
            birth = birth,
            age = 10,
            driver = driver,
            parent = parent,
            medicalInfo = medicalInfo,
            createdAt = "08/06/2025 20:30"
        )
        _children.value = _children.value + newChild
    }

    fun deleteChild(id: Int) {
        _children.value = _children.value.filterNot { it.id == id }
    }
}

