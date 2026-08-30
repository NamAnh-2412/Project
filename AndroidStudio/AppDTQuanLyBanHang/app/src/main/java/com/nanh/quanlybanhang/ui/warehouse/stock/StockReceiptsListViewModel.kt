package com.nanh.quanlybanhang.ui.warehouse.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.StockReceiptDao
import com.nanh.quanlybanhang.data.local.SupplierDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class StockReceiptsListViewModel(
    stockReceiptDao: StockReceiptDao,
    supplierDao: SupplierDao,
) : ViewModel() {

    val receipts = combine(
        stockReceiptDao.observeReceipts(),
        supplierDao.observeSuppliers(),
    ) { receipts, suppliers ->
        val smap = suppliers.associateBy { it.id }
        receipts.map { r ->
            StockReceiptListRow(
                id = r.id,
                supplierName = smap[r.supplierId]?.name ?: "—",
                createdAtEpochMillis = r.createdAtEpochMillis,
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StockReceiptsListViewModel(app.stockReceiptDao, app.supplierDao) as T
                }
            }
    }
}
