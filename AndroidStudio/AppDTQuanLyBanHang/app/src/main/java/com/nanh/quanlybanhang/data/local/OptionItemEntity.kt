package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "option_items",
    foreignKeys = [
        ForeignKey(
            entity = OptionGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionGroupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["optionGroupId"])],
)
data class OptionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val optionGroupId: Long,
    val name: String,
    /** Phụ phí khi chọn lựa chọn này (VNĐ). */
    val extraPrice: Double,
)
