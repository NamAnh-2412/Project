package com.nanh.quanlybanhang.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.repo.ReportsRawSnapshot
import com.nanh.quanlybanhang.data.repo.ReportsRepository
import com.nanh.quanlybanhang.util.formatVndGrouped
import com.nanh.quanlybanhang.ui.orders.CheckoutPaymentMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HourBarUi(
    val hour: Int,
    val label: String,
    val amount: Double,
    val valueLabel: String,
)

data class PaymentMethodRowUi(
    val title: String,
    val ordersLabel: String,
    val revenueLabel: String,
)

data class ReportsUiState(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val rangeLabel: String,
    val orderCount: Int,
    val totalRevenueLabel: String,
    val itemsSold: Int,
    val hourlyBars: List<HourBarUi>,
    val hourlyBarMax: Double,
    val paymentRows: List<PaymentMethodRowUi>,
    val isLoading: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(
    application: Application,
    private val repository: ReportsRepository,
) : AndroidViewModel(application) {

    private val range = MutableStateFlow(Pair(LocalDate.now(), LocalDate.now()))

    val uiState = range
        .flatMapLatest { (from, to) ->
            flow {
                emit(skeletonLoading(from, to))
                val raw = withContext(Dispatchers.IO) {
                    repository.loadRange(from, to)
                }
                emit(raw.toUiState(getApplication()))
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            skeletonLoading(
                LocalDate.now(),
                LocalDate.now(),
            ),
        )

    fun setDateRange(from: LocalDate, to: LocalDate) {
        var f = from
        var t = to
        if (f > t) {
            val x = f
            f = t
            t = x
        }
        range.value = f to t
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsViewModel(app, app.reportsRepository) as T
                }
            }
    }
}

private fun skeletonLoading(from: LocalDate, to: LocalDate): ReportsUiState {
    val locale = Locale.forLanguageTag("vi-VN")
    val df = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    val label = if (from == to) from.format(df) else "${from.format(df)} – ${to.format(df)}"
    return ReportsUiState(
        fromDate = from,
        toDate = to,
        rangeLabel = label,
        orderCount = 0,
        totalRevenueLabel = "—",
        itemsSold = 0,
        hourlyBars = emptyList(),
        hourlyBarMax = 1.0,
        paymentRows = emptyList(),
        isLoading = true,
    )
}

private fun ReportsRawSnapshot.toUiState(app: Application): ReportsUiState {
    val locale = Locale.forLanguageTag("vi-VN")
    val df = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    val rangeLabel = if (fromDate == toDate) {
        fromDate.format(df)
    } else {
        "${fromDate.format(df)} – ${toDate.format(df)}"
    }
    val maxH = revenueByHour.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val hourly = revenueByHour.mapIndexed { h, amt ->
        HourBarUi(
            hour = h,
            label = "${h}h",
            amount = amt,
            valueLabel = formatVndGrouped(amt),
        )
    }
    val payments = paymentBuckets.map { b ->
        PaymentMethodRowUi(
            title = CheckoutPaymentMethods.label(app, b.paymentMethodIndex),
            ordersLabel = app.getString(R.string.reports_orders_count_fmt, b.orderCount),
            revenueLabel = formatVndGrouped(b.revenue),
        )
    }
    return ReportsUiState(
        fromDate = fromDate,
        toDate = toDate,
        rangeLabel = rangeLabel,
        orderCount = orderCount,
        totalRevenueLabel = formatVndGrouped(totalRevenue),
        itemsSold = itemsSold,
        hourlyBars = hourly,
        hourlyBarMax = maxH,
        paymentRows = payments,
        isLoading = false,
    )
}
