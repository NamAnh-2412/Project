package com.nanh.quanlybanhang.util

import kotlin.math.abs
import kotlin.math.roundToInt

/** Hiển thị đầy đủ phần nguyên VNĐ với dấu phân cách (vd 28.000) — dùng màn thanh toán. */
fun formatVndGrouped(amount: Double): String {
    val n = kotlin.math.round(amount).toLong().coerceAtLeast(0)
    return java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("vi-VN")).format(n)
}

fun formatVndCompact(amount: Double): String {
    val v = abs(amount)
    return when {
        v >= 1_000_000 -> trimDecimal(v / 1_000_000.0) + "M"
        v >= 1_000 -> "${(v / 1_000.0).roundToInt()}K"
        else -> amount.roundToInt().toString()
    }
}

private fun trimDecimal(x: Double): String {
    val s = String.format(java.util.Locale.US, "%.1f", x)
    return s.trimEnd('0').trimEnd('.').ifEmpty { "0" }
}
