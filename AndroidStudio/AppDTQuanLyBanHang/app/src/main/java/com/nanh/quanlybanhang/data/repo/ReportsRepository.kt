package com.nanh.quanlybanhang.data.repo

import com.nanh.quanlybanhang.data.local.OrderStatus
import com.nanh.quanlybanhang.data.local.PaymentMethodBucketRow
import com.nanh.quanlybanhang.data.local.OrderDao
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReportsRawSnapshot(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val orderCount: Int,
    val totalRevenue: Double,
    val itemsSold: Int,
    /** index 0–23, tổng doanh thu theo giờ (theo giờ địa phương), gộp mọi ngày trong khoảng. */
    val revenueByHour: DoubleArray,
    val paymentBuckets: List<PaymentMethodBucketRow>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportsRawSnapshot

        if (fromDate != other.fromDate) return false
        if (toDate != other.toDate) return false
        if (orderCount != other.orderCount) return false
        if (totalRevenue != other.totalRevenue) return false
        if (itemsSold != other.itemsSold) return false
        if (!revenueByHour.contentEquals(other.revenueByHour)) return false
        if (paymentBuckets != other.paymentBuckets) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromDate.hashCode()
        result = 31 * result + toDate.hashCode()
        result = 31 * result + orderCount
        result = 31 * result + totalRevenue.hashCode()
        result = 31 * result + itemsSold
        result = 31 * result + revenueByHour.contentHashCode()
        result = 31 * result + paymentBuckets.hashCode()
        return result
    }
}

class ReportsRepository(
    private val dao: OrderDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {

    suspend fun loadRange(fromDate: LocalDate, toDate: LocalDate): ReportsRawSnapshot {
        var from = fromDate
        var to = toDate
        if (from > to) {
            val t = from
            from = to
            to = t
        }
        val startMs = from.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMs = to.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val agg = dao.reportCompletedAggregates(startMs, endMs, OrderStatus.COMPLETED)
        val items = dao.reportSumLineQuantities(startMs, endMs, OrderStatus.COMPLETED)
        val payment = dao.reportGroupByPaymentMethod(startMs, endMs, OrderStatus.COMPLETED)
        val orders = dao.listOrdersInRange(startMs, endMs, OrderStatus.COMPLETED)

        val byHour = DoubleArray(24)
        for (o in orders) {
            val hour = Instant.ofEpochMilli(o.createdAtEpochMillis).atZone(zoneId).hour
            byHour[hour] += o.totalAmount
        }

        return ReportsRawSnapshot(
            fromDate = from,
            toDate = to,
            orderCount = agg.orderCount,
            totalRevenue = agg.totalRevenue,
            itemsSold = items,
            revenueByHour = byHour,
            paymentBuckets = payment,
        )
    }
}
