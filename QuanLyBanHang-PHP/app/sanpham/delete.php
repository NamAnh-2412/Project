<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $id = get('id', 0, 'int');
    if (!$id) {
        setFlashMessage('error', 'ID sản phẩm không hợp lệ.');
        redirect('index.php');
    }
    
    // Get product data
    $product = db_fetch_one("SELECT * FROM sanpham WHERE id = ?", 'i', [$id]);
    if (!$product) {
        setFlashMessage('error', 'Không tìm thấy sản phẩm.');
        redirect('index.php');
    }
    
    // Check if product is used in orders
    $usedInOrders = db_fetch_one(
        "SELECT COUNT(*) as count FROM chitiet_donhang WHERE sanpham_id = ?",
        'i',
        [$id]
    );
    
    $hasOrders = ($usedInOrders && isset($usedInOrders['count']) && $usedInOrders['count'] > 0);
    
    // Handle deletion
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('confirm') === '1') {
        try {
            // If product is used in orders, we should not delete it, just deactivate
            if ($hasOrders) {
                // Deactivate instead of delete
                $result = db_query(
                    "UPDATE sanpham SET dang_hoat_dong = 0 WHERE id = ?",
                    'i',
                    [$id]
                );
                
                if ($result) {
                    setFlashMessage('success', 'Sản phẩm đã được ngừng bán (không thể xóa vì đã có trong đơn hàng).');
                    redirect('index.php');
                } else {
                    setFlashMessage('error', 'Lỗi khi cập nhật sản phẩm. Vui lòng thử lại.');
                }
            } else {
                // Delete product and its links
                global $conn;
                
                // Delete product choice links
                $conn->query("DELETE FROM sanpham_nhom_lua_chon WHERE sanpham_id = $id");
                
                // Delete product
                $result = db_query("DELETE FROM sanpham WHERE id = ?", 'i', [$id]);
                
                if ($result) {
                    setFlashMessage('success', 'Xóa sản phẩm thành công!');
                    redirect('index.php');
                } else {
                    setFlashMessage('error', 'Lỗi khi xóa sản phẩm. Vui lòng thử lại.');
                }
            }
        } catch (Exception $e) {
            error_log("Error deleting product: " . $e->getMessage());
            setFlashMessage('error', 'Có lỗi xảy ra khi xóa sản phẩm.');
        }
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/delete', [
        'product' => $product,
        'hasOrders' => $hasOrders,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Xóa sản phẩm',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/delete: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php');
}
?>

