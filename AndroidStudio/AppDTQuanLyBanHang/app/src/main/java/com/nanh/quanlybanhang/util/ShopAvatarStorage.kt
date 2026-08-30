package com.nanh.quanlybanhang.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object ShopAvatarStorage {
    private const val DIR_NAME = "shop_avatar"
    private const val FILE_NAME = "shop_avatar_image"

    private fun baseDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { mkdirs() }

    /**
     * Ghi đè file cố định để không tích lũy nhiều ảnh; [extension] vd: jpg, png.
     */
    suspend fun copyFromPickerUri(context: Context, uri: Uri, extension: String): String =
        withContext(Dispatchers.IO) {
            val dir = baseDir(context)
            dir.listFiles()?.forEach { runCatching { it.delete() } }
            val ext = extension.trim().lowercase().removePrefix(".")
                .ifEmpty { "jpg" }
            val dest = File(dir, "$FILE_NAME.$ext")
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("Không đọc được ảnh đã chọn.")
            dest.absolutePath
        }

    fun guessExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
        return when {
            mime == null -> "jpg"
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
    }
}
