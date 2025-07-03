package com.VaSeguro.data.model.Child

import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.data.remote.Auth.UserResponse

data class Child(
    val id: Int,
    val fullName: String,
    val forenames: String,
    val surnames: String,
    val birth: String,
    val age: Int,
    val driver: String,
    val parent: String,
    val medicalInfo: String,
    val createdAt: String,
    val profilePic: String? = null
)

fun Child.toChildren(
    parents: List<UserResponse>,
    drivers: List<UserResponse>,
): Children? {
    val parentId = parents.find { "${it.forenames} ${it.surnames}" == this.parent }?.id
    val driverId = drivers.find { "${it.forenames} ${it.surnames}" == this.driver }?.id

    if (parentId == null || driverId == null) return null

    return Children(
        id = this.id,
        forenames = this.forenames,
        surnames = this.surnames,
        birth_date = this.birth,
        medical_info = this.medicalInfo,
        parent_id = parentId,
        driver_id = driverId,
        gender = "M",
        profile_pic = this.profilePic
    )
}
