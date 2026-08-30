package com.nanh.quanlybanhang.ui.orders

enum class OrderRowStatus {
    COMPLETED,
    PENDING,
    CANCELLED,
}

data class OrderListUi(
    val id: Long,
    val title: String,
    val customerLine: String,
    val paymentLine: String,
    val dateLine: String,
    val price: String,
    val status: OrderRowStatus,
)
