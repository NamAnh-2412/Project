package com.nanh.quanlybanhang.ui.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.SupplierDao
import com.nanh.quanlybanhang.data.local.SupplierEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SuppliersViewModel(
    private val supplierDao: SupplierDao,
) : ViewModel() {

    val suppliers = supplierDao.observeSuppliers()
        .map { rows -> rows.map { it.toListUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addSupplier(
        name: String,
        phone: String?,
        address: String?,
        note: String?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            supplierDao.insertSupplier(
                SupplierEntity(
                    name = name.trim(),
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    address = address?.trim()?.takeIf { it.isNotEmpty() },
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    createdAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SuppliersViewModel(app.supplierDao) as T
                }
            }
    }
}

private fun SupplierEntity.toListUi(): SupplierListUi = SupplierListUi(
    id = id,
    name = name,
    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
    address = address?.trim()?.takeIf { it.isNotEmpty() },
    note = note?.trim()?.takeIf { it.isNotEmpty() },
)
