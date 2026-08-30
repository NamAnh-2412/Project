package com.nanh.quanlybanhang.ui.products

data class ProductListUi(
    val id: Long,
    val name: String,
    val priceLabel: String,
    val unitPrice: Double,
    val categoryId: Long,
    val categoryName: String,
    val imageLocalPath: String?,
    /** Id các nhóm lựa chọn gắn với sản phẩm (thứ tự lưu trong DB). */
    val linkedOptionGroupIds: List<Long>,
)
