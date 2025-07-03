package com.VaSeguro.data.remote.Call

import com.VaSeguro.data.model.Call.CallResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface CallService {
    @FormUrlEncoded
    @POST("calls/calls/")
    suspend fun createCall(
        @Field("caller_id") callerId: String,
        @Field("callee_id") calleeId: String
    ): CallResponse

    @GET("calls/calls/{id}")
    suspend fun getCallById(@Path("id") id: String): CallResponse
}