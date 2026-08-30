package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["createdAtEpochMillis"]),
        Index(value = ["customerId"]),
    ],
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtEpochMillis: Long,
    val customerId: Long?,
    /** COMPLETED | PENDING | CANCELLED */
    val status: String,
    /** Tổng đơn VND (đồng bộ với tổng dòng khi hoàn thành) */
    val totalAmount: Double,
    /** Chỉ số hàng thứ tự trong [CheckoutPaymentMethods]; gửi từ dialog thanh toán khi lưu đơn. */
    val paymentMethodIndex: Int? = null,
    /** Số tiền khách trả (VNĐ) tại quầy; null nếu không lưu. */
    val customerPayAmount: Double? = null,
)
