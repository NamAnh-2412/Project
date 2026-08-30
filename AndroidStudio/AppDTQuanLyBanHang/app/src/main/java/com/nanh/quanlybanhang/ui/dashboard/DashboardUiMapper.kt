package com.nanh.quanlybanhang.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.repo.DashboardSnapshot
import com.nanh.quanlybanhang.ui.theme.ChartBlue
import com.nanh.quanlybanhang.ui.theme.ChartGold
import com.nanh.quanlybanhang.ui.theme.ChartTeal
import com.nanh.quanlybanhang.util.formatVndCompact
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun emptyDashboardUiState(): DashboardUiState {
    val months = List(6) { i -> YearMonth.now().minusMonths((5 - i).toLong()) }
    return DashboardUiState(
        statCards = listOf(
            StatCardUi(R.string.stat_total_revenue, formatVndCompact(0.0), "—", true),
            StatCardUi(R.string.stat_new_orders, "0", "—", true),
            StatCardUi(R.string.stat_customers, "0", "—", true),
            StatCardUi(R.string.stat_products_sold, "0", "—", true),
        ),
        monthBars = months.map { ym ->
            MonthBarUi(
                label = "T${ym.monthValue}",
                valueLabel = formatVndCompact(0.0),
                amount = 0f,
                barColor = ChartGold,
            )
        },
        recentOrders = emptyList(),
    )
}

fun DashboardSnapshot.toDashboardUiState(): DashboardUiState {
    val s = stats
    val revTrend = revenueTrend(s.todayRevenue, s.yesterdayRevenue)
    val statsCards = listOf(
        StatCardUi(
            title = R.string.stat_total_revenue,
            value = formatVndCompact(s.todayRevenue),
            trendText = revTrend.first,
            trendPositive = revTrend.second,
        ),
        intStatCard(R.string.stat_new_orders, s.todayOrders, s.yesterdayOrders, "đơn"),
        intStatCard(R.string.stat_customers, s.todayCustomers, s.yesterdayCustomers, "người"),
        unitsStatCard(s.todayUnits, s.yesterdayUnits),
    )

    val monthBarsUi = monthlyTotals.mapIndexed { index, row ->
        MonthBarUi(
            label = "T${row.yearMonth.monthValue}",
            valueLabel = formatVndCompact(row.revenue),
            amount = row.revenue.toFloat(),
            barColor = barColorForMonthIndex(index),
        )
    }

    val recentUi = recentOrders.map { r ->
        RecentOrderUi(
            title = r.title,
            time = formatRecentOrderTime(r.createdAtEpochMillis),
            customer = r.customerLabel,
            price = formatVndCompact(r.totalAmount),
            completed = r.completed,
            categoryIcon = categoryIcon(r.categoryId),
            categoryId = r.categoryId,
        )
    }

    return DashboardUiState(
        statCards = statsCards,
        monthBars = monthBarsUi,
        recentOrders = recentUi,
    )
}

private fun intStatCard(title: Int, today: Int, yesterday: Int, unit: String): StatCardUi {
    val (text, positive) = intDeltaTrend(today, yesterday, unit)
    return StatCardUi(title = title, value = today.toString(), trendText = text, trendPositive = positive)
}

private fun unitsStatCard(today: Int, yesterday: Int): StatCardUi {
    val (text, positive) = percentDeltaTrend(today, yesterday)
    return StatCardUi(R.string.stat_products_sold, today.toString(), text, positive)
}

private fun barColorForMonthIndex(index: Int) = when (index) {
    1 -> ChartBlue
    2 -> ChartTeal
    else -> ChartGold
}

private fun revenueTrend(today: Double, yesterday: Double): Pair<String, Boolean> {
    if (today <= 0.0 && yesterday <= 0.0) return "—" to true
    if (yesterday <= 0.0 && today > 0.0) return "↑ mới" to true
    val pct = ((today - yesterday) / yesterday * 100.0).roundToInt()
    val positive = pct >= 0
    val arrow = if (positive) "↑" else "↓"
    return "$arrow ${abs(pct)}% so hôm qua" to positive
}

private fun intDeltaTrend(today: Int, yesterday: Int, unit: String): Pair<String, Boolean> {
    if (today == 0 && yesterday == 0) return "—" to true
    val d = today - yesterday
    val positive = d >= 0
    val arrow = if (positive) "↑" else "↓"
    return "$arrow ${abs(d)} $unit" to positive
}

private fun percentDeltaTrend(today: Int, yesterday: Int): Pair<String, Boolean> {
    if (today <= 0 && yesterday <= 0) return "—" to true
    if (yesterday <= 0 && today > 0) return "↑ mới" to true
    val pct = ((today - yesterday).toDouble() / yesterday * 100.0).roundToInt()
    val positive = pct >= 0
    val arrow = if (positive) "↑" else "↓"
    return "$arrow ${abs(pct)}%" to positive
}

private fun formatRecentOrderTime(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val zdt = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val orderDay = zdt.toLocalDate()
    val today = LocalDate.now(zone)
    val dayLabel = when {
        orderDay == today -> "Hôm nay"
        orderDay == today.minusDays(1) -> "Hôm qua"
        else -> DateTimeFormatter.ofPattern("dd/MM").format(orderDay)
    }
    val clock = DateTimeFormatter.ofPattern("HH:mm").format(zdt)
    return "$clock · $dayLabel"
}

fun categoryIcon(categoryId: Long): ImageVector {
    val bucket = if (categoryId == 0L) 3 else (categoryId.absoluteValue % 4).toInt()
    return when (bucket) {
        0 -> Icons.Outlined.Palette
        1 -> Icons.Outlined.Restaurant
        2 -> Icons.Outlined.LocalMall
        else -> Icons.Outlined.Image
    }
}
