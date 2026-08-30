package com.nanh.quanlybanhang.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.OrderDao
import com.nanh.quanlybanhang.data.local.OrderStatus
import com.nanh.quanlybanhang.util.VietnameseDates
import com.nanh.quanlybanhang.util.formatVndCompact
import java.text.Normalizer
import java.util.Locale
import kotlin.text.CharCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class OrdersListViewModel(
    private val orderDao: OrderDao,
    private val app: QuanLyBanHangApp,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val orders = combine(
        orderDao.observeOrders(),
        orderDao.observeCustomers(),
        _searchQuery,
    ) { orderRows, customerRows, queryRaw ->
        val customers = customerRows.associateBy { it.id }
        val queryNorm = normalizeSearch(queryRaw)
        val retailLabel = app.getString(R.string.place_order_customer_retail)
        val regularLabel = app.getString(R.string.place_order_customer_regular)

        orderRows
            .sortedByDescending { it.createdAtEpochMillis }
            .map { o ->
                val status = when (o.status) {
                    OrderStatus.COMPLETED -> OrderRowStatus.COMPLETED
                    OrderStatus.PENDING -> OrderRowStatus.PENDING
                    OrderStatus.CANCELLED -> OrderRowStatus.CANCELLED
                    else -> OrderRowStatus.PENDING
                }
                val customer = o.customerId?.let { customers[it] }
                val paymentLabel = CheckoutPaymentMethods.label(app, o.paymentMethodIndex)
                val customerLine = customer?.name ?: retailLabel
                val typeLabel = if (customer == null) retailLabel else regularLabel
                val ui = OrderListUi(
                    id = o.id,
                    title = "Đơn #${o.id}",
                    customerLine = customerLine,
                    paymentLine = paymentLabel,
                    dateLine = VietnameseDates.formatOrderDateTime(o.createdAtEpochMillis),
                    price = formatVndCompact(o.totalAmount),
                    status = status,
                )
                val haystack = normalizeSearch(
                    listOf(
                        paymentLabel,
                        customerLine,
                        customer?.phone.orEmpty(),
                        typeLabel,
                        ui.title,
                        o.id.toString(),
                    ).joinToString(" "),
                )
                ui to haystack
            }
            .filter { (_, haystack) ->
                queryNorm.isEmpty() || haystack.contains(queryNorm)
            }
            .map { it.first }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OrdersListViewModel(app.orderDao, app) as T
                }
            }
    }
}

private fun normalizeSearch(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    val nfd = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
    return buildString {
        for (ch in nfd) {
            if (ch.category != CharCategory.NON_SPACING_MARK) append(ch)
        }
    }.lowercase(Locale.ROOT)
}
