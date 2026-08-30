package com.nanh.quanlybanhang.util

import java.util.Locale

fun shopDisplayInitials(shopName: String): String {
    val t = shopName.trim()
    if (t.isEmpty()) return "?"
    val parts = t.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> {
            val a = parts[0].first().toString().uppercase(Locale.getDefault())
            val b = parts[1].first().toString().uppercase(Locale.getDefault())
            "$a$b"
        }
        else -> t.take(2).uppercase(Locale.getDefault())
    }
}
