package com.nanh.quanlybanhang

import android.app.Application
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.AppDatabase
import com.nanh.quanlybanhang.data.local.AuthPreferences
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.OrderDao
import com.nanh.quanlybanhang.data.local.ShopProfileDao
import com.nanh.quanlybanhang.data.local.StockCountReportDao
import com.nanh.quanlybanhang.data.local.StockReceiptDao
import com.nanh.quanlybanhang.data.local.SupplierDao
import com.nanh.quanlybanhang.data.repo.AuthRepository
import com.nanh.quanlybanhang.data.repo.DashboardRepository
import com.nanh.quanlybanhang.data.repo.ReportsRepository
import com.nanh.quanlybanhang.data.repo.ShopProfileRepository
import com.nanh.quanlybanhang.data.repo.WarehouseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuanLyBanHangApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database by lazy { AppDatabase.getInstance(this) }

    val orderDao: OrderDao by lazy { database.orderDao() }

    private val authPreferences by lazy { AuthPreferences(this) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authDao = database.authDao(),
            prefs = authPreferences,
            appScope = applicationScope,
        )
    }

    val catalogDao: CatalogDao by lazy { database.catalogDao() }

    val supplierDao: SupplierDao by lazy { database.supplierDao() }

    val stockReceiptDao: StockReceiptDao by lazy { database.stockReceiptDao() }

    val stockCountReportDao: StockCountReportDao by lazy { database.stockCountReportDao() }

    val warehouseRepository: WarehouseRepository by lazy {
        WarehouseRepository(database, catalogDao, stockReceiptDao, stockCountReportDao)
    }

    val shopProfileDao: ShopProfileDao by lazy { database.shopProfileDao() }

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(orderDao = orderDao, catalogDao = catalogDao)
    }

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepository(orderDao)
    }

    val shopProfileRepository: ShopProfileRepository by lazy {
        ShopProfileRepository(shopProfileDao)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            dashboardRepository.seedIfEmpty()
            shopProfileRepository.ensureDefaultProfile(
                defaultName = getString(R.string.app_name),
            )
        }
    }
}
