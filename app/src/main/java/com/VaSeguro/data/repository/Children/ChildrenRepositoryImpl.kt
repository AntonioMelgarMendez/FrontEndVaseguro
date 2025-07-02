package com.VaSeguro.data.repository.Children

import com.VaSeguro.data.Dao.Children.ChildDao
import com.VaSeguro.data.Entitys.Children.ChildEntity
import com.VaSeguro.data.Entitys.Children.toChild
import com.VaSeguro.data.Entitys.Children.toEntity
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Children.ChildrenService
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.toString

// ChildrenRepositoryImpl.kt
class ChildrenRepositoryImpl(
  private val childrenService: ChildrenService,
  private val childDao: ChildDao
) : ChildrenRepository {

//  override suspend fun getChildren(token: String) =
//    childrenService.getChildren("Bearer $token")

  override suspend fun getChildren(token: String): List<Children> {
    // Primero intentamos obtener los niños desde Room
    val localChildren = mutableListOf<Children>()

    // Usamos collect para obtener los valores del Flow y mapearlos a Children
    childDao.getAllChildren().collect { childEntities ->
      localChildren.addAll(childEntities.map { it.toChild() })
    }

    // Si ya hay niños locales, los devolvemos
    if (localChildren.isNotEmpty()) {
      return localChildren
    } else {
      // Si no hay niños en Room, obtenemos desde la API
      val remoteChildren = childrenService.getChildren("Bearer $token")

      // Guardamos los niños en Room
      remoteChildren.forEach { child ->
        val childEntity = child.toEntity()  // Convertimos el Children a ChildEntity
        childDao.insertChild(childEntity)  // Insertar en Room
      }
      return remoteChildren
    }
  }


//  override suspend fun getChild(id: String, token: String) =
//    childrenService.getChild(id, "Bearer $token")

  // Obtener un niño específico desde la API o Room
  override suspend fun getChild(id: String, token: String): Children {
    val localChild = childDao.getChildById(id.toInt())?.toChild()
    return localChild ?: childrenService.getChild(id, "Bearer $token")
  }

//  override suspend fun create(
//    forenames: String,
//    surnames: String,
//    birth_date: String,
//    medical_info: String,
//    gender: String,
//    parent_id: Int,
//    driver_id: Int,
//    profile_pic: MultipartBody.Part?,
//    token: String
//  ): Children = childrenService.create(
//    forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
//    surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
//    medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
//    gender.toRequestBody("text/plain".toMediaTypeOrNull()),
//    parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//    driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//    profile_pic,
//    birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
//    "Bearer $token"
//  )

  override suspend fun create(
    forenames: String,
    surnames: String,
    birth_date: String,
    medical_info: String,
    gender: String,
    parent_id: Int,
    driver_id: Int,
    profile_pic: MultipartBody.Part?,
    token: String
  ): Children {
    val createdChild = childrenService.create(
      forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
      surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
      medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
      gender.toRequestBody("text/plain".toMediaTypeOrNull()),
      parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
      driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
      profile_pic,
      birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
      "Bearer $token"
    )

    // Guardamos el nuevo niño en Room
    val childEntity = createdChild.toEntity()
    childDao.insertChild(childEntity)

    return createdChild
  }

//  override suspend fun update(
//    id: String,
//    child: Children,
//    profilePic: MultipartBody.Part?,
//    token: String
//  ): Children = childrenService.update(
//    id = id,
//    forenames = child.forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
//    surnames = child.surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
//    medicalInfo = child.medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
//    gender = child.gender.toRequestBody("text/plain".toMediaTypeOrNull()),
//    parentId = child.parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//    driverId = child.driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//    profilePic = profilePic,
//    birthDate = child.birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
//    token = "Bearer $token"
//  )

  override suspend fun update(
    id: String,
    child: Children,
    profilePic: MultipartBody.Part?,
    token: String
  ): Children {
    val updatedChild = childrenService.update(
      id = id,
      forenames = child.forenames.toRequestBody("text/plain".toMediaTypeOrNull()),
      surnames = child.surnames.toRequestBody("text/plain".toMediaTypeOrNull()),
      medicalInfo = child.medical_info.toRequestBody("text/plain".toMediaTypeOrNull()),
      gender = child.gender.toRequestBody("text/plain".toMediaTypeOrNull()),
      parentId = child.parent_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
      driverId = child.driver_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
      profilePic = profilePic,
      birthDate = child.birth_date.toRequestBody("text/plain".toMediaTypeOrNull()),
      token = "Bearer $token"
    )

    // Actualizamos el niño en Room
    val childEntity = updatedChild.toEntity()
    childDao.updateChild(childEntity)

    return updatedChild
  }

//  override suspend fun remove(id: String, token: String) =
//    childrenService.remove(id, "Bearer $token")

  // Eliminar un niño desde la API y Room
  override suspend fun remove(id: String, token: String) {
    childrenService.remove(id, "Bearer $token")

    // Eliminar el niño de Room
    childDao.deleteChildById(id.toInt())
  }
}