package com.nanh.quanlybanhang.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.OptionGroupEntity
import com.nanh.quanlybanhang.data.local.CatalogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChoiceGroupsCatalogViewModel(
    private val catalogDao: CatalogDao,
) : ViewModel() {

    val choiceGroups = catalogDao.observeOptionGroups()
        .map { list ->
            list.sortedBy { it.name.lowercase() }.map { OptionGroupRowUi(it.id, it.name) }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertOptionGroup(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.insertOptionGroup(OptionGroupEntity(name = trimmed))
        }
    }

    fun updateOptionGroup(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || id <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.updateOptionGroupName(id, trimmed)
        }
    }

    fun deleteOptionGroup(id: Long) {
        if (id <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            catalogDao.deleteOptionGroupById(id)
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChoiceGroupsCatalogViewModel(app.catalogDao) as T
            }
    }
}
