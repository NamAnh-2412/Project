package com.nanh.quanlybanhang.ui.warehouse.stock

data class StockCountLineUi(
    val productId: Long,
    val name: String,
    val categoryName: String,
    val systemStock: Int,
    val qtyInput: String,
)
