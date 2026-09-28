<div class="header-bar">
    <h2><i class="fas fa-file-invoice"></i> Chi tiết phiếu nhập kho</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <div style="margin-bottom:20px;">
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php" class="main-link" style="display:inline-block;margin-bottom:16px;">
      <i class="fas fa-arrow-left"></i> Quay lại danh sách
    </a>
  </div>

  <!-- Thông tin phiếu nhập kho -->
  <div style="background:#f8fafc;padding:20px;border-radius:8px;margin-bottom:24px;">
    <h3 style="margin-top:0;margin-bottom:16px;color:#22334b;">Thông tin phiếu nhập kho</h3>
    <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(250px, 1fr));gap:16px;">
      <div>
        <strong style="color:#64748b;font-size:14px;">Mã phiếu:</strong>
        <div style="font-size:16px;font-weight:600;color:#22334b;"><?php echo htmlspecialchars($phieu['ma_phieu_nhap']); ?></div>
      </div>
      <div>
        <strong style="color:#64748b;font-size:14px;">Ngày nhập:</strong>
        <div style="font-size:16px;font-weight:600;color:#22334b;"><?php echo $phieu['ngay_nhap']; ?></div>
      </div>
      <div>
        <strong style="color:#64748b;font-size:14px;">Nhà cung cấp:</strong>
        <div style="font-size:16px;font-weight:600;color:#22334b;"><?php echo htmlspecialchars($phieu['ten_nhacungcap'] ?? 'Không có'); ?></div>
        <?php if(!empty($phieu['ncc_sdt'])): ?>
          <div style="font-size:14px;color:#64748b;"><?php echo htmlspecialchars($phieu['ncc_sdt']); ?></div>
        <?php endif; ?>
      </div>
      <div>
        <strong style="color:#64748b;font-size:14px;">Nhân viên nhập:</strong>
        <div style="font-size:16px;font-weight:600;color:#22334b;"><?php echo htmlspecialchars($phieu['ten_nhanvien'] ?? 'N/A'); ?></div>
      </div>
      <div>
        <strong style="color:#64748b;font-size:14px;">Trạng thái:</strong>
        <div>
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
          <span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php echo $status_class; ?>">
            <?php echo $status_text; ?>
          </span>
        </div>
      </div>
      <div>
        <strong style="color:#64748b;font-size:14px;">Tổng tiền:</strong>
        <div style="font-size:20px;font-weight:700;color:#28a745;"><?php echo number_format($phieu['tong_tien_nhap'], 0, ',', '.'); ?>₫</div>
      </div>
    </div>
    <?php if(!empty($phieu['ghi_chu'])): ?>
      <div style="margin-top:16px;padding-top:16px;border-top:1px solid #e5e7eb;">
        <strong style="color:#64748b;font-size:14px;">Ghi chú:</strong>
        <div style="color:#22334b;margin-top:4px;"><?php echo nl2br(htmlspecialchars($phieu['ghi_chu'])); ?></div>
      </div>
    <?php endif; ?>
  </div>

  <!-- Chi tiết nguyên liệu -->
  <h3 style="margin-bottom:16px;color:#22334b;">Chi tiết nguyên liệu</h3>
  <div style="overflow-x:auto;">
    <table style="width:100%;border-collapse:collapse;">
      <thead>
        <tr style="background:#eef2ff;">
          <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">STT</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tên nguyên liệu</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Số lượng</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Đơn vị</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Đơn giá</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Thành tiền</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Hạn sử dụng</th>
        </tr>
      </thead>
      <tbody>
      <?php if(!empty($chitiet) && is_array($chitiet)): 
        $stt = 1;
        foreach($chitiet as $item): ?>
        <tr style="border-bottom:1px solid #eef2fa;">
          <td style="padding:12px;"><?php echo $stt++; ?></td>
          <td style="padding:12px;"><?php echo htmlspecialchars($item['ten_nguyenlieu']); ?></td>
          <td style="padding:12px;text-align:right;"><?php echo number_format($item['so_luong_nhap'], 2, ',', '.'); ?></td>
          <td style="padding:12px;"><?php echo htmlspecialchars($item['don_vi_tinh']); ?></td>
          <td style="padding:12px;text-align:right;"><?php echo number_format($item['don_gia_nhap'], 0, ',', '.'); ?>₫</td>
          <td style="padding:12px;text-align:right;font-weight:600;"><?php echo number_format($item['thanh_tien'], 0, ',', '.'); ?>₫</td>
          <td style="padding:12px;"><?php echo $item['han_su_dung'] ? date('d/m/Y', strtotime($item['han_su_dung'])) : '-'; ?></td>
        </tr>
      <?php endforeach; else: ?>
        <tr><td colspan="7" style="padding:20px;text-align:center;color:#999;">Không có chi tiết</td></tr>
      <?php endif; ?>
      </tbody>
      <tfoot>
        <tr style="background:#f8fafc;font-weight:600;">
          <td colspan="5" style="padding:12px;text-align:right;">Tổng cộng:</td>
          <td style="padding:12px;text-align:right;font-size:18px;color:#28a745;"><?php echo number_format($phieu['tong_tien_nhap'], 0, ',', '.'); ?>₫</td>
          <td></td>
        </tr>
      </tfoot>
    </table>
  </div>

  <!-- Actions -->
  <?php if($phieu['trang_thai'] === 'cho_duyet'): ?>
    <div style="margin-top:24px;padding-top:24px;border-top:2px solid #e5e7eb;display:flex;gap:12px;">
      <?php if(isManager()): ?>
        <a href="<?php echo getBaseUrl(); ?>app/kho/approve.php?id=<?php echo $phieu['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn duyệt phiếu nhập kho này? Sau khi duyệt, số lượng tồn kho sẽ được cập nhật.')" style="padding:12px 24px;background:#28a745;color:#fff;text-decoration:none;border-radius:8px;font-weight:600;display:inline-block;">
          <i class="fas fa-check"></i> Duyệt phiếu nhập kho
        </a>
      <?php endif; ?>
      <a href="<?php echo getBaseUrl(); ?>app/kho/cancel.php?id=<?php echo $phieu['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn hủy phiếu nhập kho này?')" style="padding:12px 24px;background:#dc3545;color:#fff;text-decoration:none;border-radius:8px;font-weight:600;display:inline-block;">
        <i class="fas fa-times"></i> Hủy phiếu nhập kho
      </a>
    </div>
  <?php endif; ?>
</div>

