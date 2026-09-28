<?php
require_once '../core/bootstrap.php';

requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

try {
    $customer = db_fetch_one("SELECT * FROM khachhang WHERE id = ?", 'i', [$id]);
    if (!$customer) {
        setFlashMessage('error', 'Không tìm thấy khách hàng.');
        redirect('index.php');
    }

    $orders = db_fetch_all(
        "SELECT id, ma_donhang, thanh_tien, trang_thai, ngay_tao, phuong_thuc_thanhtoan
         FROM donhang
         WHERE khachhang_id = ?
         ORDER BY ngay_tao DESC
         LIMIT 10",
        'i',
        [$id]
    );

    $statsRow = db_fetch_one(
        "SELECT 
            COUNT(*) AS total_orders,
            COALESCE(SUM(CASE WHEN trang_thai NOT IN ('da_huy') THEN thanh_tien ELSE 0 END), 0) AS total_spent,
            COALESCE(AVG(CASE WHEN trang_thai NOT IN ('da_huy') THEN thanh_tien END), 0) AS avg_order
         FROM donhang
         WHERE khachhang_id = ?",
        'i',
        [$id]
    );

    $stats = [
        'total_orders' => (int)($statsRow['total_orders'] ?? 0),
        'total_spent' => (float)($customer['tong_tien_da_chi'] ?? $statsRow['total_spent'] ?? 0),
        'avg_order' => (float)($statsRow['avg_order'] ?? 0),
    ];

    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));

    renderView('khachhang/view', [
        'customer' => $customer,
        'orders' => $orders,
        'stats' => $stats,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Chi tiết khách hàng',
        'page_css' => ['customers.css']
    ]);
} catch (Exception $e) {
    error_log("Error in khachhang/view: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải chi tiết khách hàng.');
    redirect('index.php');
}
?>
