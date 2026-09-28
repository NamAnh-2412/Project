<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    setFlashMessage('error', 'Không tìm thấy đơn hàng.');
    redirect('index.php');
}

// Get order data
$order = db_fetch_one("SELECT * FROM donhang WHERE id = ?", 'i', [$id]);
if (!$order) {
    setFlashMessage('error', 'Không tìm thấy đơn hàng.');
    redirect('index.php');
}

// Check if order is already cancelled
if ($order['trang_thai'] === ORDER_STATUS_CANCELLED) {
    setFlashMessage('error', 'Đơn hàng này đã được hủy trước đó.');
    redirect('index.php');
}

// Handle cancellation
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
    try {
        global $conn;
        
        // Start transaction
        $conn->begin_transaction();
        
        // Update order status to cancelled
        $updateOrder = db_query(
            "UPDATE donhang SET trang_thai = ? WHERE id = ?",
            'si',
            [ORDER_STATUS_CANCELLED, $id]
        );
        
        if (!$updateOrder) {
            throw new Exception('Không thể cập nhật trạng thái đơn hàng.');
        }
        
        // Trừ số đơn và tổng chi tiêu khi hủy đơn đã hoàn thành
        if (!empty($order['khachhang_id']) && $order['trang_thai'] === ORDER_STATUS_COMPLETED) {
            reverseCustomerSpendingOnCancel(
                (int)$order['khachhang_id'],
                (float)($order['thanh_tien'] ?? 0)
            );
        }
        
        // Commit transaction
        $conn->commit();
        
        setFlashMessage('success', 'Hủy đơn hàng thành công!');
        redirect('index.php');
        
    } catch (Exception $e) {
        $conn->rollback();
        error_log("Error cancelling order: " . $e->getMessage());
        setFlashMessage('error', 'Có lỗi xảy ra khi hủy đơn hàng: ' . $e->getMessage());
    }
}

// Get customer info if exists
$customer = null;
if (!empty($order['khachhang_id'])) {
    $customer = db_fetch_one(
        "SELECT id, ten_khachhang, so_dien_thoai FROM khachhang WHERE id = ?",
        'i',
        [$order['khachhang_id']]
    );
}

$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('donhang/cancel', [
    'order' => $order,
    'customer' => $customer,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Hủy đơn hàng',
    'page_css' => ['ui.css', 'orders.css']
]);
?>

