package com.nanh.quanlybanhang.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CustomerEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderLineEntity::class,
        OptionGroupEntity::class,
        OptionItemEntity::class,
        ProductOptionGroupEntity::class,
        ShopProfileEntity::class,
        AuthUserEntity::class,
        SupplierEntity::class,
        StockReceiptEntity::class,
        StockReceiptLineEntity::class,
        StockCountReportEntity::class,
        StockCountReportLineEntity::class,
    ],
    version = 14,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun catalogDao(): CatalogDao
    abstract fun shopProfileDao(): ShopProfileDao
    abstract fun authDao(): AuthDao
    abstract fun supplierDao(): SupplierDao
    abstract fun stockReceiptDao(): StockReceiptDao
    abstract fun stockCountReportDao(): StockCountReportDao

    companion object {
        private const val NAME = "quanlybanhang.db"

        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                .addMigrations(
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN scope INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_scope` ON `products` (`scope`)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_count_reports` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL,
                        `note` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stock_count_reports_createdAtEpochMillis` ON `stock_count_reports` (`createdAtEpochMillis`)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_count_report_lines` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `reportId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `systemQuantityBefore` INTEGER NOT NULL,
                        `countedQuantity` INTEGER NOT NULL,
                        FOREIGN KEY(`reportId`) REFERENCES `stock_count_reports`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stock_count_report_lines_reportId` ON `stock_count_report_lines` (`reportId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stock_count_report_lines_productId` ON `stock_count_report_lines` (`productId`)",
                )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_receipts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `supplierId` INTEGER NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL,
                        `note` TEXT,
                        FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_receipts_supplierId` ON `stock_receipts` (`supplierId`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stock_receipts_createdAtEpochMillis` ON `stock_receipts` (`createdAtEpochMillis`)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_receipt_lines` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `receiptId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        FOREIGN KEY(`receiptId`) REFERENCES `stock_receipts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_receipt_lines_receiptId` ON `stock_receipt_lines` (`receiptId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_receipt_lines_productId` ON `stock_receipt_lines` (`productId`)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `suppliers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT,
                        `address` TEXT,
                        `note` TEXT,
                        `createdAtEpochMillis` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_suppliers_name` ON `suppliers` (`name`)")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `auth_users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `passwordHashHex` TEXT NOT NULL,
                        `saltHex` TEXT NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_auth_users_username` ON `auth_users` (`username`)",
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shop_profile` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `avatarLocalPath` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO shop_profile (id, name, avatarLocalPath)
                    VALUES (1, 'Tinh Tú Shop', NULL)
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN paymentMethodIndex INTEGER")
                db.execSQL("ALTER TABLE orders ADD COLUMN customerPayAmount REAL")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE order_lines ADD COLUMN lineNote TEXT")
            }
        }

        /** Giữ dữ liệu khi nâng 5→6 (liên kết sản phẩm ↔ nhóm lựa chọn). */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `product_option_groups` (
                        `productId` INTEGER NOT NULL,
                        `optionGroupId` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`productId`, `optionGroupId`),
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`optionGroupId`) REFERENCES `option_groups`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_product_option_groups_optionGroupId` " +
                        "ON `product_option_groups` (`optionGroupId`)",
                )
            }
        }
    }
}
