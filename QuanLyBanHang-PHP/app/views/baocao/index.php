<div class="header-bar">
    <h2><i class="fas fa-chart-line"></i> Báo cáo doanh thu</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <!-- Filter Form -->
  <form method="get" style="margin-bottom:30px;padding:20px;background:#f7fafc;border-radius:8px;display:flex;align-items:center;gap:15px;flex-wrap:wrap;">
    <input type="hidden" name="tab" value="<?php echo htmlspecialchars($tab ?? 'tonghop'); ?>">
    <label style="font-weight:600;">Từ ngày:</label>
    <input type="date" name="ngay_bd" value="<?php echo htmlspecialchars($ngay_bd ?? ''); ?>" required style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;">
    <label style="font-weight:600;">Đến ngày:</label>
    <input type="date" name="ngay_kt" value="<?php echo htmlspecialchars($ngay_kt ?? ''); ?>" required style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;">
    <button type="submit" class="btn btn-primary" style="padding:8px 20px;background:#4caf50;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:600;">
      <i class="fas fa-filter"></i> Lọc
    </button>
  </form>

  <!-- Summary Statistics -->
  <div class="stats-row" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(250px, 1fr));gap:20px;margin-bottom:30px;">
    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
      <div style="font-size:14px;color:#666;margin-bottom:10px;">Tổng doanh thu</div>
      <b style="font-size:28px;color:#2196f3;"><?php echo number_format($tong_doanh_thu ?? 0, 0, ',', '.'); ?>₫</b>
    </div>
    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
      <div style="font-size:14px;color:#666;margin-bottom:10px;">Tổng số đơn</div>
      <b style="font-size:28px;color:#4caf50;"><?php echo number_format($tong_so_don ?? 0, 0, ',', '.'); ?></b>
    </div>
    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
      <div style="font-size:14px;color:#666;margin-bottom:10px;">Đơn hàng trung bình</div>
      <b style="font-size:28px;color:#ff9800;">
        <?php 
        $don_tb = ($tong_so_don ?? 0) > 0 ? ($tong_doanh_thu ?? 0) / ($tong_so_don ?? 1) : 0;
        echo number_format($don_tb, 0, ',', '.'); 
        ?>₫
      </b>
    </div>
  </div>

  <!-- Tab Navigation -->
  <div style="display:flex;gap:8px;margin-bottom:24px;border-bottom:2px solid #eef2fa;">
    <a href="<?php echo getBaseUrl(); ?>app/baocao/index.php?tab=tonghop&ngay_bd=<?php echo urlencode($ngay_bd ?? ''); ?>&ngay_kt=<?php echo urlencode($ngay_kt ?? ''); ?>" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? 'tonghop') === 'tonghop' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;">
      <i class="fas fa-list"></i> Báo cáo tổng hợp
    </a>
    <a href="<?php echo getBaseUrl(); ?>app/baocao/index.php?tab=chitiet&ngay_bd=<?php echo urlencode($ngay_bd ?? ''); ?>&ngay_kt=<?php echo urlencode($ngay_kt ?? ''); ?>" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? '') === 'chitiet' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;">
      <i class="fas fa-table"></i> Báo cáo chi tiết
    </a>
    <a href="<?php echo getBaseUrl(); ?>app/baocao/index.php?tab=daily_revenue&ngay_bd=<?php echo urlencode($ngay_bd ?? ''); ?>&ngay_kt=<?php echo urlencode($ngay_kt ?? ''); ?>" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? '') === 'daily_revenue' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;">
      <i class="fas fa-calendar-day"></i> Doanh thu theo ngày
    </a>
  </div>

  <?php if(($tab ?? 'tonghop') === 'tonghop'): ?>
    <!-- Tab: Báo cáo tổng hợp -->
    <div style="margin-bottom:30px;">
      <h3 style="margin-bottom:16px;color:#22334b;">Báo cáo tổng hợp theo ngày</h3>
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
          <thead>
            <tr style="background:#eef2ff;">
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Số đơn</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tổng tiền</th>
            </tr>
          </thead>
          <tbody>
          <?php if(!empty($tonghop_data) && is_array($tonghop_data)): foreach($tonghop_data as $row): ?>
            <tr style="border-bottom:1px solid #eef2fa;">
              <td style="padding:12px;"><?php echo date('d/m/Y', strtotime($row['ngay'])); ?></td>
              <td style="padding:12px;text-align:right;"><?php echo number_format($row['so_don'] ?? 0, 0, ',', '.'); ?></td>
              <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;">
                <?php echo number_format($row['tong_tien'] ?? 0, 0, ',', '.'); ?>₫
              </td>
            </tr>
          <?php endforeach; else: ?>
            <tr><td colspan="3" style="padding:20px;text-align:center;color:#999;">Chưa có dữ liệu trong khoảng thời gian này</td></tr>
          <?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>

  <?php elseif(($tab ?? '') === 'chitiet'): ?>
    <!-- Tab: Báo cáo chi tiết -->
    <div style="margin-bottom:30px;">
      <h3 style="margin-bottom:16px;color:#22334b;">Báo cáo chi tiết theo phương thức thanh toán</h3>
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
          <thead>
            <tr style="background:#eef2ff;">
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tiền mặt</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Chuyển khoản</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Thẻ</th>
              <?php 
              // Check if app_online column exists (passed from controller)
              if (isset($has_app_online) && $has_app_online): 
              ?>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">App online</th>
              <?php endif; ?>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tổng tiền</th>
            </tr>
          </thead>
          <tbody>
          <?php if(!empty($chitiet_data) && is_array($chitiet_data)): foreach($chitiet_data as $row): ?>
            <tr style="border-bottom:1px solid #eef2fa;">
              <td style="padding:12px;"><?php echo date('d/m/Y', strtotime($row['ngay'])); ?></td>
              <td style="padding:12px;text-align:right;">
                <?php echo number_format($row['tien_mat'] ?? 0, 0, ',', '.'); ?>₫
              </td>
              <td style="padding:12px;text-align:right;">
                <?php echo number_format($row['chuyen_khoan'] ?? 0, 0, ',', '.'); ?>₫
              </td>
              <td style="padding:12px;text-align:right;">
                <?php echo number_format($row['the'] ?? 0, 0, ',', '.'); ?>₫
              </td>
              <?php if ($has_app_online): ?>
              <td style="padding:12px;text-align:right;">
                <?php echo number_format($row['app_online'] ?? 0, 0, ',', '.'); ?>₫
              </td>
              <?php endif; ?>
              <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;">
                <?php echo number_format($row['tong_tien'] ?? 0, 0, ',', '.'); ?>₫
              </td>
            </tr>
          <?php endforeach; else: ?>
            <tr><td colspan="<?php echo $has_app_online ? 6 : 5; ?>" style="padding:20px;text-align:center;color:#999;">Chưa có dữ liệu trong khoảng thời gian này</td></tr>
          <?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>
  <?php elseif(($tab ?? '') === 'daily_revenue'): ?>
    <!-- Tab: Doanh thu theo ngày (daily_revenue) -->
    <div style="margin-bottom:30px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
        <h3 style="margin:0;color:#22334b;">Doanh thu theo ngày (từ bảng daily_revenue)</h3>
        <button id="btnRebuild" onclick="rebuildDailyRevenue()" 
                style="padding:8px 16px;background:#ff9800;color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:600;">
          <i class="fas fa-sync-alt"></i> Rebuild dữ liệu
        </button>
      </div>
      <div style="padding:12px;background:#fff3cd;border:1px solid #ffc107;border-radius:8px;margin-bottom:16px;color:#856404;font-size:14px;">
        <i class="fas fa-info-circle"></i> 
        Dữ liệu này được tính từ bảng <code>daily_revenue</code>. 
        Nếu dữ liệu chưa cập nhật, vui lòng click "Rebuild dữ liệu" để cập nhật từ bảng đơn hàng.
      </div>
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
          <thead>
            <tr style="background:#eef2ff;">
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Số đơn</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tổng tiền</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Đơn trung bình</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Cập nhật lúc</th>
            </tr>
          </thead>
          <tbody>
          <?php 
          if(!empty($daily_revenue_data) && is_array($daily_revenue_data)): 
            $total_revenue = 0;
            $total_orders = 0;
            foreach($daily_revenue_data as $row): 
              $total_revenue += (float)($row['total_amount'] ?? 0);
              $total_orders += (int)($row['order_count'] ?? 0);
              $avg_order = (int)($row['order_count'] ?? 0) > 0 ? (float)($row['total_amount'] ?? 0) / (int)($row['order_count'] ?? 0) : 0;
          ?>
            <tr style="border-bottom:1px solid #eef2fa;">
              <td style="padding:12px;font-weight:600;"><?php echo date('d/m/Y', strtotime($row['day_date'])); ?></td>
              <td style="padding:12px;text-align:right;"><?php echo number_format($row['order_count'] ?? 0, 0, ',', '.'); ?></td>
              <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;">
                <?php echo number_format($row['total_amount'] ?? 0, 0, ',', '.'); ?>₫
              </td>
              <td style="padding:12px;text-align:right;color:#64748b;">
                <?php echo number_format($avg_order, 0, ',', '.'); ?>₫
              </td>
              <td style="padding:12px;color:#64748b;font-size:13px;">
                <?php echo $row['updated_at'] ? date('d/m/Y H:i', strtotime($row['updated_at'])) : '-'; ?>
              </td>
            </tr>
          <?php 
            endforeach; 
          ?>
            <tr style="background:#f7fafc;font-weight:600;border-top:2px solid #e0e0e0;">
              <td style="padding:12px;">Tổng cộng</td>
              <td style="padding:12px;text-align:right;color:#4caf50;"><?php echo number_format($total_orders, 0, ',', '.'); ?></td>
              <td style="padding:12px;text-align:right;color:#2196f3;"><?php echo number_format($total_revenue, 0, ',', '.'); ?>₫</td>
              <td style="padding:12px;text-align:right;color:#ff9800;">
                <?php echo $total_orders > 0 ? number_format($total_revenue / $total_orders, 0, ',', '.') : '0'; ?>₫
              </td>
              <td style="padding:12px;"></td>
            </tr>
          <?php else: ?>
            <tr><td colspan="5" style="padding:20px;text-align:center;color:#999;">
              Chưa có dữ liệu trong khoảng thời gian này. 
              <a href="javascript:void(0)" onclick="rebuildDailyRevenue()" style="color:#2196f3;text-decoration:underline;">Click để rebuild dữ liệu</a>
            </td></tr>
          <?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>
  <?php endif; ?>

  <!-- Biểu đồ doanh thu theo giờ -->
  <div class="chart-card" style="margin-top:30px;">
    <div class="chart-title">Doanh thu theo giờ trong ngày</div>
    <div class="chart-placeholder">
      <?php 
      if (!isset($hourly_chart) || !is_array($hourly_chart) || empty($hourly_chart)) {
        $hourly_chart = [];
        for ($i = 0; $i < 24; $i++) {
          $hourly_chart[] = ['label' => $i . 'h', 'value' => 0, 'height' => 30];
        }
      }
      foreach($hourly_chart as $i => $bar): 
        $height = isset($bar['height']) ? max(20, (int)$bar['height']) : 30;
        $value = isset($bar['value']) ? (float)$bar['value'] : 0;
        $label = isset($bar['label']) ? htmlspecialchars($bar['label']) : '';
      ?>
      <div class="chart-bar-wrapper">
        <div class="chart-bar" style="height:<?php echo $height; ?>px" title="<?php echo $label; ?>: <?php echo number_format($value, 0, ',', '.'); ?>₫">
          <?php if ($value > 0): ?>
          <span class="bar-value"><?php echo number_format($value/1000, 0); ?>k</span>
          <?php endif; ?>
        </div>
        <div class="bar-label"><?php echo $label; ?></div>
      </div>
      <?php endforeach; ?>
    </div>
  </div>
</div>

<script>
function rebuildDailyRevenue() {
  if (!confirm('Bạn có chắc chắn muốn rebuild dữ liệu daily_revenue? Hành động này sẽ xóa và tính lại toàn bộ dữ liệu từ bảng đơn hàng.')) {
    return;
  }
  
  const btn = document.getElementById('btnRebuild');
  const originalText = btn.innerHTML;
  btn.disabled = true;
  btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
  
  fetch('<?php echo getBaseUrl(); ?>app/api/rebuild_daily_revenue.php', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      alert('Rebuild thành công! Đã cập nhật ' + data.inserted + ' bản ghi.\nThời gian: ' + data.timestamp);
      // Reload page to show updated data
      window.location.reload();
    } else {
      alert('Lỗi: ' + (data.error || 'Không thể rebuild dữ liệu'));
      btn.disabled = false;
      btn.innerHTML = originalText;
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Có lỗi xảy ra khi rebuild dữ liệu');
    btn.disabled = false;
    btn.innerHTML = originalText;
  });
}
</script>
