<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get filter parameters
    $q = get('q', '', 'string');
    $rank = get('rank', '', 'string');
    
    // Build query
    $where = "1=1";
    $params = [];
    $types = '';
    
    if (!empty($q)) {
        $where .= " AND (ten_khachhang LIKE ? OR so_dien_thoai LIKE ?)";
        $params[] = '%' . $q . '%';
        $params[] = '%' . $q . '%';
        $types .= 'ss';
    }
    
    if (!empty($rank) && in_array($rank, ['thuong', 'bac', 'vang', 'vip'])) {
        $where .= " AND loai_thanh_vien = ?";
        $params[] = $rank;
        $types .= 's';
    }
    
    // Get all customers ordered by creation date or total spending
    $list = db_fetch_all("SELECT * FROM khachhang WHERE $where ORDER BY tong_tien_da_chi DESC, ngay_tao DESC", $types, $params);
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('khachhang/index', [
        'list' => $list,
        'q' => $q,
        'rank' => $rank,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Quản lý khách hàng',
        'page_css' => ['customers.css']
    ]);
} catch (Exception $e) {
    error_log("Error in khachhang/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải danh sách khách hàng.');
    redirect('../index.php');
}
?>
