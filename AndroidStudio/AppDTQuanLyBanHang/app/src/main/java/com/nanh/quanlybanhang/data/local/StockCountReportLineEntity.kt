package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_count_report_lines",
    foreignKeys = [
        ForeignKey(
            entity = StockCountReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["reportId"]),
        Index(value = ["productId"]),
    ],
)
data class StockCountReportLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val productId: Long,
    /** Tồn trên hệ thống trước khi cập nhật theo kiểm kê. */
    val systemQuantityBefore: Int,
    /** Số lượng thực tế ghi nhận trên phiếu. */
    val countedQuantity: Int,
)
