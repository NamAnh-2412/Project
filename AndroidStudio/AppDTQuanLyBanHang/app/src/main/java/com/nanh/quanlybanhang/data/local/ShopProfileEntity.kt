package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một dòng cố định (id = 1): thông tin hiển thị cửa hàng.
 */
@Entity(tableName = "shop_profile")
data class ShopProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    /** Đường dẫn file ảnh trong bộ nhớ nội bộ app. */
    val avatarLocalPath: String? = null,
)
