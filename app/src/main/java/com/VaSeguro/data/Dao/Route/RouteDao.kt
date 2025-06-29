package com.VaSeguro.data.Dao.Route

import androidx.room.*
import com.VaSeguro.data.Entitys.Routes.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes")
    suspend fun getAllRoutes(): List<RouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Query("DELETE FROM routes")
    suspend fun clearRoutes()
}