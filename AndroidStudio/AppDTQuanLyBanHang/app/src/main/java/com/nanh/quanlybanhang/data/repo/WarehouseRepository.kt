package com.nanh.quanlybanhang.data.repo

import androidx.room.withTransaction
import com.nanh.quanlybanhang.data.local.AppDatabase
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.StockCountReportDao
import com.nanh.quanlybanhang.data.local.StockCountReportEntity
import com.nanh.quanlybanhang.data.local.StockCountReportLineEntity
import com.nanh.quanlybanhang.data.local.StockReceiptDao
import com.nanh.quanlybanhang.data.local.StockReceiptEntity
import com.nanh.quanlybanhang.data.local.StockReceiptLineEntity

class WarehouseRepository(
    private val db: AppDatabase,
    private val catalogDao: CatalogDao,
    private val stockReceiptDao: StockReceiptDao,
    private val stockCountReportDao: StockCountReportDao,
) {

    /** @param lines cặp (productId, quantity) quantity > 0 */
    suspend fun createStockReceipt(supplierId: Long, note: String?, lines: List<Pair<Long, Int>>): Long =
        db.withTransaction {
            val rid = stockReceiptDao.insertReceipt(
                StockReceiptEntity(
                    supplierId = supplierId,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
            for ((productId, qty) in lines) {
                if (qty <= 0) continue
                stockReceiptDao.insertLine(
                    StockReceiptLineEntity(receiptId = rid, productId = productId, quantity = qty),
                )
                catalogDao.adjustProductStock(productId, qty)
            }
            rid
        }

    /**
     * Phiếu kiểm kê / báo cáo tồn: lưu lịch sử và gán [products.stockQuantity] = số đếm (mỗi dòng >= 0).
     */
    suspend fun createStockCountReport(note: String?, lines: List<Pair<Long, Int>>): Long =
        db.withTransaction {
            val reportId = stockCountReportDao.insertReport(
                StockCountReportEntity(
                    createdAtEpochMillis = System.currentTimeMillis(),
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
            for ((productId, counted) in lines) {
                if (counted < 0) continue
                val product = catalogDao.getProductById(productId) ?: continue
                stockCountReportDao.insertLine(
                    StockCountReportLineEntity(
                        reportId = reportId,
                        productId = productId,
                        systemQuantityBefore = product.stockQuantity,
                        countedQuantity = counted,
                    ),
                )
                catalogDao.setProductStock(productId, counted)
            }
            reportId
        }
}
