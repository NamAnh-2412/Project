<?php
require_once '../core/bootstrap.php';

requireLogin();

try {
    $kw = get('kw');
    $from = get('from');
    $to = get('to');
    $payment_method = get('payment_method');
    $status = get('status', '', 'string');

    $where = '1=1';
    $params = [];
    $types = '';

    if (!empty($status)) {
        $validStatuses = array_keys(getOrderStatusFilterOptions());
        $validStatuses = array_filter($validStatuses, function ($key) {
            return $key !== '';
        });
        if (in_array($status, $validStatuses, true)) {
            $where .= ' AND trang_thai = ?';
            $params[] = $status;
            $types .= 's';
        }
    }

    if (!empty($kw)) {
        $where .= ' AND (ma_donhang LIKE ? OR ten_khachhang LIKE ? OR sdt_khachhang LIKE ?)';
        $kwSearch = '%' . $kw . '%';
        $params[] = $kwSearch;
        $params[] = $kwSearch;
        $params[] = $kwSearch;
        $types .= 'sss';
    }

    if (!empty($from)) {
        $where .= ' AND DATE(ngay_tao) >= ?';
        $params[] = $from;
        $types .= 's';
    }

    if (!empty($to)) {
        $where .= ' AND DATE(ngay_tao) <= ?';
        $params[] = $to;
        $types .= 's';
    }

    if (!empty($payment_method)) {
        $where .= ' AND phuong_thuc_thanhtoan = ?';
        $params[] = $payment_method;
        $types .= 's';
    }

    $sql = "SELECT * FROM donhang WHERE $where ORDER BY ngay_tao DESC LIMIT 300";
    $list = db_fetch_all($sql, $types, $params);

    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));

    renderView('donhang/index', [
        'list' => $list,
        'kw' => $kw,
        'from' => $from,
        'to' => $to,
        'payment_method' => $payment_method,
        'status' => $status,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Đơn hàng'
    ], 'none');
} catch (Exception $e) {
    error_log("Error in donhang/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải danh sách đơn hàng.');
    redirect('../index.php');
}
?>
