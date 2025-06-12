package com.VaSeguro.data.repository.Vehicle

import com.VaSeguro.data.model.Vehicle.Vehicle
import com.VaSeguro.data.model.Vehicle.VehicleCreateRequest
import com.VaSeguro.data.model.Vehicle.VehicleUpdateRequest
import com.VaSeguro.data.remote.Responses.toDomain
import com.VaSeguro.data.remote.Vehicle.VehicleService
import com.VaSeguro.helpers.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun String.toPlainRequestBody(): RequestBody =
  this.toRequestBody("text/plain".toMediaTypeOrNull())

fun String.toImageMultipart(partName: String): MultipartBody.Part? {
  if (this.isBlank()) return null

  val file = File(this)
  if (!file.exists()) return null

  val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
  return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

class VehicleRepositoryImpl(
  private val vehicleService: VehicleService
) : VehicleRepository {

  override suspend fun getVehicles(): Flow<Resource<List<Vehicle>>> = flow {
    emit(Resource.Loading)
    try {
      val vehiclesResponse = vehicleService.getVehicles()
      val vehicles = vehiclesResponse.map { it.toDomain() }
      emit(Resource.Success(vehicles))
    } catch (e: Exception) {
      emit(Resource.Error(e.localizedMessage ?: "Error desconocido"))
    }
  }

  override suspend fun getVehicleById(id: String): Flow<Resource<Vehicle>> = flow {
    emit(Resource.Loading)
    try {
      val vehicleResponse = vehicleService.getVehicleById(id)
      emit(Resource.Success(vehicleResponse.toDomain()))
    } catch (e: Exception) {
      emit(Resource.Error(e.localizedMessage ?: "Error desconocido"))
    }
  }

  override suspend fun createVehicle(request: VehicleCreateRequest): Flow<Resource<Vehicle>> = flow {
    emit(Resource.Loading)
    try {
      val response = vehicleService.createVehicle(
        plate = request.plate.toPlainRequestBody(),
        driverId = request.driverId.toPlainRequestBody(),
        model = request.model.toPlainRequestBody(),
        brand = request.brand.toPlainRequestBody(),
        year = request.year.toPlainRequestBody(),
        color = request.color.toPlainRequestBody(),
        capacity = request.capacity.toPlainRequestBody(),
        carPic = request.carPic?.toImageMultipart("car_pic")
      )
      emit(Resource.Success(response.toDomain()))
    } catch (e: Exception) {
      emit(Resource.Error(e.localizedMessage ?: "Error al crear el vehículo"))
    }
  }

  override suspend fun updateVehicle(id: String, request: VehicleUpdateRequest): Flow<Resource<Vehicle>> = flow {
    emit(Resource.Loading)
    try {
      val response = vehicleService.updateVehicle(
        id = id,
        plate = request.plate.toPlainRequestBody(),
        driverId = request.driverId.toPlainRequestBody(),
        model = request.model.toPlainRequestBody(),
        brand = request.brand.toPlainRequestBody(),
        year = request.year.toPlainRequestBody(),
        color = request.color.toPlainRequestBody(),
        capacity = request.capacity.toPlainRequestBody(),
        carPic = request.carPic?.toImageMultipart("car_pic")
      )

      emit(Resource.Success(response.toDomain()))
    } catch (e: Exception) {
      emit(Resource.Error(e.localizedMessage ?: "Error al actualizar el vehículo"))
    }
  }

  override suspend fun deleteVehicle(id: String): Flow<Resource<Unit>> = flow {
    emit(Resource.Loading)
    try {
      vehicleService.deleteVehicle(id)
      emit(Resource.Success(Unit))
    } catch (e: Exception) {
      emit(Resource.Error(e.localizedMessage ?: "Error al eliminar el vehículo"))
    }
  }
}
