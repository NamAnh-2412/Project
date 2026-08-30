package com.nanh.quanlybanhang.ui.dashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class StatCardUi(
    val title: Int,
    val value: String,
    val trendText: String,
    val trendPositive: Boolean,
)

data class MonthBarUi(
    val label: String,
    val valueLabel: String,
    val amount: Float,
    val barColor: Color,
)

data class RecentOrderUi(
    val title: String,
    val time: String,
    val customer: String,
    val price: String,
    val completed: Boolean,
    val categoryIcon: ImageVector,
    /** Để XML dashboard chọn drawable theo danh mục */
    val categoryId: Long = 0L,
)

data class DashboardUiState(
    val statCards: List<StatCardUi>,
    val monthBars: List<MonthBarUi>,
    val recentOrders: List<RecentOrderUi>,
)
