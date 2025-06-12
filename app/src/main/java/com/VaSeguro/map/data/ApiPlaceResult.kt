package com.VaSeguro.map.data

data class ApiPlaceResult(
    val name: String,
    val formatted_address: String,
    val geometry: ApiGeometry
)