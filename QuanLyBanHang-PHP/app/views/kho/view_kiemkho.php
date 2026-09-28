<div class="header-bar">
    <h2><i class="fas fa-file-alt"></i> Chi tiết phiếu kiểm kho: <?php echo htmlspecialchars($phieu['ma_phieu_kiem']); ?></h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <div style="margin-bottom:20px;display:flex;justify-content:space-between;align-items:center;">
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php?tab=kiemkho" class="main-link" style="display:inline-block;">
      <i class="fas fa-arrow-left"></i> Quay lại danh sách
    </a>
    
    <div>
      <?php if($phieu['trang_thai'] === 'cho_duyet'): ?>
        <?php if(isManager()): ?>
          <a href="<?php echo getBaseUrl(); ?>app/kho/approve_kiemkho.php?id=<?php echo $phieu['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn duyệt phiếu này? Số lượng tồn kho sẽ bị ghi đè thành Tồn thực tế!')" class="btn btn-primary" style="background:#28a745;border:none;margin-right:8px;">
            <i class="fas fa-check"></i> Duyệt phiếu
          </a>
        <?php endif; ?>
        <a href="<?php echo getBaseUrl(); ?>app/kho/cancel_kiemkho.php?id=<?php echo $phieu['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn hủy phiếu này?')" class="btn btn-danger" style="background:#dc3545;border:none;color:white;padding:9px 20px;border-radius:4px;text-decoration:none;">
          <i class="fas fa-times"></i> Hủy phiếu
        </a>
      <?php endif; ?>
    </div>
  </div>

  <?php if(isset($_GET['success'])): ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:#e7f7ed;color:#0c6b3d;">Thao tác thành công!</div>
  <?php endif; ?>
  <?php if(isset($_GET['error'])): ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:#f8d7da;color:#721c24;">Có lỗi xảy ra, vui lòng thử lại!</div>
  <?php endif; ?>

  <div style="background:#f8fafc;padding:20px;border-radius:8px;margin-bottom:24px;display:grid;grid-template-columns:1fr 1fr;gap:20px;">
    <div>
      <div style="margin-bottom:12px;"><span style="color:#64748b;display:inline-block;width:120px;">Mã phiếu:</span> <strong style="color:#0f172a;"><?php echo htmlspecialchars($phieu['ma_phieu_kiem']); ?></strong></div>
      <div style="margin-bottom:12px;"><span style="color:#64748b;display:inline-block;width:120px;">Ngày kiểm:</span> <strong style="color:#0f172a;"><?php echo date('d/m/Y', strtotime($phieu['ngay_kiem'])); ?></strong></div>
      <div style="margin-bottom:12px;"><span style="color:#64748b;display:inline-block;width:120px;">Trạng thái:</span> 
        <?php
          $status_class = '';
          $status_text = '';
          switch($phieu['trang_thai']) {
            case 'cho_duyet':
              $status_class = 'background:#fff3cd;color:#856404;';
              $status_text = 'Chờ duyệt';
              break;
            case 'da_duyet':
              $status_class = 'background:#d1ecf1;color:#0c5460;';
              $status_text = 'Đã duyệt';
              break;
            case 'da_huy':
              $status_class = 'background:#f8d7da;color:#721c24;';
              $status_text = 'Đã hủy';
              break;
          }
        ?>
        <span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:13px;font-weight:600;<?php echo $status_class; ?>"><?php echo $status_text; ?></span>
      </div>
    </div>
    <div>
      <div style="margin-bottom:12px;"><span style="color:#64748b;display:inline-block;width:120px;">Nhân viên:</span> <strong style="color:#0f172a;"><?php echo htmlspecialchars($phieu['ten_nhanvien'] ?? 'N/A'); ?></strong></div>
      <div style="margin-bottom:12px;"><span style="color:#64748b;display:inline-block;width:120px;vertical-align:top;">Ghi chú:</span> <span style="color:#0f172a;display:inline-block;width:calc(100% - 125px);"><?php echo nl2br(htmlspecialchars($phieu['ghi_chu'] ?: 'Không có ghi chú')); ?></span></div>
    </div>
  </div>

  <h3 style="margin-bottom:16px;color:#1e293b;">Chi tiết kiểm kê</h3>
  
  <div style="overflow-x:auto;">
    <table style="width:100%;border-collapse:collapse;">
      <thead>
        <tr style="background:#eef2ff;">
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;">Mã NVL</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;">Tên NVL</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;">Đơn vị</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#334155;">Tồn hệ thống</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#0f172a;">Tồn thực tế</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#334155;">Chênh lệch</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;">Lý do</th>
        </tr>
      </thead>
      <tbody>
      <?php if(!empty($chitiet)): foreach($chitiet as $row): 
        $sys = $row['ton_he_thong'];
        $act = $row['ton_thuc_te'];
        $diff = $row['chenh_lech'];
      ?>
        <tr style="border-bottom:1px solid #e2e8f0;">
          <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($row['ma_nguyenlieu']); ?></td>
          <td style="padding:12px;font-weight:600;color:#1e293b;"><?php echo htmlspecialchars($row['ten_nguyenlieu']); ?></td>
          <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($row['don_vi_tinh']); ?></td>
          <td style="padding:12px;text-align:right;color:#64748b;"><?php echo number_format($sys, 2, ',', '.'); ?></td>
          <td style="padding:12px;text-align:right;font-weight:bold;color:#0f172a;"><?php echo number_format($act, 2, ',', '.'); ?></td>
          <td style="padding:12px;text-align:right;font-weight:bold;color:<?php echo $diff < 0 ? '#dc3545' : ($diff > 0 ? '#28a745' : '#94a3b8'); ?>;">
            <?php echo ($diff > 0 ? '+' : '') . number_format($diff, 2, ',', '.'); ?>
          </td>
          <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($row['ly_do'] ?: '-'); ?></td>
        </tr>
      <?php endforeach; else: ?>
        <tr><td colspan="7" style="padding:20px;text-align:center;color:#999;">Không có dữ liệu chi tiết</td></tr>
      <?php endif; ?>
      </tbody>
    </table>
  </div>
</div>
