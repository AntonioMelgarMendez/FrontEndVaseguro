package com.VaSeguro.data.model.StopPassenger

import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.data.model.Child.ChildMap
import com.VaSeguro.data.model.Stop.StopData
import com.VaSeguro.data.model.Stop.StopType

data class StopPassenger (
    val id: Int,
    val stop: StopData,
    val child: ChildMap,
    val stopType: StopType,
)