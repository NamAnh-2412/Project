package com.nanh.quanlybanhang.ui.warehouse.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.ProductScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockProductsViewModel(
    private val catalogDao: CatalogDao,
) : ViewModel() {

    val products = combine(
        catalogDao.observeProductsByScope(ProductScope.WAREHOUSE),
        catalogDao.observeCategories(),
    ) { prods, cats ->
        val cmap = cats.associateBy { it.id }
        prods
            .sortedBy { it.name.lowercase() }
            .map { p ->
                StockProductRowUi(
                    id = p.id,
                    name = p.name,
                    categoryName = cmap[p.categoryId]?.name ?: "—",
                    stockQuantity = p.stockQuantity,
                )
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addStock(productId: Long, delta: Int) {
        if (delta <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.adjustProductStock(productId, delta)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StockProductsViewModel(app.catalogDao) as T
                }
            }
    }
}
