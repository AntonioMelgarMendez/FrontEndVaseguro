package com.VaSeguro.data.model.Child

data class ChildMap(
    val id: Int,
    val fullName: String,
    val forenames: String,
    val surnames: String,
    val birth: String,
    val age: Int,
    val driverId: Int,
    val parentId: Int,
    val medicalInfo: String,
    val createdAt: String,
    val profilePic: String? = null
)