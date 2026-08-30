package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_receipt_lines",
    foreignKeys = [
        ForeignKey(
            entity = StockReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
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
        Index(value = ["receiptId"]),
        Index(value = ["productId"]),
    ],
)
data class StockReceiptLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: Long,
    val productId: Long,
    /** Số lượng nhập (dương). */
    val quantity: Int,
)
