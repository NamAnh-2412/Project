<?php
// Load bootstrap first (initApi() is defined in helpers.php after bootstrap)
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication
requireApiAuth();

try {
    $today = date('Y-m-d');

    // 1. Lấy tổng đơn hàng
    $dh = db_fetch_one(
        "SELECT COUNT(*) AS tong FROM donhang WHERE DATE(ngay_tao) = ? AND trang_thai = ?",
        'ss',
        [$today, ORDER_STATUS_COMPLETED]
    );
    $dh_today = (int)($dh['tong'] ?? 0);
    // 2. Lấy khách hàng (Logic cũ: Khách = Đơn)
    $kh_today = $dh_today; 

    // 3. Lấy doanh thu
    $dt = db_fetch_one(
        "SELECT SUM(thanh_tien) AS sum FROM donhang WHERE DATE(ngay_tao) = ? AND trang_thai = ?",
        'ss',
        [$today, ORDER_STATUS_COMPLETED]
    );
    $dt_today = (float)($dt['sum'] ?? 0);
    
    // Nếu đang test rand() ở trên thì cũng fake luôn doanh thu cho đồng bộ (Tùy chọn)
    // if ($dh_today > 10) $dt_today = $dh_today * 50000; 

    // Return JSON response
    jsonResponse([
        'status' => 'success',
        'orders' => $dh_today,
        'customers' => $kh_today,
        'revenue' => number_format($dt_today, 0, ',', '.') . 'đ'
    ]);

} catch (Exception $e) {
    error_log("Error in dashboard_stats: " . $e->getMessage());
    jsonResponse(['status' => 'error', 'message' => $e->getMessage()], 500);
}
?>

