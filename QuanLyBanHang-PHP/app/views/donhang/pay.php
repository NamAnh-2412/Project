<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if (!empty($notice)): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>

      <div class="order-pay-card">
        <h3 class="order-pay-title">
          <i class="fas fa-credit-card"></i> Thanh toán đơn hàng
        </h3>

        <div class="order-pay-summary">
          <div class="order-pay-row">
            <span>Mã đơn</span>
            <strong><?php echo htmlspecialchars($order['ma_donhang'] ?? ''); ?></strong>
          </div>
          <div class="order-pay-row">
            <span>Khách hàng</span>
            <strong><?php echo htmlspecialchars(getOrderCustomerDisplayName($order)); ?></strong>
          </div>
          <div class="order-pay-row">
            <span>Trạng thái</span>
            <span class="order-status-badge <?php echo getOrderStatusClass($order['trang_thai'] ?? ''); ?>">
              <?php echo formatOrderStatus($order['trang_thai'] ?? ''); ?>
            </span>
          </div>
          <div class="order-pay-row">
            <span>Số lượng</span>
            <strong><?php echo (int)($total_qty ?? 0); ?> món</strong>
          </div>
          <div class="order-pay-row order-pay-total">
            <span>Tổng thanh toán</span>
            <strong><?php echo formatMoney($order['thanh_tien'] ?? 0); ?></strong>
          </div>
        </div>

        <?php if (!empty($items)): ?>
          <div class="order-pay-items">
            <div class="order-pay-items-title">Chi tiết món</div>
            <ul>
              <?php foreach ($items as $item): ?>
                <li>
                  <span><?php echo htmlspecialchars($item['ten_sanpham'] ?? ''); ?></span>
                  <span>x<?php echo (int)($item['so_luong'] ?? 0); ?></span>
                  <span><?php echo formatMoney($item['thanh_tien'] ?? 0); ?></span>
                </li>
              <?php endforeach; ?>
            </ul>
          </div>
        <?php endif; ?>

        <form method="post" class="order-pay-form">
          <input type="hidden" name="confirm" value="1">

          <label class="form-label required">Phương thức thanh toán</label>
          <div class="order-pay-methods">
            <?php foreach ($paymentOptions as $value => $label): ?>
              <label class="order-pay-method-option">
                <input type="radio" name="phuong_thuc_thanhtoan" value="<?php echo htmlspecialchars($value); ?>"
                  <?php echo ($defaultPayment ?? PAYMENT_CASH) === $value ? 'checked' : ''; ?> required>
                <span><?php echo htmlspecialchars($label); ?></span>
              </label>
            <?php endforeach; ?>
          </div>

          <div class="order-pay-actions">
            <button type="submit" class="btn btn-primary" onclick="return confirm('Xác nhận thanh toán đơn hàng này?');">
              <i class="fas fa-check"></i> Xác nhận thanh toán
            </button>
            <a href="view.php?id=<?php echo (int)$order['id']; ?>" class="btn btn-secondary">Quay lại</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>
