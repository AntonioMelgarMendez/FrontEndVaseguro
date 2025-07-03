package com.VaSeguro.data.Dao.DriverCode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.VaSeguro.data.Entitys.DriverCode.DriverCodeEntity

@Dao
interface DriverCodeDao {
    @Query("SELECT code FROM driver_code WHERE id = 0 LIMIT 1")
    suspend fun getDriverCode(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDriverCode(entity: DriverCodeEntity)
}