package com.VaSeguro.map.response

import com.VaSeguro.map.data.ApiPlaceResult
import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("results")
    val results: List<ApiPlaceResult>
)