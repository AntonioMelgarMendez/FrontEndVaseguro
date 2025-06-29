package com.VaSeguro.data.Dao.Children

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.VaSeguro.data.Entitys.Children.ChildEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ChildDao {
    @Query("SELECT * FROM children")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: Int): ChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("DELETE FROM children WHERE id = :id")
    suspend fun deleteChildById(id: Int)
}