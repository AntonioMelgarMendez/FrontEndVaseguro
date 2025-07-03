package com.VaSeguro.data.model.Stop

import com.VaSeguro.data.model.StopPassenger.StopPassenger

data class StopRoute (
    val id: Int,
    val stopPassenger: StopPassenger,
    val order: Int,
    val state: Boolean
)

fun StopRoute.toSave(): StopRouteToSave {
    return StopRouteToSave(
        id = this.id,
        stopPassengerId = this.stopPassenger.id,
        order = this.order,
        state = this.state
    )
}

data class StopRouteToSave (
    val id: Int,
    val stopPassengerId: Int,
    val order: Int,
    val state: Boolean
)
