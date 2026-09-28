<?php
require_once '../core/bootstrap.php';

requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    setFlashMessage('error', 'Không tìm thấy đơn hàng.');
    redirect('index.php');
}

$order = db_fetch_one("SELECT * FROM donhang WHERE id = ?", 'i', [$id]);
if (!$order) {
    setFlashMessage('error', 'Không tìm thấy đơn hàng.');
    redirect('index.php');
}

if ($order['trang_thai'] === ORDER_STATUS_CANCELLED) {
    setFlashMessage('error', 'Đơn hàng đã bị hủy, không thể thanh toán.');
    redirect('view.php?id=' . $id);
}

if ($order['trang_thai'] === ORDER_STATUS_COMPLETED) {
    setFlashMessage('info', 'Đơn hàng này đã được thanh toán.');
    redirect('view.php?id=' . $id);
}

if (!canCompleteOrder($order['trang_thai'])) {
    setFlashMessage('error', 'Đơn hàng không thể thanh toán ở trạng thái hiện tại.');
    redirect('view.php?id=' . $id);
}

$notice = '';
$noticeType = 'error';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('confirm') === '1') {
    $paymentMethod = post('phuong_thuc_thanhtoan', PAYMENT_CASH);
    $result = completePendingOrder($id, $paymentMethod);

    if ($result['success']) {
        setFlashMessage('success', 'Thanh toán thành công! Đơn hàng đã hoàn thành.');
        redirect('view.php?id=' . $id);
    }

    $notice = $result['error'] ?? 'Không thể thanh toán đơn hàng.';
    $noticeType = 'error';
}

$detail = getOrderDetail($id);
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

$defaultPayment = getSetting('default_payment_method', PAYMENT_CASH);
if (!array_key_exists($defaultPayment, getPaymentMethodOptions())) {
    $defaultPayment = PAYMENT_CASH;
}

renderView('donhang/pay', [
    'order' => $order,
    'items' => $detail['items'] ?? [],
    'total_qty' => $detail['total_qty'] ?? 0,
    'notice' => $notice,
    'noticeType' => $noticeType,
    'defaultPayment' => $defaultPayment,
    'paymentOptions' => getPaymentMethodOptions(),
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Thanh toán đơn hàng',
    'page_css' => ['ui.css', 'orders.css']
]);
?>
