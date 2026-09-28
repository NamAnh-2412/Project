<?php
if (!function_exists('getBaseUrl')) {
    require_once __DIR__ . '/../../core/view.php';
}
$base_path = getBaseUrl();
$statusOptions = getOrderStatusFilterOptions();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn hàng - CHILL GUYS</title>
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/style.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/layout.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/ui.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/orders.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/dark-mode.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css"/>
</head>
<body>
<div class="app-wrapper">
    <?php
    $sidebar_path = __DIR__ . '/../../core/sidebar.php';
    if (file_exists($sidebar_path)) {
        include $sidebar_path;
    }
    ?>
    <div class="main-content">
        <div class="main-inner">
            <div class="header-bar-donhang">
                <div class="left">
                    <a href="<?php echo $base_path; ?>index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-receipt"></i></span> ĐƠN HÀNG</h1>
                </div>
                <a class="btn-add" href="<?php echo $base_path; ?>app/donhang/create.php"><i class="fa fa-plus"></i> Thêm đơn hàng mới</a>
            </div>
            <div class="content-wrap orders-list">
                <div class="tbl-wrap orders-list" style="width:100%">
                    <?php
                    $flashMessages = getFlashMessages();
                    foreach ($flashMessages as $msg):
                        $bg = $msg['type'] === 'success' ? '#e7f7ed' : '#fee2e2';
                        $color = $msg['type'] === 'success' ? '#0c6b3d' : '#991b1b';
                    ?>
                        <div style="padding:12px 16px;border-radius:8px;margin-bottom:16px;background:<?php echo $bg; ?>;color:<?php echo $color; ?>;">
                            <?php echo htmlspecialchars($msg['message']); ?>
                        </div>
                    <?php endforeach; ?>

                    <div class="toolbar">
                        <form method="get" class="filters order-filters">
                            <input type="text" name="kw" placeholder="Nhập mã đơn, tên/SDT khách..." value="<?php echo htmlspecialchars($kw ?? '');?>">
                            <input type="date" name="from" value="<?php echo htmlspecialchars($from ?? '');?>">
                            <input type="date" name="to" value="<?php echo htmlspecialchars($to ?? '');?>">
                            <select name="status">
                                <?php foreach ($statusOptions as $value => $label): ?>
                                    <option value="<?php echo htmlspecialchars($value); ?>" <?php echo ($status ?? '') === $value ? 'selected' : ''; ?>>
                                        <?php echo htmlspecialchars($label); ?>
                                    </option>
                                <?php endforeach; ?>
                            </select>
                            <select name="payment_method">
                                <?php
                                $payment_methods = getPaymentMethods();
                                $selected_payment = $payment_method ?? '';
                                foreach ($payment_methods as $value => $label):
                                ?>
                                    <option value="<?php echo htmlspecialchars($value); ?>" <?php echo $selected_payment === $value ? 'selected' : ''; ?>>
                                        <?php echo htmlspecialchars($label); ?>
                                    </option>
                                <?php endforeach; ?>
                            </select>
                            <button class="btn btn-primary" type="submit">Lọc</button>
                        </form>
                    </div>
                    <table class="orders-table table-responsive">
                        <thead>
                            <tr>
                                <th>Mã đơn</th><th>Ngày/T.gian</th><th>Khách hàng</th><th>SDT</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th>Tác vụ</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php if(!empty($list) && is_array($list)): foreach($list as $row): ?>
                            <tr class="order-row-clickable" data-href="<?php echo $base_path; ?>app/donhang/view.php?id=<?php echo $row['id']; ?>">
                                <td>
                                    <a href="<?php echo $base_path; ?>app/donhang/view.php?id=<?php echo $row['id']; ?>" class="order-code-link">
                                        <?php echo htmlspecialchars($row['ma_donhang']);?>
                                    </a>
                                </td>
                                <td><?php echo !empty($row['ngay_tao']) ? formatDate($row['ngay_tao'], 'd/m/Y H:i') : '-'; ?></td>
                                <td><?php echo htmlspecialchars(getOrderCustomerDisplayName($row)); ?></td>
                                <td><?php echo htmlspecialchars($row['sdt_khachhang'] ?? '—'); ?></td>
                                <td><?php echo formatMoney($row['thanh_tien'] ?? 0); ?></td>
                                <td><?php echo formatOrderPayment($row['phuong_thuc_thanhtoan'] ?? '', $row['trang_thai'] ?? ''); ?></td>
                                <td>
                                    <span class="order-status-badge <?php echo getOrderStatusClass($row['trang_thai'] ?? ''); ?>">
                                        <?php echo formatOrderStatus($row['trang_thai'] ?? ''); ?>
                                    </span>
                                </td>
                                <td class="order-actions-cell" onclick="event.stopPropagation();">
                                    <a href="<?php echo $base_path; ?>app/donhang/view.php?id=<?php echo $row['id']; ?>" class="btn-view-order">Xem</a>
                                    <?php if (canCompleteOrder($row['trang_thai'] ?? '')): ?>
                                        <a href="<?php echo $base_path; ?>app/donhang/pay.php?id=<?php echo $row['id']; ?>" class="btn-pay-order-sm">
                                            <i class="fas fa-credit-card"></i> TT
                                        </a>
                                    <?php endif; ?>
                                    <?php if(($row['trang_thai'] ?? '') !== ORDER_STATUS_CANCELLED): ?>
                                        <a href="<?php echo $base_path; ?>app/donhang/cancel.php?id=<?php echo $row['id']; ?>" class="btn-cancel" onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')">
                                            <i class="fas fa-times"></i> Hủy
                                        </a>
                                    <?php else: ?>
                                        <span class="order-cancelled-label">Đã hủy</span>
                                    <?php endif; ?>
                                </td>
                            </tr>
                        <?php endforeach; else: ?>
                            <tr><td colspan="8">Chưa có dữ liệu</td></tr>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="<?php echo $base_path; ?>js/main.js"></script>
<script>
document.querySelectorAll('.order-row-clickable').forEach(function(row) {
    row.addEventListener('click', function(e) {
        if (e.target.closest('a, button')) return;
        var href = row.getAttribute('data-href');
        if (href) window.location.href = href;
    });
});
</script>
</body>
</html>
