package com.VaSeguro.ui.screens.Parents.Children

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.VaSeguro.MyApplication
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.repository.Children.ChildrenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus
import kotlin.collections.set

class ChildrenViewModel(
  private val childrenRepository: ChildrenRepository,
) : ViewModel() {

  // private val _children = MutableStateFlow<List<Child>>(emptyList())
  private val _children = MutableStateFlow(
    listOf(
      Child(
        id = "1",
        fullName = "Jonh Doe",
        surnames = "Doe",
        forenames = "Jonh",
        birth = "2010-05-12",
        age = 14,
        driver = "19",
        parent = "5",
        medicalInfo = "None",
        createdAt = "2023-02-01",
        profilePic = null
      ),
      Child(
        id = "2",
        fullName = "Emma López",
        surnames = "López",
        forenames = "Emma",
        birth = "2011-08-23",
        age = 13,
        driver = "20",
        parent = "6",
        medicalInfo = "Allergic to peanuts",
        createdAt = "2023-03-15",
        profilePic = null
      ),
      Child(
        id = "3",
        fullName = "Mateo Rodríguez",
        surnames = "Rodríguez",
        forenames = "Mateo",
        birth = "2012-01-30",
        age = 12,
        driver = "21",
        parent = "7",
        medicalInfo = "Asthma",
        createdAt = "2023-01-20",
        profilePic = null
      ),
      Child(
        id = "4",
        fullName = "Sofía Martínez",
        surnames = "Martínez",
        forenames = "Sofía",
        birth = "2010-12-10",
        age = 13,
        driver = "19",
        parent = "5",
        medicalInfo = "",
        createdAt = "2023-04-10",
        profilePic = null
      ),
      Child(
        id = "5",
        fullName = "Lucas Ramírez",
        surnames = "Ramírez",
        forenames = "Lucas",
        birth = "2009-11-05",
        age = 15,
        driver = "20",
        parent = "6",
        medicalInfo = "Uses glasses",
        createdAt = "2023-05-22",
        profilePic = null
      )
    )
  )

  val children: StateFlow<List<Child>> = _children
  private val _drivers = MutableStateFlow<List<UserData>>(emptyList())
  val drivers: StateFlow<List<UserData>> = _drivers
  private val _parents = MutableStateFlow<List<UserData>>(emptyList())
  val parents: StateFlow<List<UserData>> = _parents


  private val _expandedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val expandedMap: StateFlow<Map<String, Boolean>> = _expandedMap

  fun toggleExpand(childId: String) {
    _expandedMap.update { current ->
      current.toMutableMap().apply {
        this[childId] = !(this[childId] ?: false)
      }
    }
  }

  fun deleteChild(childId: String) {
    _children.update { list ->
      list.filterNot { it.id == childId }
    }
    _expandedMap.update { it - childId }
  }

  fun addChild(child: Child) {
    _children.update { current -> current + child }
    _expandedMap.update { current -> current + (child.id to false) }
  }

  fun updateChild(updatedChild: Child) {
    _children.update { list ->
      list.map {
        if (it.id == updatedChild.id) updatedChild else it
      }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        try {
          val application = this[APPLICATION_KEY] as MyApplication
          ChildrenViewModel(
            application.appProvider.provideChildrenRepository()
          )
        } catch (e: Exception) {
          e.printStackTrace()
          throw e
        }
      }
    }
  }
}