package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_count_reports",
    indices = [Index(value = ["createdAtEpochMillis"])],
)
data class StockCountReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtEpochMillis: Long,
    val note: String?,
)
