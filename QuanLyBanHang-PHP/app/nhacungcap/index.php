<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get filter parameters
    $kw = get('kw');
    $status = get('status');
    
    // Build query with filters
    $where = "1=1";
    $params = [];
    $types = '';
    
    if (!empty($kw)) {
        $where .= " AND (ten_nhacungcap LIKE ? OR so_dien_thoai LIKE ? OR email LIKE ?)";
        $params[] = '%' . $kw . '%';
        $params[] = '%' . $kw . '%';
        $params[] = '%' . $kw . '%';
        $types .= 'sss';
    }
    
    if (!empty($status)) {
        $where .= " AND trang_thai = ?";
        $params[] = $status;
        $types .= 's';
    }
    
    // Get all suppliers ordered by creation date
    $list = db_fetch_all(
        "SELECT * FROM nhacungcap WHERE $where ORDER BY ngay_tao DESC",
        $types,
        $params
    );
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('nhacungcap/index', [
        'list' => $list,
        'kw' => $kw,
        'status' => $status,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Quản lý nhà cung cấp',
        'page_css' => ['customers.css']
    ]);
} catch (Exception $e) {
    error_log("Error in nhacungcap/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải danh sách nhà cung cấp.');
    redirect('../index.php');
}
?>

