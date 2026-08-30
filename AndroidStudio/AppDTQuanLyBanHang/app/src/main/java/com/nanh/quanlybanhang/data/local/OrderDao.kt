package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_lines")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Query("SELECT * FROM customers")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    suspend fun listCustomersSorted(): List<CustomerEntity>

    @Insert
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE orders SET customerId = NULL WHERE customerId = :customerId")
    suspend fun clearOrdersCustomerId(customerId: Long)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerRow(customerId: Long)

    @Transaction
    suspend fun deleteCustomerById(customerId: Long) {
        clearOrdersCustomerId(customerId)
        deleteCustomerRow(customerId)
    }

    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertLines(lines: List<OrderLineEntity>)

    @Transaction
    suspend fun insertOrderWithLines(order: OrderEntity, lines: List<OrderLineEntity>): Long {
        val orderId = insertOrder(order)
        insertLines(lines.map { line -> line.copy(orderId = orderId) })
        return orderId
    }

    @Query(
        """
        SELECT COUNT(*) AS orderCount, COALESCE(SUM(totalAmount), 0.0) AS totalRevenue
        FROM orders
        WHERE status = :status AND createdAtEpochMillis >= :startMs AND createdAtEpochMillis < :endMs
        """,
    )
    suspend fun reportCompletedAggregates(startMs: Long, endMs: Long, status: String): CompletedOrderStatsRow

    @Query(
        """
        SELECT COALESCE(SUM(quantity), 0) FROM order_lines
        WHERE orderId IN (
            SELECT id FROM orders
            WHERE status = :status AND createdAtEpochMillis >= :startMs AND createdAtEpochMillis < :endMs
        )
        """,
    )
    suspend fun reportSumLineQuantities(startMs: Long, endMs: Long, status: String): Int

    @Query(
        """
        SELECT paymentMethodIndex AS paymentMethodIndex, COUNT(*) AS orderCount,
               COALESCE(SUM(totalAmount), 0.0) AS revenue
        FROM orders
        WHERE status = :status AND createdAtEpochMillis >= :startMs AND createdAtEpochMillis < :endMs
        GROUP BY paymentMethodIndex
        ORDER BY revenue DESC
        """,
    )
    suspend fun reportGroupByPaymentMethod(
        startMs: Long,
        endMs: Long,
        status: String,
    ): List<PaymentMethodBucketRow>

    @Query(
        """
        SELECT * FROM orders
        WHERE status = :status AND createdAtEpochMillis >= :startMs AND createdAtEpochMillis < :endMs
        """,
    )
    suspend fun listOrdersInRange(startMs: Long, endMs: Long, status: String): List<OrderEntity>
}
