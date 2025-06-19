package com.VaSeguro.data.model.Stop

import com.VaSeguro.data.model.User.UserData

data class StopData (
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)
