<?php
if (!function_exists('getBaseUrl')) {
    require_once __DIR__ . '/../../core/view.php';
}
$base_path = getBaseUrl();
$canCancel = ($order['trang_thai'] ?? '') !== ORDER_STATUS_CANCELLED;
$canPay = canCompleteOrder($order['trang_thai'] ?? '');
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết đơn hàng - CHILL GUYS</title>
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
                    <a href="<?php echo $base_path; ?>app/donhang/index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách đơn</a>
                    <h1><span class="ic"><i class="fas fa-receipt"></i></span> CHI TIẾT ĐƠN HÀNG</h1>
                </div>
            </div>

            <div class="content-wrap order-detail-wrap">
                <?php
                $flashMessages = getFlashMessages();
                foreach ($flashMessages as $msg):
                    $bg = $msg['type'] === 'success' ? '#e7f7ed' : ($msg['type'] === 'info' ? '#e0f2fe' : '#fee2e2');
                    $color = $msg['type'] === 'success' ? '#0c6b3d' : ($msg['type'] === 'info' ? '#0369a1' : '#991b1b');
                ?>
                    <div style="max-width:960px;margin:0 auto 16px;padding:12px 16px;border-radius:8px;background:<?php echo $bg; ?>;color:<?php echo $color; ?>;">
                        <?php echo htmlspecialchars($msg['message']); ?>
                    </div>
                <?php endforeach; ?>

                <div class="order-detail-card">
                    <div class="order-detail-header">
                        <div>
                            <div class="order-detail-code"><?php echo htmlspecialchars($order['ma_donhang'] ?? ''); ?></div>
                            <div class="order-detail-time">
                                <i class="fas fa-clock"></i>
                                <?php echo !empty($order['ngay_tao']) ? formatDate($order['ngay_tao'], 'd/m/Y H:i') : '-'; ?>
                            </div>
                        </div>
                        <span class="order-status-badge <?php echo getOrderStatusClass($order['trang_thai'] ?? ''); ?>">
                            <?php echo formatOrderStatus($order['trang_thai'] ?? ''); ?>
                        </span>
                    </div>

                    <div class="order-detail-grid">
                        <div class="order-detail-item">
                            <span class="label">Khách hàng</span>
                            <span class="value"><?php echo htmlspecialchars(getOrderCustomerDisplayName($order)); ?></span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">SĐT</span>
                            <span class="value"><?php echo htmlspecialchars($order['sdt_khachhang'] ?? '—'); ?></span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Số thứ tự trong ngày</span>
                            <span class="value">#<?php echo (int)$daily_sequence; ?></span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Số lượng sản phẩm</span>
                            <span class="value"><?php echo (int)$total_qty; ?> món</span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Hình thức nhận</span>
                            <span class="value"><?php echo ($order['hinh_thuc_nhan'] ?? '') === 'tai_cho' ? 'Tại quầy' : htmlspecialchars($order['hinh_thuc_nhan'] ?? '—'); ?></span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Thanh toán</span>
                            <span class="value"><?php echo formatOrderPayment($order['phuong_thuc_thanhtoan'] ?? '', $order['trang_thai'] ?? ''); ?></span>
                        </div>
                    </div>

                    <h3 class="order-detail-section-title"><i class="fas fa-list"></i> Chi tiết sản phẩm</h3>
                    <?php if (empty($items)): ?>
                        <p class="order-detail-empty">Không có sản phẩm trong đơn.</p>
                    <?php else: ?>
                        <table class="order-detail-table table-responsive">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Size</th>
                                    <th>SL</th>
                                    <th>Đơn giá</th>
                                    <th>Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($items as $item): ?>
                                <tr>
                                    <td>
                                        <div class="order-item-name"><?php echo htmlspecialchars($item['ten_sanpham'] ?? ''); ?></div>
                                        <?php if (!empty($item['choices'])): ?>
                                            <div class="order-item-choices">
                                                <?php
                                                $choiceParts = [];
                                                foreach ($item['choices'] as $choice) {
                                                    $choiceParts[] = ($choice['ten_nhom'] ?? '') . ': ' . ($choice['ten_lua_chon'] ?? '');
                                                }
                                                echo htmlspecialchars(implode(' · ', $choiceParts));
                                                ?>
                                            </div>
                                        <?php endif; ?>
                                    </td>
                                    <td><?php echo htmlspecialchars($item['size'] ?? '—'); ?></td>
                                    <td><?php echo (int)($item['so_luong'] ?? 0); ?></td>
                                    <td><?php echo formatMoney($item['don_gia'] ?? 0); ?></td>
                                    <td class="order-item-total"><?php echo formatMoney($item['thanh_tien'] ?? 0); ?></td>
                                </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    <?php endif; ?>

                    <div class="order-detail-total">
                        <span>Tổng tiền đơn hàng</span>
                        <strong><?php echo formatMoney($order['thanh_tien'] ?? 0); ?></strong>
                    </div>

                    <div class="order-detail-actions">
                        <?php if ($canPay): ?>
                            <a href="<?php echo $base_path; ?>app/donhang/pay.php?id=<?php echo $order['id']; ?>" class="btn btn-pay-order">
                                <i class="fas fa-credit-card"></i> Thanh toán
                            </a>
                        <?php endif; ?>
                        <?php if ($canCancel): ?>
                            <a href="<?php echo $base_path; ?>app/donhang/cancel.php?id=<?php echo $order['id']; ?>" class="btn btn-danger" onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')">
                                <i class="fas fa-times"></i> Hủy đơn
                            </a>
                        <?php endif; ?>
                        <a href="<?php echo $base_path; ?>app/donhang/index.php" class="btn btn-secondary">Về danh sách</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="<?php echo $base_path; ?>js/main.js"></script>
</body>
</html>
