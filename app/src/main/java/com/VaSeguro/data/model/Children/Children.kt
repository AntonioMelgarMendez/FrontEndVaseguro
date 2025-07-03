package com.VaSeguro.data.model.Children

import com.VaSeguro.data.model.Child.Child

data class Children(
    val id: Int=0,
    val forenames: String,
    val surnames: String,
    val birth_date: String,
    val medical_info: String,
    val parent_id: Int,
    val driver_id: Int,
    val gender: String,
    val profile_pic: String? = null,

)