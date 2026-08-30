package com.nanh.quanlybanhang.ui.warehouse.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.data.repo.WarehouseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockCountEditorViewModel(
    private val catalogDao: CatalogDao,
    private val warehouseRepository: WarehouseRepository,
) : ViewModel() {

    private data class ProductBrief(
        val id: Long,
        val name: String,
        val categoryName: String,
        val stockQuantity: Int,
    )

    private val briefRows = combine(
        catalogDao.observeProductsByScope(ProductScope.WAREHOUSE),
        catalogDao.observeCategories(),
    ) { prods, cats ->
        val cmap = cats.associateBy { it.id }
        prods
            .sortedBy { it.name.lowercase() }
            .map { p ->
                ProductBrief(
                    id = p.id,
                    name = p.name,
                    categoryName = cmap[p.categoryId]?.name ?: "—",
                    stockQuantity = p.stockQuantity,
                )
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val userQtyEdits = MutableStateFlow<Map<Long, String>>(emptyMap())

    val lines = combine(briefRows, userQtyEdits) { rows, edits ->
        rows.map { r ->
            val qty = edits[r.id] ?: r.stockQuantity.toString()
            StockCountLineUi(
                productId = r.id,
                name = r.name,
                categoryName = r.categoryName,
                systemStock = r.stockQuantity,
                qtyInput = qty,
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQty(productId: Long, text: String) {
        userQtyEdits.update { it + (productId to text) }
    }

    fun save(note: String?, onResult: (SaveStockCountResult) -> Unit) {
        viewModelScope.launch {
            val current = lines.value
            if (current.isEmpty()) {
                onResult(SaveStockCountResult.NoProducts)
                return@launch
            }
            val pairs = mutableListOf<Pair<Long, Int>>()
            for (line in current) {
                val n = line.qtyInput.trim().toIntOrNull()
                if (n == null || n < 0) {
                    onResult(SaveStockCountResult.InvalidQuantity(line.name))
                    return@launch
                }
                pairs.add(line.productId to n)
            }
            withContext(Dispatchers.IO) {
                warehouseRepository.createStockCountReport(note, pairs)
            }
            onResult(SaveStockCountResult.Success)
        }
    }

    sealed interface SaveStockCountResult {
        data object Success : SaveStockCountResult
        data object NoProducts : SaveStockCountResult
        data class InvalidQuantity(val productName: String) : SaveStockCountResult
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StockCountEditorViewModel(app.catalogDao, app.warehouseRepository) as T
            }
    }
}
