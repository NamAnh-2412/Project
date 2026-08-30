package com.nanh.quanlybanhang.data.repo

import com.nanh.quanlybanhang.data.local.ShopProfileDao
import com.nanh.quanlybanhang.data.local.ShopProfileEntity
import com.nanh.quanlybanhang.util.ProductImageStorage
import kotlinx.coroutines.flow.Flow

class ShopProfileRepository(
    private val dao: ShopProfileDao,
) {

    fun observeProfile(): Flow<ShopProfileEntity?> = dao.observeShopProfile()

    suspend fun ensureDefaultProfile(defaultName: String) {
        if (dao.getShopProfile() == null) {
            dao.upsertShopProfile(ShopProfileEntity(name = defaultName))
        }
    }

    suspend fun updateShopName(name: String) {
        val current = dao.getShopProfile() ?: ShopProfileEntity(name = name)
        dao.upsertShopProfile(current.copy(name = name))
    }

    suspend fun replaceAvatarKeepingOldCleanup(oldPath: String?, newPath: String) {
        val current = dao.getShopProfile() ?: return
        val old = oldPath?.trim()?.takeIf { it.isNotEmpty() }
        if (old != null && old != newPath.trim()) {
            ProductImageStorage.deleteIfExists(old)
        }
        dao.upsertShopProfile(current.copy(avatarLocalPath = newPath.trim()))
    }

    suspend fun clearAvatar() {
        val current = dao.getShopProfile() ?: return
        ProductImageStorage.deleteIfExists(current.avatarLocalPath)
        dao.upsertShopProfile(current.copy(avatarLocalPath = null))
    }
}
