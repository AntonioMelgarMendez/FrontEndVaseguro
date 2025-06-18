package com.VaSeguro.data.model.Stop

import com.VaSeguro.data.model.StopPassenger.StopPassenger

data class StopRoute (
    val id: Int,
    val stopPassenger: StopPassenger,
    val order: Int,
    val state: Boolean
)
