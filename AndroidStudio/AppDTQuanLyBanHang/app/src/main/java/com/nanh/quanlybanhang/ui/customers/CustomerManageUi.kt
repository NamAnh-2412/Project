package com.nanh.quanlybanhang.ui.customers

data class CustomerManageUi(
    val id: Long,
    val name: String,
    /** Chuỗi hiển thị (có thể là "Chưa có SĐT"). */
    val phoneLine: String,
    /** Số gốc để điền form sửa; null nếu không có. */
    val phoneRaw: String?,
)
