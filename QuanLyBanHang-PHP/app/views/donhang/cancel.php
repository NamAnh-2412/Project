<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <div class="alert alert-danger">
        <strong><i class="fas fa-exclamation-triangle"></i> Bạn có chắc chắn muốn hủy đơn hàng này?</strong>
        <div style="margin-top:15px;padding:15px;background:#fff;border-radius:8px">
          <div style="margin-bottom:10px"><strong>Mã đơn:</strong> <?php echo htmlspecialchars($order['ma_donhang']); ?></div>
          <div style="margin-bottom:10px"><strong>Ngày tạo:</strong> <?php echo htmlspecialchars($order['ngay_tao']); ?></div>
          <div style="margin-bottom:10px"><strong>Khách hàng:</strong> <?php echo htmlspecialchars($order['ten_khachhang'] ?? 'Khách lẻ'); ?></div>
          <?php if ($customer): ?>
            <div style="margin-bottom:10px"><strong>SĐT:</strong> <?php echo htmlspecialchars($customer['so_dien_thoai'] ?? ''); ?></div>
          <?php endif; ?>
          <div style="margin-bottom:10px"><strong>Tổng tiền:</strong> <span style="color:#f44336;font-weight:700"><?php echo number_format($order['thanh_tien'], 0, ',', '.'); ?>₫</span></div>
          <div style="margin-bottom:10px"><strong>Trạng thái hiện tại:</strong> <?php echo formatOrderStatus($order['trang_thai']); ?></div>
        </div>
        <div style="margin-top:15px;padding:10px;background:#fff3cd;border-left:4px solid #ffc107;border-radius:4px">
          <strong>Lưu ý:</strong>
          <ul style="margin:10px 0 0 20px;padding:0">
            <li>Đơn hàng sẽ được đánh dấu là "Đã hủy"</li>
            <?php if (!empty($order['khachhang_id']) && $order['trang_thai'] === ORDER_STATUS_COMPLETED): ?>
              <li>Tổng chi tiêu và hạng thành viên của khách sẽ được điều chỉnh lại</li>
            <?php elseif ($order['trang_thai'] === ORDER_STATUS_PENDING): ?>
              <li>Đơn chờ xử lý sẽ được đánh dấu hủy (chưa ảnh hưởng doanh thu)</li>
            <?php endif; ?>
            <li>Hành động này không thể hoàn tác</li>
          </ul>
        </div>
      </div>
      <form method="post" style="margin-top:20px">
        <input type="hidden" name="confirm" value="1">
        <div style="display:flex;gap:10px">
          <button class="btn btn-danger" type="submit">
            <i class="fas fa-times"></i> Xác nhận hủy đơn hàng
          </button>
          <a class="btn btn-secondary" href="index.php">
            <i class="fas fa-arrow-left"></i> Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

