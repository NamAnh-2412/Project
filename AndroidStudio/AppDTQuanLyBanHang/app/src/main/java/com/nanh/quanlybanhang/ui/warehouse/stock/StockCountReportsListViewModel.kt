package com.nanh.quanlybanhang.ui.warehouse.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.StockCountReportDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockCountReportsListViewModel(
    private val stockCountReportDao: StockCountReportDao,
    private val dateFormat: SimpleDateFormat,
) : ViewModel() {

    val reports = stockCountReportDao.observeReports()
        .map { list ->
            list.map { r ->
                StockCountReportListRow(
                    id = r.id,
                    dateLabel = dateFormat.format(Date(r.createdAtEpochMillis)),
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
                    val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    return StockCountReportsListViewModel(app.stockCountReportDao, fmt) as T
                }
            }
    }
}
