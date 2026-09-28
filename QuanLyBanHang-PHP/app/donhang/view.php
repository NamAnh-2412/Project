<?php
require_once '../core/bootstrap.php';

requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

try {
    $detail = getOrderDetail($id);
    if (!$detail) {
        setFlashMessage('error', 'Không tìm thấy đơn hàng.');
        redirect('index.php');
    }

    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));

    renderView('donhang/view', [
        'order' => $detail['order'],
        'items' => $detail['items'],
        'total_qty' => $detail['total_qty'],
        'daily_sequence' => $detail['daily_sequence'],
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Chi tiết đơn hàng',
        'page_css' => ['orders.css']
    ], 'none');
} catch (Exception $e) {
    error_log("Error in donhang/view: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải chi tiết đơn hàng.');
    redirect('index.php');
}
?>
