package com.nanh.quanlybanhang.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

object ProductImageStorage {
    private const val DIR_NAME = "product_images"

    fun directory(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { mkdirs() }

    suspend fun copyFromPickerUri(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri)
            val ext = guessExtension(mime)
            val dest = File(directory(context), "${UUID.randomUUID()}.$ext")
            resolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("Không đọc được ảnh đã chọn.")
            dest.absolutePath
        }

    fun deleteIfExists(path: String?) {
        val p = path?.trim().orEmpty()
        if (p.isEmpty()) return
        runCatching {
            File(p).takeIf { it.exists() }?.delete()
        }
    }

    private fun guessExtension(mime: String?): String =
        when {
            mime == null -> "jpg"
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
}

fun ImageView.setProductThumbnail(localPath: String?, categoryId: Long, maxSidePx: Int = 384) {
    val path = localPath?.trim()?.takeIf { it.isNotEmpty() }
    val file = path?.let { File(it) }
    if (file != null && file.isFile && file.canRead()) {
        val bmp = decodeScaledBitmap(file.absolutePath, maxSidePx)
        if (bmp != null) {
            setImageBitmap(bmp)
            scaleType = ImageView.ScaleType.CENTER_CROP
            return
        }
    }
    scaleType = ImageView.ScaleType.CENTER_INSIDE
    setImageResource(CategoryDrawableRes.forCategoryId(categoryId))
}

private fun decodeScaledBitmap(path: String, maxSidePx: Int): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxSidePx || bounds.outHeight / sample > maxSidePx) {
        sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, opts)
}

/** Hiển thị ảnh cửa hàng hoặc chữ viết tắt trên [initialsView]. */
fun bindShopAvatarBadge(
    imageView: ImageView,
    initialsView: TextView,
    avatarPath: String?,
    shopName: String,
    maxSidePx: Int = 384,
) {
    val path = avatarPath?.trim()?.takeIf { it.isNotEmpty() }
    val file = path?.let { File(it) }
    val bmp = if (file != null && file.isFile && file.canRead()) {
        decodeScaledBitmap(file.absolutePath, maxSidePx)
    } else {
        null
    }
    if (bmp != null) {
        imageView.setImageBitmap(bmp)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.visibility = View.VISIBLE
        initialsView.visibility = View.GONE
        imageView.post {
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            imageView.clipToOutline = true
        }
    } else {
        imageView.clipToOutline = false
        imageView.outlineProvider = null
        imageView.setImageDrawable(null)
        imageView.visibility = View.GONE
        initialsView.visibility = View.VISIBLE
        initialsView.text = shopDisplayInitials(shopName)
    }
}
