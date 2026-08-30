package com.nanh.quanlybanhang.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.CustomerEntity
import com.nanh.quanlybanhang.data.local.OrderDao
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
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val orderDao: OrderDao,
    private val app: QuanLyBanHangApp,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val customers = combine(
        orderDao.observeCustomers(),
        _searchQuery,
    ) { rows, queryRaw ->
        val queryNorm = normalizeSearch(queryRaw)
        rows
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            .map { entity ->
                val phoneRaw = entity.phone?.trim()?.takeIf { it.isNotEmpty() }
                val ui = CustomerManageUi(
                    id = entity.id,
                    name = entity.name,
                    phoneLine = phoneRaw ?: app.getString(R.string.customers_phone_none),
                    phoneRaw = phoneRaw,
                )
                val haystack = normalizeSearch("${entity.name} ${phoneRaw.orEmpty()}")
                ui to haystack
            }
            .filter { (_, haystack) -> queryNorm.isEmpty() || haystack.contains(queryNorm) }
            .map { it.first }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String, phone: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            orderDao.insertCustomer(
                CustomerEntity(
                    name = name.trim(),
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
        }
    }

    fun updateCustomer(id: Long, name: String, phone: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            orderDao.updateCustomer(
                CustomerEntity(
                    id = id,
                    name = name.trim(),
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            orderDao.deleteCustomerById(id)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CustomersViewModel(app.orderDao, app) as T
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
