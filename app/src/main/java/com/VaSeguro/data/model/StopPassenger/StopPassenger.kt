package com.VaSeguro.data.model.StopPassenger

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType
import com.google.gson.annotations.SerializedName

data class StopPassenger (
    val id: Int,
    val stop: StopData,
    val child: ChildMap,
    @SerializedName("stop_id")
    val stop_id: Int,
    @SerializedName("type_id")
    val type_id: Int,
    @SerializedName("child_id")
    val child_id: Int
) {
    // Propiedad calculada para compatibilidad con código existente
    val stopType: StopType
        get() = StopType.fromId(type_id.toString())
}
