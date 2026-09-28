<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Lấy danh sách nguyên vật liệu có thể kiểm kê
    $sql = "SELECT * FROM nguyenvatlieu WHERE trang_thai NOT IN ('ngung_ban', 'ngung_nhap') ORDER BY danhmuc_nl, ten_nguyenlieu";
    $list = db_fetch_all($sql);
    
    // Khởi tạo giỏ hàng kiểm kho nếu chưa có (tự động add tất cả)
    if (!isset($_SESSION['kiemkho_cart']) || empty($_SESSION['kiemkho_cart']) || isset($_GET['reset'])) {
        $_SESSION['kiemkho_cart'] = [];
        foreach ($list as $item) {
            $_SESSION['kiemkho_cart'][$item['id']] = [
                'id' => $item['id'],
                'name' => $item['ten_nguyenlieu'],
                'don_vi' => $item['don_vi_tinh'],
                'ton_he_thong' => (float)($item['so_luong_ton'] ?? 0),
                'ton_thuc_te' => (float)($item['so_luong_ton'] ?? 0),
                'ly_do' => ''
            ];
        }
    }
    $cart = $_SESSION['kiemkho_cart'];
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('kho/create_kiemkho', [
        'list' => $list,
        'cart' => $cart,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Tạo phiếu kiểm kho',
        'page_css' => ['inventory.css']
    ]);
} catch (Exception $e) {
    error_log("Error in kho/create_kiemkho: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang tạo phiếu kiểm kho.');
    redirect('index.php?tab=kiemkho');
}
?>
