package com.VaSeguro.map.response

import com.google.gson.annotations.SerializedName

data class RouteMatrixResponse(
    val routes: List<RouteMatrixElement>
)

data class RouteMatrixElement(
    val originIndex: Int,
    val destinationIndex: Int,
    val status: RouteMatrixStatus,
    val distanceMeters: Int,
    val duration: String,
    val condition: String
)

data class RouteMatrixStatus(
    val code: Int? = null,
    val message: String? = null
)
