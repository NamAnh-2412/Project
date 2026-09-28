<div class="header-bar">
    <h2><i class="fas fa-users"></i> Quản lý khách hàng</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <?php 
  $flashMessages = getFlashMessages();
  foreach ($flashMessages as $msg):
    $flashBg = '#e0f2fe';
    $flashColor = '#0369a1';
    if ($msg['type'] === 'success') { $flashBg = '#e7f7ed'; $flashColor = '#0c6b3d'; }
    elseif ($msg['type'] === 'error') { $flashBg = '#fee2e2'; $flashColor = '#991b1b'; }
  ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:<?php echo $flashBg; ?>;color:<?php echo $flashColor; ?>;">
      <?php echo htmlspecialchars($msg['message']); ?>
    </div>
  <?php endforeach; ?>

  <p style="color:#64748b;font-size:14px;margin:0 0 16px 0;">
    <i class="fas fa-info-circle"></i> Khách hàng được tạo tự động khi đặt hàng. Trang này dùng để xem và phân tích.
  </p>

  <div class="search-filter-row">
    <form method="get" class="filter-form" id="customerFilterForm">
      <div class="input-group">
        <i class="fas fa-search search-icon"></i>
        <input type="text" name="q" id="customerSearchInput" placeholder="Tìm theo tên hoặc SĐT..." value="<?php echo htmlspecialchars($q ?? ''); ?>">
      </div>
      <div class="select-group">
        <select name="rank">
          <option value="">Tất cả hạng</option>
          <option value="thuong" <?php echo (isset($rank) && $rank === 'thuong') ? 'selected' : ''; ?>>Thường</option>
          <option value="bac" <?php echo (isset($rank) && $rank === 'bac') ? 'selected' : ''; ?>>Bạc</option>
          <option value="vang" <?php echo (isset($rank) && $rank === 'vang') ? 'selected' : ''; ?>>Vàng</option>
          <option value="vip" <?php echo (isset($rank) && $rank === 'vip') ? 'selected' : ''; ?>>VIP</option>
        </select>
      </div>
      <button type="submit" class="btn-filter"><i class="fas fa-filter"></i> Lọc</button>
      <?php if (!empty($q) || !empty($rank)): ?>
        <a href="index.php" class="btn-reset"><i class="fas fa-times"></i> Xóa lọc</a>
      <?php endif; ?>
    </form>
  </div>

  <table class="table-responsive">
    <thead>
      <tr>
        <th>Khách hàng</th>
        <th>SĐT</th>
        <th>Hạng TV</th>
        <th>Tổng tiền đã chi</th>
        <th>Tổng đơn</th>
        <th>Ngày tạo</th>
        <th>Tác vụ</th>
      </tr>
    </thead>
    <tbody>
    <?php if(!empty($list) && is_array($list)): foreach($list as $row): 
      $name = $row['ten_khachhang'] ?? '';
      $initial = $name ? strtoupper(mb_substr($name, 0, 1, 'UTF-8')) : '?';
      $rankKey = $row['loai_thanh_vien'] ?? 'thuong';
      $totalSpent = (float)($row['tong_tien_da_chi'] ?? 0);
    ?>
      <tr>
        <td>
          <div style="display:flex;align-items:center;gap:12px;">
            <div style="width:40px;height:40px;border-radius:50%;background:linear-gradient(135deg,#f97316,#ea580c);color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:16px;flex-shrink:0;">
              <?php echo htmlspecialchars($initial); ?>
            </div>
            <div>
              <div style="font-weight:600;color:#1e293b;"><?php echo htmlspecialchars($name); ?></div>
              <?php if (!empty($row['email'])): ?>
                <div style="font-size:12px;color:#64748b;"><?php echo htmlspecialchars($row['email']); ?></div>
              <?php endif; ?>
            </div>
          </div>
        </td>
        <td><?php echo htmlspecialchars($row['so_dien_thoai'] ?? '-'); ?></td>
        <td>
          <span class="rank-badge <?php echo getCustomerRankClass($rankKey); ?>">
            <?php echo getCustomerRankLabel($rankKey); ?>
          </span>
        </td>
        <td style="font-weight:600;color:#059669;"><?php echo formatMoney($totalSpent); ?></td>
        <td><?php echo (int)($row['tong_donhang'] ?? 0); ?></td>
        <td><?php echo !empty($row['ngay_tao']) ? formatDate($row['ngay_tao']) : '-'; ?></td>
        <td>
          <a href="<?php echo getBaseUrl(); ?>app/khachhang/view.php?id=<?php echo $row['id']; ?>" style="color:#2196f3;">Xem</a> |
          <a href="<?php echo getBaseUrl(); ?>app/khachhang/edit.php?id=<?php echo $row['id']; ?>" style="color:#ff9800;">Sửa</a> |
          <a href="<?php echo getBaseUrl(); ?>app/khachhang/delete.php?id=<?php echo $row['id']; ?>" style="color:#f44336;" onclick="return confirm('Xóa khách hàng?')">Xóa</a>
        </td>
      </tr>
    <?php endforeach; else: ?>
      <tr><td colspan="7" style="text-align:center;padding:40px;color:#94a3b8;">Không có khách hàng nào</td></tr>
    <?php endif; ?>
    </tbody>
  </table>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
  const input = document.getElementById('customerSearchInput');
  const form = document.getElementById('customerFilterForm');
  if (!input || !form) return;

  let timer = null;
  input.addEventListener('input', function() {
    clearTimeout(timer);
    timer = setTimeout(function() {
      form.submit();
    }, 400);
  });
});
</script>
