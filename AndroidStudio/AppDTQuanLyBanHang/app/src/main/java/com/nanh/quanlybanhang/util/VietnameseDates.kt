package com.nanh.quanlybanhang.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object VietnameseDates {
    fun todayHeading(): String {
        val locale = Locale.forLanguageTag("vi-VN")
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'tháng' M, yyyy", locale)
        return LocalDate.now().format(formatter)
    }

    fun formatOrderDateTime(epochMillis: Long): String {
        val locale = Locale.forLanguageTag("vi-VN")
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale).format(zdt)
    }
}
