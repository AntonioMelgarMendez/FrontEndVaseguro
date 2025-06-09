package com.VaSeguro.map.data

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val steps: List<Step>
)