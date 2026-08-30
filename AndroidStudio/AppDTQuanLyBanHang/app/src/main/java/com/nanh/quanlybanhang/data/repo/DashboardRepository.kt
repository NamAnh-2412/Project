package com.nanh.quanlybanhang.data.repo

import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.CustomerEntity
import com.nanh.quanlybanhang.data.local.OrderDao
import com.nanh.quanlybanhang.data.local.OrderEntity
import com.nanh.quanlybanhang.data.local.OrderLineEntity
import com.nanh.quanlybanhang.data.local.OrderStatus
import com.nanh.quanlybanhang.data.local.ProductEntity
import com.nanh.quanlybanhang.data.local.ProductScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class DashboardRepository(
    private val orderDao: OrderDao,
    private val catalogDao: CatalogDao,
) {

    fun observeDashboardSnapshot(zoneId: ZoneId = ZoneId.systemDefault()): Flow<DashboardSnapshot> {
        return combine(
            orderDao.observeOrders(),
            orderDao.observeOrderLines(),
            orderDao.observeCustomers(),
            catalogDao.observeProductsByScope(ProductScope.SALE),
        ) { orders, lines, customers, products ->
            buildSnapshot(orders, lines, customers, products, zoneId)
        }.flowOn(Dispatchers.Default)
    }

    suspend fun seedIfEmpty() {
        /* Không nhập dữ liệu mẫu — người dùng thêm danh mục và sản phẩm trong màn Sản phẩm. */
    }

    private fun buildSnapshot(
        orders: List<OrderEntity>,
        lines: List<OrderLineEntity>,
        customers: List<CustomerEntity>,
        products: List<ProductEntity>,
        zoneId: ZoneId,
    ): DashboardSnapshot {
        val customerById = customers.associateBy { it.id }
        val productById = products.associateBy { it.id }
        val linesByOrderId = lines.groupBy { it.orderId }

        val today = LocalDate.now(zoneId)
        val yesterday = today.minusDays(1)

        fun LocalDate.bounds(): Pair<Long, Long> {
            val start = this.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = this.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            return start to end
        }

        val (tStart, tEnd) = today.bounds()
        val (yStart, yEnd) = yesterday.bounds()

        fun inDay(millis: Long, start: Long, endExclusive: Long) =
            millis >= start && millis < endExclusive

        val todayOrdersList = orders.filter { o ->
            inDay(o.createdAtEpochMillis, tStart, tEnd) && o.status != OrderStatus.CANCELLED
        }
        val yesterdayOrdersList = orders.filter { o ->
            inDay(o.createdAtEpochMillis, yStart, yEnd) && o.status != OrderStatus.CANCELLED
        }

        val todayCompleted = todayOrdersList.filter { it.status == OrderStatus.COMPLETED }
        val yesterdayCompleted = yesterdayOrdersList.filter { it.status == OrderStatus.COMPLETED }

        val todayRevenue = todayCompleted.sumOf { it.totalAmount }
        val yesterdayRevenue = yesterdayCompleted.sumOf { it.totalAmount }

        val todayOrders = todayOrdersList.size
        val yesterdayOrders = yesterdayOrdersList.size

        fun distinctCustomers(list: List<OrderEntity>) =
            list.mapNotNull { it.customerId }.toSet().size

        val todayCustomers = distinctCustomers(todayOrdersList)
        val yesterdayCustomers = distinctCustomers(yesterdayOrdersList)

        fun unitsForCompleted(completed: List<OrderEntity>): Int {
            val ids = completed.map { it.id }.toSet()
            return lines.filter { it.orderId in ids }.sumOf { it.quantity }
        }

        val todayUnits = unitsForCompleted(todayCompleted)
        val yesterdayUnits = unitsForCompleted(yesterdayCompleted)

        val stats = DashboardStatsRaw(
            todayRevenue = todayRevenue,
            yesterdayRevenue = yesterdayRevenue,
            todayOrders = todayOrders,
            yesterdayOrders = yesterdayOrders,
            todayCustomers = todayCustomers,
            yesterdayCustomers = yesterdayCustomers,
            todayUnits = todayUnits,
            yesterdayUnits = yesterdayUnits,
        )

        val monthWindow = List(6) { i -> YearMonth.from(today).minusMonths((5 - i).toLong()) }
        val monthlyTotals = monthWindow.map { ym ->
            val start = ym.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = ym.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val total = orders
                .filter { it.status == OrderStatus.COMPLETED }
                .filter { it.createdAtEpochMillis >= start && it.createdAtEpochMillis < end }
                .sumOf { it.totalAmount }
            MonthTotal(ym, total)
        }

        val recentRaw = orders
            .asSequence()
            .filter { it.status != OrderStatus.CANCELLED }
            .sortedByDescending { it.createdAtEpochMillis }
            .take(12)
            .map { order ->
                val osLines = linesByOrderId[order.id].orEmpty().sortedBy { it.id }
                val firstLine = osLines.firstOrNull()
                val firstProduct = firstLine?.let { productById[it.productId] }
                val title = when {
                    firstProduct == null -> "Đơn #${order.id}"
                    osLines.size <= 1 -> firstProduct.name
                    else -> "${firstProduct.name} (+${osLines.size - 1})"
                }
                val customerLabel = order.customerId?.let { customerById[it]?.name } ?: "Khách lẻ"
                RecentOrderRaw(
                    createdAtEpochMillis = order.createdAtEpochMillis,
                    customerLabel = customerLabel,
                    title = title,
                    totalAmount = order.totalAmount,
                    completed = order.status == OrderStatus.COMPLETED,
                    categoryId = firstProduct?.categoryId ?: 0L,
                )
            }
            .toList()

        return DashboardSnapshot(
            stats = stats,
            monthlyTotals = monthlyTotals,
            recentOrders = recentRaw,
        )
    }
}
