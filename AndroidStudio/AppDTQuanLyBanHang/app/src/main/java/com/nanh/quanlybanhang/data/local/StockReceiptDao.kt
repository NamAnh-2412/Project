package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockReceiptDao {

    @Insert
    suspend fun insertReceipt(receipt: StockReceiptEntity): Long

    @Insert
    suspend fun insertLine(line: StockReceiptLineEntity): Long

    @Query("SELECT * FROM stock_receipts ORDER BY createdAtEpochMillis DESC")
    fun observeReceipts(): Flow<List<StockReceiptEntity>>

    @Query("SELECT * FROM stock_receipt_lines WHERE receiptId = :receiptId")
    suspend fun linesForReceipt(receiptId: Long): List<StockReceiptLineEntity>
}
