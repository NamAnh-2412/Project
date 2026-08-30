package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["scope"]),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** VND đơn giá */
    val unitPrice: Double,
    val categoryId: Long,
    /**
     * [ProductScope.SALE] — món bán (màn Sản phẩm / lên đơn).
     * [ProductScope.WAREHOUSE] — chỉ kho (tab Kho), không xuất hiện khi lên đơn.
     */
    val scope: Int = ProductScope.SALE,
    /** Số lượng tồn kho (nhập nhanh hoặc qua phiếu nhập). */
    val stockQuantity: Int = 0,
    /** Đường dẫn file ảnh trong bộ nhớ nội bộ app (copy từ thư viện ảnh). */
    val imageLocalPath: String? = null,
)
