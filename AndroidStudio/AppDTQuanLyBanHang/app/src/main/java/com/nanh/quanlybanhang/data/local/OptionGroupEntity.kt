package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Nhóm lựa chọn (vd: size, độ đường). Chi tiết trong [OptionItemEntity]. */
@Entity(tableName = "option_groups")
data class OptionGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
