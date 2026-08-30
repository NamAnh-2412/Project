package com.nanh.quanlybanhang.ui.warehouse.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.data.local.SupplierDao
import com.nanh.quanlybanhang.data.repo.WarehouseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class StockReceiptEditorViewModel(
    catalogDao: CatalogDao,
    supplierDao: SupplierDao,
    private val warehouseRepository: WarehouseRepository,
) : ViewModel() {

    val suppliers = supplierDao.observeSuppliers()
        .map { list -> list.sortedBy { it.name.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val products = catalogDao.observeProductsByScope(ProductScope.WAREHOUSE)
        .map { list -> list.sortedBy { it.name.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _supplierId = MutableStateFlow<Long?>(null)
    val supplierId: StateFlow<Long?> = _supplierId.asStateFlow()

    private val _lines = MutableStateFlow<List<ReceiptLineDraft>>(emptyList())
    val lines: StateFlow<List<ReceiptLineDraft>> = _lines.asStateFlow()

    fun setSupplier(id: Long?) {
        _supplierId.value = id
    }

    fun addLine(productId: Long, productName: String, quantity: Int) {
        if (quantity <= 0) return
        _lines.update { old ->
            val i = old.indexOfFirst { it.productId == productId }
            if (i >= 0) {
                old.toMutableList().apply {
                    val cur = this[i]
                    this[i] = cur.copy(quantity = cur.quantity + quantity)
                }
            } else {
                old + ReceiptLineDraft(productId, productName, quantity)
            }
        }
    }

    fun removeLineAt(index: Int) {
        _lines.update { list -> list.filterIndexed { idx, _ -> idx != index } }
    }

    suspend fun saveReceipt(note: String?): Result<Long> = withContext(Dispatchers.IO) {
        val sid = _supplierId.value ?: return@withContext Result.failure(IllegalStateException("supplier"))
        val pairs = _lines.value.map { it.productId to it.quantity }.filter { it.second > 0 }
        if (pairs.isEmpty()) return@withContext Result.failure(IllegalStateException("lines"))
        runCatching { warehouseRepository.createStockReceipt(sid, note, pairs) }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StockReceiptEditorViewModel(
                        app.catalogDao,
                        app.supplierDao,
                        app.warehouseRepository,
                    ) as T
                }
            }
    }
}
