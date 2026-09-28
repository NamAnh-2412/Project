<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Load categories (only menu categories, not choice groups)
    $cats = db_fetch_all(
        "SELECT id, ten_danhmuc FROM danhmuc WHERE (mota IS NULL OR mota != ? OR mota = '') ORDER BY thu_tu, ten_danhmuc",
        's',
        ['nhóm lựa chọn']
    );
    
    // Get active category
    $activeCat = get('cat', 0, 'int');
    if ($activeCat === 0 && !empty($cats)) {
        $activeCat = (int)$cats[0]['id'];
    }
    
    // Load products for active category
    $prod = [];
    if ($activeCat > 0) {
        $prod = db_fetch_all(
            "SELECT * FROM sanpham WHERE danhmuc_id = ? AND dang_hoat_dong = 1 ORDER BY ten_sanpham",
            'i',
            [$activeCat]
        );
    }
    
    $notice = '';
    
    // Xử lý tạo nhiều đơn cùng lúc
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'create_multiple_orders') {
        $ordersJson = post('orders', '[]', 'raw');
        $orders = json_decode($ordersJson, true);
        
        if (!$orders || !is_array($orders) || count($orders) === 0) {
            jsonResponse(['success' => false, 'message' => 'Không có đơn hàng nào'], 400);
        }
        
        $result = createMultipleOrders($orders);
        $statusCode = $result['success'] ? 200 : 400;
        
        jsonResponse([
            'success' => $result['success'],
            'message' => $result['message'],
            'count' => $result['count'],
            'errors' => $result['errors'],
        ], $statusCode);
    }
    
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'save_pending_order') {
        $itemsJson = post('items', '[]', 'raw');
        $items = json_decode($itemsJson, true);

        $result = createPendingOrder([
            'ten_khachhang' => post('ten_khachhang', 'Khách lẻ'),
            'sdt_khachhang' => post('sdt_khachhang'),
            'items' => $items,
            'unique_code' => false,
            'use_transaction' => true,
        ]);

        if ($result['success']) {
            setFlashMessage('success', 'Đã lưu đơn chờ xử lý!');
            redirect('index.php');
        }

        $notice = $result['error'] === 'Vui lòng chọn sản phẩm.'
            ? 'Vui lòng chọn sản phẩm trước khi lưu đơn.'
            : 'Không thể lưu đơn, vui lòng thử lại!';
    }

    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'create_order') {
        $itemsJson = post('items', '[]', 'raw');
        $items = json_decode($itemsJson, true);
        
        $result = createOrder([
            'ten_khachhang' => post('ten_khachhang', 'Khách lẻ'),
            'sdt_khachhang' => post('sdt_khachhang'),
            'items' => $items,
            'phuong_thuc_thanhtoan' => post('phuong_thuc_thanhtoan', PAYMENT_CASH),
            'unique_code' => false,
            'use_transaction' => true,
        ]);
        
        if ($result['success']) {
            setFlashMessage('success', 'Tạo đơn hàng thành công!');
            redirect('index.php');
        }
        
        $notice = $result['error'] === 'Vui lòng chọn sản phẩm.'
            ? 'Vui lòng chọn sản phẩm.'
            : 'Không thể tạo đơn hàng, vui lòng thử lại!';
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('donhang/create', [
        'cats' => $cats,
        'activeCat' => $activeCat,
        'prod' => $prod,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Tạo đơn hàng'
    ], 'none');
} catch (Exception $e) {
    error_log("Error in donhang/create: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php');
}
?>
