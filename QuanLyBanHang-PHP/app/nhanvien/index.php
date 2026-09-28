<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get all employees ordered by creation date
    $list = db_fetch_all("SELECT * FROM nhanvien ORDER BY ngay_tao DESC");
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('nhanvien/index', [
        'list' => $list,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Quản lý nhân viên',
        'page_css' => ['employees.css']
    ]);
} catch (Exception $e) {
    error_log("Error in nhanvien/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải danh sách nhân viên.');
    redirect('../index.php');
}
?>
