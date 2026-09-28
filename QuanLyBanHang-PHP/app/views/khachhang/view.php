<?php
$name = $customer['ten_khachhang'] ?? '';
$initial = $name ? strtoupper(mb_substr($name, 0, 1, 'UTF-8')) : '?';
$rankKey = $customer['loai_thanh_vien'] ?? 'thuong';
?>
<div class="header-bar">
    <h2><i class="fas fa-user"></i> Chi tiết khách hàng</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <div class="customer-detail-grid">
    <div class="customer-info-card">
      <div class="customer-avatar-large"><?php echo htmlspecialchars($initial); ?></div>
      <div class="customer-profile-name"><?php echo htmlspecialchars($name); ?></div>
      <div class="customer-profile-rank">
        <span class="rank-badge <?php echo getCustomerRankClass($rankKey); ?>">
          <?php echo getCustomerRankLabel($rankKey); ?>
        </span>
      </div>

      <div class="customer-info-list">
        <div class="info-item">
          <span class="info-label">Số điện thoại</span>
          <span class="info-value"><?php echo htmlspecialchars($customer['so_dien_thoai'] ?? '-'); ?></span>
        </div>
        <?php if (!empty($customer['email'])): ?>
        <div class="info-item">
          <span class="info-label">Email</span>
          <span class="info-value"><?php echo htmlspecialchars($customer['email']); ?></span>
        </div>
        <?php endif; ?>
        <?php if (!empty($customer['ngay_sinh'])): ?>
        <div class="info-item">
          <span class="info-label">Ngày sinh</span>
          <span class="info-value"><?php echo formatDate($customer['ngay_sinh']); ?></span>
        </div>
        <?php endif; ?>
        <?php if (!empty($customer['dia_chi'])): ?>
        <div class="info-item">
          <span class="info-label">Địa chỉ</span>
          <span class="info-value"><?php echo htmlspecialchars($customer['dia_chi']); ?></span>
        </div>
        <?php endif; ?>
        <div class="info-item">
          <span class="info-label">Ngày tạo</span>
          <span class="info-value"><?php echo !empty($customer['ngay_tao']) ? formatDate($customer['ngay_tao'], 'd/m/Y H:i') : '-'; ?></span>
        </div>
        <?php if (!empty($customer['ghi_chu'])): ?>
        <div class="info-item">
          <span class="info-label">Ghi chú</span>
          <span class="info-value" style="white-space:pre-wrap;"><?php echo htmlspecialchars($customer['ghi_chu']); ?></span>
        </div>
        <?php endif; ?>
      </div>

      <div style="margin-top:20px;display:flex;gap:8px;flex-wrap:wrap;">
        <a href="<?php echo getBaseUrl(); ?>app/khachhang/edit.php?id=<?php echo $customer['id']; ?>" class="btn btn-primary" style="text-decoration:none;padding:8px 16px;">Sửa thông tin</a>
        <a href="index.php" class="btn btn-secondary" style="text-decoration:none;padding:8px 16px;background:#64748b;color:#fff;border-radius:6px;">Về danh sách</a>
      </div>
    </div>

    <div>
      <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px;">
        <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
          <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Tổng tiền đã chi</div>
          <div style="font-size:22px;font-weight:700;color:#059669;"><?php echo formatMoney($stats['total_spent']); ?></div>
        </div>
        <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
          <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Số đơn hàng</div>
          <div style="font-size:22px;font-weight:700;color:#2196f3;"><?php echo $stats['total_orders']; ?></div>
        </div>
        <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
          <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Đơn trung bình</div>
          <div style="font-size:22px;font-weight:700;color:#f97316;"><?php echo formatMoney($stats['avg_order']); ?></div>
        </div>
      </div>

      <div class="customer-history-card">
        <div class="card-title"><i class="fas fa-receipt"></i> Lịch sử đơn hàng (10 gần nhất)</div>
        <?php if (empty($orders)): ?>
          <p style="color:#94a3b8;text-align:center;padding:30px 0;">Chưa có đơn hàng nào</p>
        <?php else: ?>
          <table class="table-responsive">
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Ngày</th>
                <th>Thanh toán</th>
                <th>Trạng thái</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              <?php foreach ($orders as $order): ?>
              <tr>
                <td style="font-weight:600;"><?php echo htmlspecialchars($order['ma_donhang'] ?? '-'); ?></td>
                <td><?php echo !empty($order['ngay_tao']) ? formatDate($order['ngay_tao'], 'd/m/Y H:i') : '-'; ?></td>
                <td><?php echo formatPaymentMethod($order['phuong_thuc_thanhtoan'] ?? ''); ?></td>
                <td>
                  <?php
                  $status = $order['trang_thai'] ?? '';
                  $statusLabel = $status === ORDER_STATUS_CANCELLED ? 'Đã hủy' : 'Hoàn thành';
                  $statusColor = $status === ORDER_STATUS_CANCELLED ? '#991b1b' : '#0c6b3d';
                  $statusBg = $status === ORDER_STATUS_CANCELLED ? '#fee2e2' : '#e7f7ed';
                  ?>
                  <span style="padding:4px 10px;border-radius:999px;font-size:12px;font-weight:600;background:<?php echo $statusBg; ?>;color:<?php echo $statusColor; ?>;">
                    <?php echo $statusLabel; ?>
                  </span>
                </td>
                <td style="font-weight:600;color:#059669;"><?php echo formatMoney($order['thanh_tien'] ?? 0); ?></td>
              </tr>
              <?php endforeach; ?>
            </tbody>
          </table>
        <?php endif; ?>
      </div>
    </div>
  </div>
</div>
