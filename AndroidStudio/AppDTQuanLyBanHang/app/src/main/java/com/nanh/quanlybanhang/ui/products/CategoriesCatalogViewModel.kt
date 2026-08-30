package com.nanh.quanlybanhang.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CategoryEntity
import com.nanh.quanlybanhang.data.local.CatalogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DeleteCategoryResult {
    data object Success : DeleteCategoryResult
    data class BlockedByProducts(val count: Int) : DeleteCategoryResult
}

class CategoriesCatalogViewModel(
    private val catalogDao: CatalogDao,
) : ViewModel() {

    val categories = catalogDao.observeCategories()
        .map { list ->
            list.sortedBy { it.name.lowercase() }.map { CategoryRowUi(it.id, it.name) }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.insertCategory(CategoryEntity(name = trimmed))
        }
    }

    fun updateCategory(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || id <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.updateCategoryName(id, trimmed)
        }
    }

    fun deleteCategory(id: Long, onResult: (DeleteCategoryResult) -> Unit) {
        if (id <= 0L) return
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                catalogDao.productCountForCategory(id)
            }
            if (count > 0) {
                onResult(DeleteCategoryResult.BlockedByProducts(count))
                return@launch
            }
            withContext(Dispatchers.IO) {
                catalogDao.deleteCategoryById(id)
            }
            onResult(DeleteCategoryResult.Success)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CategoriesCatalogViewModel(app.catalogDao) as T
            }
    }
}
