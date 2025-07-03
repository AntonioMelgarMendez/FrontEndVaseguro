package com.VaSeguro.data.Dao.Stops


import androidx.room.*
import com.VaSeguro.data.Entitys.Stops.StopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Query("SELECT * FROM stops")
    suspend fun getAllStops(): List<StopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStop(stop: StopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("DELETE FROM stops")
    suspend fun clearStops()
}