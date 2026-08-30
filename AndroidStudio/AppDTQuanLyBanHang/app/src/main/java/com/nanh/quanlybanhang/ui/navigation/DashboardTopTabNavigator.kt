package com.nanh.quanlybanhang.ui.navigation

/** Điều hướng từ [com.nanh.quanlybanhang.ui.dashboard.xml.DashboardFragment] (tab trên). */
interface DashboardTopTabNavigator {
    /** Tab trên dashboard (lịch sử đơn hàng); giữ bottom nav ở Trang chủ. */
    fun openOrderHistoryFromDashboardTab()

    /** Nút xem lịch sử từ màn Lên đơn; có back stack để quay lại. */
    fun pushOrderHistoryFromPlaceOrder()

    /** Back từ lịch sử mở từ dashboard → về dashboard. */
    fun returnToDashboardFromOrderHistory()

    fun openProductsFromDashboardTab()

    /** Quản lý khách quen — tương tự sản phẩm (có nút back về dashboard). */
    fun openCustomersFromDashboardTab()

    fun openSuppliersFromDashboardTab()

    fun openWarehouseFromDashboardTab()

    fun openReportsFromDashboardTab()
}
