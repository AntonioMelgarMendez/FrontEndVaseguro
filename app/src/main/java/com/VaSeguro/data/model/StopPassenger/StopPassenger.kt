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
    val type_id: Int?,  // Cambiar a nullable
    @SerializedName("child_id")
    val child_id: Int
) {
    // Propiedad calculada para compatibilidad con código existente
    val stopType: StopType
        get() = when (type_id) {
            1 -> StopType.HOME
            2 -> StopType.INSTITUTION
            else -> StopType.HOME // Valor por defecto si type_id es null o desconocido
        }
}
