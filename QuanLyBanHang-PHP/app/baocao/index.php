<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get tab parameter (tonghop, chitiet, or daily_revenue)
    $tab = get('tab', 'tonghop');
    
    // Get date range parameters (default to current month)
    $ngay_bd = get('ngay_bd', date('Y-m-01'));
    $ngay_kt = get('ngay_kt', date('Y-m-d'));
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    // Get báo cáo tổng hợp (always needed for summary)
    $tonghop_data = db_fetch_all(
        "SELECT 
            DATE(ngay_tao) as ngay,
            SUM(thanh_tien) as tong_tien,
            COUNT(*) as so_don
         FROM donhang 
         WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ?
         GROUP BY DATE(ngay_tao)
         ORDER BY ngay DESC",
        'sss',
        [$ngay_bd, $ngay_kt, ORDER_STATUS_COMPLETED]
    );
    
    // Get báo cáo chi tiết (if tab = chitiet)
    $chitiet_data = [];
    $has_app_online = false; // Initialize for view
    
    if ($tab === 'chitiet') {
        // Check if app_online exists in enum (for backward compatibility)
        global $conn;
        $result = $conn->query("SHOW COLUMNS FROM donhang WHERE Field = 'phuong_thuc_thanhtoan'");
        if ($result && $row = $result->fetch_assoc()) {
            $enum_values = $row['Type'];
            if (strpos($enum_values, 'app_online') !== false) {
                $has_app_online = true;
            }
        }
        
        $sql = "SELECT 
                DATE(ngay_tao) as ngay,
                SUM(CASE WHEN phuong_thuc_thanhtoan = 'tien_mat' THEN thanh_tien ELSE 0 END) as tien_mat,
                SUM(CASE WHEN phuong_thuc_thanhtoan = 'chuyen_khoan' THEN thanh_tien ELSE 0 END) as chuyen_khoan,
                SUM(CASE WHEN phuong_thuc_thanhtoan = 'the' THEN thanh_tien ELSE 0 END) as the";
        
        if ($has_app_online) {
            $sql .= ",
                SUM(CASE WHEN phuong_thuc_thanhtoan = 'app_online' THEN thanh_tien ELSE 0 END) as app_online";
        }
        
        $sql .= ",
                SUM(thanh_tien) as tong_tien,
                COUNT(*) as so_don
             FROM donhang
             WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ?
             GROUP BY DATE(ngay_tao)
             ORDER BY ngay DESC";
        
        $chitiet_data = db_fetch_all($sql, 'sss', [$ngay_bd, $ngay_kt, ORDER_STATUS_COMPLETED]);
    }
    
    // Get biểu đồ doanh thu theo giờ
    $hourly_data = db_fetch_all(
        "SELECT 
            HOUR(ngay_tao) as gio,
            SUM(thanh_tien) as doanh_thu,
            COUNT(*) as so_don
         FROM donhang
         WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ?
         GROUP BY HOUR(ngay_tao)
         ORDER BY gio ASC",
        'sss',
        [$ngay_bd, $ngay_kt, ORDER_STATUS_COMPLETED]
    );
    
    // Initialize hourly chart data (24 hours: 0-23)
    $hourly_chart = [];
    $hourly_values = array_fill(0, 24, 0);
    
    foreach ($hourly_data as $row) {
        $gio = (int)$row['gio'];
        if ($gio >= 0 && $gio <= 23) {
            $hourly_values[$gio] = (float)$row['doanh_thu'];
        }
    }
    
    // Calculate chart bar heights (similar to dashboard)
    $maxValue = max($hourly_values);
    if ($maxValue == 0) {
        $maxValue = 1;
    }
    
    $maxHeight = 200;
    $minHeight = 30;
    
    for ($i = 0; $i < 24; $i++) {
        $value = $hourly_values[$i];
        if ($maxValue > 0) {
            $height = $minHeight + (($value / $maxValue) * ($maxHeight - $minHeight));
        } else {
            $height = $minHeight;
        }
        $height = round($height);
        
        $hourly_chart[] = [
            'label' => $i . 'h',
            'value' => $value,
            'height' => $height
        ];
    }
    
    // Get daily_revenue data (if tab = daily_revenue)
    $daily_revenue_data = [];
    if ($tab === 'daily_revenue') {
        $daily_revenue_data = db_fetch_all(
            "SELECT day_date, total_amount, order_count, updated_at 
             FROM daily_revenue 
             WHERE day_date BETWEEN ? AND ?
             ORDER BY day_date DESC",
            'ss',
            [$ngay_bd, $ngay_kt]
        );
    }
    
    // Calculate summary statistics
    $tong_doanh_thu = 0;
    $tong_so_don = 0;
    foreach ($tonghop_data as $row) {
        $tong_doanh_thu += (float)$row['tong_tien'];
        $tong_so_don += (int)$row['so_don'];
    }
    
    renderView('baocao/index', [
        'tab' => $tab,
        'tonghop_data' => $tonghop_data,
        'chitiet_data' => $chitiet_data,
        'has_app_online' => $has_app_online, // Pass to view
        'hourly_chart' => $hourly_chart,
        'tong_doanh_thu' => $tong_doanh_thu,
        'tong_so_don' => $tong_so_don,
        'daily_revenue_data' => $daily_revenue_data,
        'ngay_bd' => $ngay_bd,
        'ngay_kt' => $ngay_kt,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Báo cáo doanh thu',
        'page_css' => ['reports.css', 'dashboard.css']
    ]);
} catch (Exception $e) {
    error_log("Error in baocao/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải báo cáo.');
    redirect('../index.php');
}
?>
