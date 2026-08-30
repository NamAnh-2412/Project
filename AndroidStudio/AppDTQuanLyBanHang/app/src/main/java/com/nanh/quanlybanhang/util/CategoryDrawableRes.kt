package com.nanh.quanlybanhang.util

import androidx.annotation.DrawableRes
import com.nanh.quanlybanhang.R
import kotlin.math.absoluteValue

object CategoryDrawableRes {
    @DrawableRes
    fun forCategoryId(categoryId: Long): Int {
        val bucket = if (categoryId == 0L) 3 else (categoryId.absoluteValue % 4).toInt()
        return when (bucket) {
            0 -> R.drawable.ic_cat_art
            1 -> R.drawable.ic_cat_food
            2 -> R.drawable.ic_cat_fashion
            else -> R.drawable.ic_cat_other
        }
    }
}
