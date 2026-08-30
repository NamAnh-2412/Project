package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("UPDATE categories SET name = :name WHERE id = :id")
    suspend fun updateCategoryName(id: Long, name: String)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT COUNT(*) FROM products WHERE categoryId = :categoryId")
    suspend fun productCountForCategory(categoryId: Long): Int

    @Query("SELECT * FROM option_groups ORDER BY name COLLATE NOCASE ASC")
    fun observeOptionGroups(): Flow<List<OptionGroupEntity>>

    @Insert
    suspend fun insertOptionGroup(group: OptionGroupEntity): Long

    @Query("SELECT * FROM option_groups WHERE id = :id LIMIT 1")
    fun observeOptionGroupById(id: Long): Flow<OptionGroupEntity?>

    @Query("UPDATE option_groups SET name = :name WHERE id = :id")
    suspend fun updateOptionGroupName(id: Long, name: String)

    @Query("DELETE FROM option_groups WHERE id = :id")
    suspend fun deleteOptionGroupById(id: Long)

    @Query("SELECT * FROM option_items WHERE optionGroupId = :groupId ORDER BY name COLLATE NOCASE ASC")
    fun observeOptionItemsForGroup(groupId: Long): Flow<List<OptionItemEntity>>

    @Insert
    suspend fun insertOptionItem(item: OptionItemEntity): Long

    @Query(
        """
        UPDATE option_items SET name = :name, extraPrice = :extraPrice WHERE id = :id
        """,
    )
    suspend fun updateOptionItem(id: Long, name: String, extraPrice: Double)

    @Query("DELETE FROM option_items WHERE id = :id")
    suspend fun deleteOptionItemById(id: Long)

    @Query("SELECT * FROM option_items")
    fun observeOptionItemsAll(): Flow<List<OptionItemEntity>>

    @Query("SELECT * FROM product_option_groups ORDER BY productId ASC, sortOrder ASC")
    fun observeProductOptionLinks(): Flow<List<ProductOptionGroupEntity>>

    @Insert
    suspend fun insertProductOptionGroup(link: ProductOptionGroupEntity)

    @Query("DELETE FROM product_option_groups WHERE productId = :productId")
    suspend fun deleteProductOptionGroupsForProduct(productId: Long)

    @Transaction
    suspend fun replaceProductOptionGroups(productId: Long, groupIds: List<Long>) {
        deleteProductOptionGroupsForProduct(productId)
        groupIds.distinct().forEachIndexed { index, gid ->
            insertProductOptionGroup(
                ProductOptionGroupEntity(productId = productId, optionGroupId = gid, sortOrder = index),
            )
        }
    }

    @Query("SELECT * FROM products WHERE scope = :scope ORDER BY name COLLATE NOCASE ASC")
    fun observeProductsByScope(scope: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert
    suspend fun insertProduct(product: ProductEntity): Long

    @Query(
        """
        UPDATE products SET name = :name, unitPrice = :unitPrice, categoryId = :categoryId,
        imageLocalPath = :imageLocalPath WHERE id = :id
        """,
    )
    suspend fun updateProduct(
        id: Long,
        name: String,
        unitPrice: Double,
        categoryId: Long,
        imageLocalPath: String?,
    )

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :delta WHERE id = :productId")
    suspend fun adjustProductStock(productId: Long, delta: Int)

    @Query("UPDATE products SET stockQuantity = :quantity WHERE id = :productId")
    suspend fun setProductStock(productId: Long, quantity: Int)

    @Query("SELECT COUNT(*) FROM order_lines WHERE productId = :productId")
    suspend fun orderLineCountForProduct(productId: Long): Int
}
