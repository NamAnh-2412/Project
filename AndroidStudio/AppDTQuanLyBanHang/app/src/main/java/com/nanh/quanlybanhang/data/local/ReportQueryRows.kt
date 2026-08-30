package com.nanh.quanlybanhang.data.local

import androidx.room.ColumnInfo

/** Thống kê đơn hoàn thành trong khoảng [startMs, endMs). */
data class CompletedOrderStatsRow(
    @ColumnInfo(name = "orderCount") val orderCount: Int,
    @ColumnInfo(name = "totalRevenue") val totalRevenue: Double,
)

/** Gom nhóm theo hình thức thanh toán (paymentMethodIndex NULL = đơn cũ chưa lưu). */
data class PaymentMethodBucketRow(
    @ColumnInfo(name = "paymentMethodIndex") val paymentMethodIndex: Int?,
    @ColumnInfo(name = "orderCount") val orderCount: Int,
    @ColumnInfo(name = "revenue") val revenue: Double,
)
