package com.nanh.quanlybanhang.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.ProductEntity
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.util.formatVndCompact
import com.nanh.quanlybanhang.util.ProductImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DeleteProductResult {
    data object Success : DeleteProductResult
    data class BlockedByOrders(val count: Int) : DeleteProductResult
}

class ProductItemsViewModel(
    private val catalogDao: CatalogDao,
    private val productScope: Int,
) : ViewModel() {

    val categoriesForPicker = catalogDao.observeCategories()
        .map { list -> list.sortedBy { c -> c.name.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val optionGroupsForPicker = catalogDao.observeOptionGroups()
        .map { list -> list.sortedBy { g -> g.name.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val products = combine(
        catalogDao.observeProductsByScope(productScope),
        catalogDao.observeCategories(),
        catalogDao.observeProductOptionLinks(),
    ) { prods, cats, links ->
        val map = cats.associateBy { it.id }
        val linksByProduct = links.groupBy { it.productId }.mapValues { (_, rows) ->
            rows.sortedBy { it.sortOrder }.map { it.optionGroupId }
        }
        prods
            .sortedBy { it.name.lowercase() }
            .map { p ->
                ProductListUi(
                    id = p.id,
                    name = p.name,
                    priceLabel = formatVndCompact(p.unitPrice),
                    unitPrice = p.unitPrice,
                    categoryId = p.categoryId,
                    categoryName = map[p.categoryId]?.name ?: "—",
                    imageLocalPath = p.imageLocalPath,
                    linkedOptionGroupIds = linksByProduct[p.id].orEmpty(),
                )
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertProduct(
        name: String,
        unitPrice: Double,
        categoryId: Long,
        imageLocalPath: String?,
        linkedOptionGroupIds: List<Long>,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || unitPrice <= 0 || categoryId <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            val pid = catalogDao.insertProduct(
                ProductEntity(
                    name = trimmed,
                    unitPrice = unitPrice,
                    categoryId = categoryId,
                    scope = productScope,
                    imageLocalPath = imageLocalPath?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
            catalogDao.replaceProductOptionGroups(pid, linkedOptionGroupIds)
        }
    }

    fun updateProduct(
        id: Long,
        name: String,
        unitPrice: Double,
        categoryId: Long,
        imageLocalPathForDb: String?,
        linkedOptionGroupIds: List<Long>,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || unitPrice <= 0 || categoryId <= 0L || id <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            val prev = catalogDao.getProductById(id) ?: return@launch
            val normalizedPath = imageLocalPathForDb?.trim()?.takeIf { it.isNotEmpty() }
            catalogDao.updateProduct(id, trimmed, unitPrice, categoryId, normalizedPath)
            catalogDao.replaceProductOptionGroups(id, linkedOptionGroupIds)
            val oldPath = prev.imageLocalPath?.trim()?.takeIf { it.isNotEmpty() }
            if (!oldPath.isNullOrBlank() && oldPath != normalizedPath) {
                ProductImageStorage.deleteIfExists(oldPath)
            }
        }
    }

    fun deleteProduct(id: Long, onResult: (DeleteProductResult) -> Unit) {
        if (id <= 0L) return
        viewModelScope.launch {
            val blockedCount = withContext(Dispatchers.IO) {
                catalogDao.orderLineCountForProduct(id)
            }
            if (blockedCount > 0) {
                onResult(DeleteProductResult.BlockedByOrders(blockedCount))
                return@launch
            }
            val prev = withContext(Dispatchers.IO) {
                catalogDao.getProductById(id)
            }
            withContext(Dispatchers.IO) {
                catalogDao.deleteProductById(id)
                ProductImageStorage.deleteIfExists(prev?.imageLocalPath)
            }
            onResult(DeleteProductResult.Success)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp, productScope: Int = ProductScope.SALE): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProductItemsViewModel(app.catalogDao, productScope) as T
            }
    }
}
