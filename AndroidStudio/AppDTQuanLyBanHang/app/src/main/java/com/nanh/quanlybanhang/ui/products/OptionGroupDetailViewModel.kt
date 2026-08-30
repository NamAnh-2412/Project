package com.nanh.quanlybanhang.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.OptionItemEntity
import com.nanh.quanlybanhang.util.formatVndCompact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OptionGroupDetailViewModel(
    private val catalogDao: CatalogDao,
    private val groupId: Long,
) : ViewModel() {

    val group = catalogDao.observeOptionGroupById(groupId)
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val items = catalogDao.observeOptionItemsForGroup(groupId)
        .map { list ->
            list.map { e ->
                OptionItemRowUi(
                    id = e.id,
                    name = e.name,
                    extraPrice = e.extraPrice,
                    extraPriceLabel = formatExtraLabel(e.extraPrice),
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun formatExtraLabel(price: Double): String =
        if (price <= 0.0) formatVndCompact(0.0) else "+${formatVndCompact(price)}"

    fun insertItem(name: String, extraPrice: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || groupId <= 0L || extraPrice < 0) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.insertOptionItem(
                OptionItemEntity(optionGroupId = groupId, name = trimmed, extraPrice = extraPrice),
            )
        }
    }

    fun updateItem(itemId: Long, name: String, extraPrice: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || itemId <= 0L || extraPrice < 0) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.updateOptionItem(itemId, trimmed, extraPrice)
        }
    }

    fun deleteItem(itemId: Long) {
        if (itemId <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.deleteOptionItemById(itemId)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp, groupId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OptionGroupDetailViewModel(app.catalogDao, groupId) as T
            }
    }
}
