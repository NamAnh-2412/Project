<?php
require_once 'app/core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get today's date
    $today = date('Y-m-d');
    
    // Get today's statistics using prepared statements
    $dh = db_fetch_one(
        "SELECT COUNT(*) AS tong FROM donhang WHERE DATE(ngay_tao) = ? AND trang_thai = ?",
        'ss',
        [$today, ORDER_STATUS_COMPLETED]
    );
    $dh_today = (int)($dh['tong'] ?? 0);
    $kh_today = $dh_today;
    
    // Get today's revenue
    $dt = db_fetch_one(
        "SELECT SUM(thanh_tien) AS sum FROM donhang WHERE DATE(ngay_tao) = ? AND trang_thai = ?",
        'ss',
        [$today, ORDER_STATUS_COMPLETED]
    );
    $dt_today = (float)($dt['sum'] ?? 0);
    
    // Get best selling products today
    $sp_best = db_fetch_all(
        "SELECT sp.ten_sanpham, SUM(ctd.so_luong) AS tong_sl 
         FROM donhang dh 
         JOIN chitiet_donhang ctd ON dh.id = ctd.donhang_id 
         JOIN sanpham sp ON ctd.sanpham_id = sp.id 
         WHERE DATE(dh.ngay_tao) = ? AND dh.trang_thai = ? 
         GROUP BY ctd.sanpham_id 
         ORDER BY tong_sl DESC 
         LIMIT 3",
        'ss',
        [$today, ORDER_STATUS_COMPLETED]
    );
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    // Calculate week chart data
    $today_dt = new DateTime();
    $dayOfWeek = (int)$today_dt->format('w'); // 0=CN, 1=T2, ..., 6=T7
    $mondayOffset = ($dayOfWeek == 0) ? 6 : ($dayOfWeek - 1);
    $monday = clone $today_dt;
    $monday->modify("-{$mondayOffset} days");
    
    $weekStart = $monday->format('Y-m-d');
    $sunday = clone $monday;
    $sunday->modify('+6 days');
    $weekEnd = $sunday->format('Y-m-d');
    
    // Query week data using prepared statements
    $weekDataRows = db_fetch_all(
        "SELECT DATE(ngay_tao) AS ngay, COUNT(*) AS cnt 
         FROM donhang 
         WHERE DATE(ngay_tao) >= ? AND DATE(ngay_tao) <= ? AND trang_thai = ? 
         GROUP BY DATE(ngay_tao)",
        'sss',
        [$weekStart, $weekEnd, ORDER_STATUS_COMPLETED]
    );
    
    // Initialize bars array
    $bars = [0, 0, 0, 0, 0, 0, 0];
    $labels = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
    
    // Fill data from database
    foreach ($weekDataRows as $row) {
        $date = new DateTime($row['ngay']);
        $dayIndex = (int)$date->format('w');
        $arrayIndex = ($dayIndex == 0) ? 6 : ($dayIndex - 1);
        $bars[$arrayIndex] = (int)$row['cnt'];
    }
    
    // Calculate chart bar heights
    $maxValue = max($bars);
    if ($maxValue == 0) {
        $maxValue = 1;
    }
    
    $maxHeight = 200;
    $minHeight = 30;
    $chart_bars = [];
    
    foreach ($bars as $i => $v) {
        if ($maxValue > 0) {
            $height = $minHeight + (($v / $maxValue) * ($maxHeight - $minHeight));
        } else {
            $height = $minHeight;
        }
        $height = round($height);
        
        $chart_bars[] = [
            'label' => $labels[$i],
            'value' => $v,
            'height' => $height
        ];
    }
    
    // Render view
    renderView('bangdieukhien', [
        'dh_today' => $dh_today,
        'kh_today' => $kh_today,
        'dt_today' => $dt_today,
        'sp_best' => $sp_best,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'chart_bars' => $chart_bars,
        'page_title' => 'Trang Quản Lý Tổng',
        'page_css' => ['dashboard.css'],
        'show_header' => false  // View tự quản lý header
    ]);
} catch (Exception $e) {
    error_log("Error in index.php: " . $e->getMessage());
    die('Có lỗi xảy ra khi tải trang chủ. Vui lòng thử lại sau.');
}
?>
