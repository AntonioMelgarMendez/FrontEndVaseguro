package com.VaSeguro.data.Entitys.Children


import androidx.room.Entity
import androidx.room.PrimaryKey

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