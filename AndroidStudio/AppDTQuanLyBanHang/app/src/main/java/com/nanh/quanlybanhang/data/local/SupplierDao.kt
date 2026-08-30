package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM suppliers ORDER BY name COLLATE NOCASE ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Insert
    suspend fun insertSupplier(supplier: SupplierEntity): Long
}
