package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockCountReportDao {

    @Insert
    suspend fun insertReport(report: StockCountReportEntity): Long

    @Insert
    suspend fun insertLine(line: StockCountReportLineEntity): Long

    @Query("SELECT * FROM stock_count_reports ORDER BY createdAtEpochMillis DESC")
    fun observeReports(): Flow<List<StockCountReportEntity>>

    @Query("SELECT COUNT(*) FROM stock_count_report_lines WHERE reportId = :reportId")
    suspend fun lineCountForReport(reportId: Long): Int
}
