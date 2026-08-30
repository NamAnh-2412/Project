package com.nanh.quanlybanhang.data.repo

import java.time.YearMonth

data class DashboardSnapshot(
    val stats: DashboardStatsRaw,
    val monthlyTotals: List<MonthTotal>,
    val recentOrders: List<RecentOrderRaw>,
)

data class DashboardStatsRaw(
    val todayRevenue: Double,
    val yesterdayRevenue: Double,
    val todayOrders: Int,
    val yesterdayOrders: Int,
    val todayCustomers: Int,
    val yesterdayCustomers: Int,
    val todayUnits: Int,
    val yesterdayUnits: Int,
)

data class MonthTotal(
    val yearMonth: YearMonth,
    val revenue: Double,
)

data class RecentOrderRaw(
    val createdAtEpochMillis: Long,
    val customerLabel: String,
    val title: String,
    val totalAmount: Double,
    val completed: Boolean,
    val categoryId: Long,
)
