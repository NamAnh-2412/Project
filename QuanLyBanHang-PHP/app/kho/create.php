<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get all materials from inventory
    $list = db_fetch_all("SELECT * FROM nguyenvatlieu ORDER BY ten_nguyenlieu");
    
    // Get suppliers for dropdown
    $suppliers = db_fetch_all("SELECT id, ten_nhacungcap FROM nhacungcap WHERE trang_thai = 'dang_hop_tac' ORDER BY ten_nhacungcap");
    
    // Get current cart from session
    $cart = $_SESSION['kho_cart'] ?? [];
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('kho/create', [
        'list' => $list,
        'suppliers' => $suppliers,
        'cart' => $cart,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Tạo phiếu nhập kho',
        'page_css' => ['inventory.css'],
        'page_js' => ['inventory/create.js']
    ]);
} catch (Exception $e) {
    error_log("Error in kho/create: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang tạo phiếu nhập kho.');
    redirect('index.php');
}
?>

