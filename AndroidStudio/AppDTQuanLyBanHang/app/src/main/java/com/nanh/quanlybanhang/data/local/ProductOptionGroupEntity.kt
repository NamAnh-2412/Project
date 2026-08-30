package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product_option_groups",
    primaryKeys = ["productId", "optionGroupId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = OptionGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionGroupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["optionGroupId"])],
)
data class ProductOptionGroupEntity(
    val productId: Long,
    val optionGroupId: Long,
    val sortOrder: Int = 0,
)
