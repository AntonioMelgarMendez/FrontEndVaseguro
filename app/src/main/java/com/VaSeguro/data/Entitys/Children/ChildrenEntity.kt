package com.VaSeguro.data.Entitys.Children


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.VaSeguro.data.model.Children.Children

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: Int,
    val forenames: String,
    val surnames: String,
    val birthDate: String,
    val medicalInfo: String,
    val gender: String,
    val parentId: Int,
    val driverId: Int,
    val profilePic: String? // URI or path
)

fun Children.toEntity(): ChildEntity {
    return ChildEntity(
        id = this.id,
        forenames = this.forenames,
        surnames = this.surnames,
        birthDate = this.birth_date,
        medicalInfo = this.medical_info,
        gender = this.gender,
        parentId = this.parent_id,
        driverId = this.driver_id,
        profilePic = this.profile_pic
    )
}

fun ChildEntity.toChild(): Children {
    return Children(
        id = this.id,
        forenames = this.forenames,
        surnames = this.surnames,
        birth_date = this.birthDate,
        medical_info = this.medicalInfo,
        gender = this.gender,
        parent_id = this.parentId,
        driver_id = this.driverId,
        profile_pic = this.profilePic
    )
}