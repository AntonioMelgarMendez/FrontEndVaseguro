package com.VaSeguro.data.repository.RequestRepository

import android.util.Log
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.remote.Request.RequestService
import com.VaSeguro.data.remote.Request.RequestState
import com.google.gson.Gson
import org.json.JSONObject

class RequestRepositoryImpl(
    private val requestService: RequestService
) : RequestRepository {

    override suspend fun sendRequest(token: String, userId: Int): Result<Unit> {
        val response = requestService.approveRequest(
            token = "Bearer $token",
            userId = userId,
            request = RequestState(isSuccessful = true)
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }

    override suspend fun rejectRequest(token: String, userId: Int): Result<Unit> {
        val response = requestService.deleteRequest(
            token = "Bearer $token",
            userId = userId
        )
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error"))
    }

    override suspend fun getCode(token: String, userId: Int): Result<String> {
        return try {
            Log.d("RequestRepository", "Calling getRequestById with userId=$userId")
            val responseBody = requestService.getRequestById("Bearer $token", userId)
            val jsonString = responseBody.string()
            val code = JSONObject(jsonString).getString("code")
            Log.d("RequestRepository", "Received code: $code")
            Result.success(code)
        } catch (e: Exception) {
            Log.e("RequestRepository", "Error fetching code: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun validateCode(code: String): Result<Map<String, Any>> {
        return try {
            val response = requestService.validateCode(mapOf("code" to code))
            if (response.isSuccessful) {
                val bodyString = response.body()?.string()
                if (bodyString != null) {
                    val json = JSONObject(bodyString)
                    val driverJson = json.optJSONObject("driver")
                    if (driverJson != null) {
                        val driverMap = Gson().fromJson(driverJson.toString(), Map::class.java) as Map<String, Any>
                        Result.success(mapOf("driver" to driverMap))
                    } else {
                        Result.failure(Exception("No driver data found"))
                    }
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Invalid code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}