package com.VaSeguro.map.response

import com.VaSeguro.map.data.Route
import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>
)