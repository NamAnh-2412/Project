<div class="header-bar">
    <h2><i class="fas fa-truck"></i> Chi tiết nhà cung cấp</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <!-- Thông tin nhà cung cấp -->
  <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
      <i class="fas fa-info-circle" style="color:#2196f3;"></i>
      Thông tin nhà cung cấp
    </h3>
    <table style="width:100%;border-collapse:collapse;">
      <tr>
        <td style="padding:10px;width:150px;font-weight:600;color:#64748b;">Tên:</td>
        <td style="padding:10px;color:#22334b;font-weight:600;"><?php echo htmlspecialchars($supplier['ten_nhacungcap']); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">SĐT:</td>
        <td style="padding:10px;color:#22334b;"><?php echo htmlspecialchars($supplier['so_dien_thoai'] ?? '-'); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Email:</td>
        <td style="padding:10px;color:#22334b;"><?php echo htmlspecialchars($supplier['email'] ?? '-'); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Địa chỉ:</td>
        <td style="padding:10px;color:#22334b;"><?php echo htmlspecialchars($supplier['dia_chi'] ?? '-'); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Mã số thuế:</td>
        <td style="padding:10px;color:#22334b;"><?php echo htmlspecialchars($supplier['ma_so_thue'] ?? '-'); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Người liên hệ:</td>
        <td style="padding:10px;color:#22334b;"><?php echo htmlspecialchars($supplier['nguoi_lien_he'] ?? '-'); ?></td>
      </tr>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Trạng thái:</td>
        <td style="padding:10px;">
          <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php echo $supplier['trang_thai'] === 'dang_hop_tac' ? 'background:#d4edda;color:#155724;' : 'background:#f8d7da;color:#721c24;'; ?>">
            <?php echo $supplier['trang_thai'] === 'dang_hop_tac' ? 'Đang hợp tác' : 'Ngừng hợp tác'; ?>
          </span>
        </td>
      </tr>
      <?php if (!empty($supplier['ghi_chu'])): ?>
      <tr>
        <td style="padding:10px;font-weight:600;color:#64748b;">Ghi chú:</td>
        <td style="padding:10px;color:#22334b;"><?php echo nl2br(htmlspecialchars($supplier['ghi_chu'])); ?></td>
      </tr>
      <?php endif; ?>
    </table>
    <div style="margin-top:15px;padding-top:15px;border-top:1px solid #e0e0e0;">
      <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/edit.php?id=<?php echo $supplier['id']; ?>" class="btn btn-primary" style="text-decoration:none;display:inline-block;padding:8px 16px;background:#2196f3;color:#fff;border-radius:6px;margin-right:8px;">Sửa</a>
      <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/delete.php?id=<?php echo $supplier['id']; ?>" class="btn btn-danger" style="text-decoration:none;display:inline-block;padding:8px 16px;background:#dc3545;color:#fff;border-radius:6px;" onclick="return confirm('Xóa nhà cung cấp?')">Xóa</a>
    </div>
  </div>
  
  <!-- Nguyên vật liệu -->
  <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
      <i class="fas fa-box" style="color:#28a745;"></i>
      Nguyên vật liệu cung cấp (<?php echo $stats['total_materials']; ?>)
    </h3>
    <?php if (empty($materials)): ?>
      <p style="color:#64748b;padding:20px;text-align:center;">Chưa có nguyên vật liệu nào được gán cho nhà cung cấp này.</p>
      <p style="color:#64748b;padding:0 20px 20px;text-align:center;font-size:14px;">
        Bạn có thể gán nhà cung cấp khi <a href="<?php echo getBaseUrl(); ?>app/sanpham/nguyenlieu.php">thêm/sửa nguyên liệu</a>.
      </p>
    <?php else: ?>
      <table style="width:100%;border-collapse:collapse;margin-top:15px;">
        <thead>
          <tr style="background:#f7fafc;border-bottom:2px solid #e0e0e0;">
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tên nguyên liệu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Đơn vị</th>
            <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Giá nhập</th>
            <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;">Trạng thái</th>
            <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;">Tác vụ</th>
          </tr>
        </thead>
        <tbody>
          <?php foreach($materials as $mat): ?>
            <tr style="border-bottom:1px solid #f0f0f0;">
              <td style="padding:12px;color:#22334b;font-weight:600;"><?php echo htmlspecialchars($mat['ten_nguyenlieu']); ?></td>
              <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($mat['ma_nguyenlieu']); ?></td>
              <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($mat['don_vi_tinh']); ?></td>
              <td style="padding:12px;text-align:right;color:#28a745;font-weight:600;"><?php echo $mat['gia_nhap'] ? number_format($mat['gia_nhap'], 0, ',', '.') . '₫' : '-'; ?></td>
              <td style="padding:12px;text-align:center;">
                <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php echo $mat['trang_thai'] === 'con_hang' ? 'background:#d4edda;color:#155724;' : ($mat['trang_thai'] === 'het_hang' ? 'background:#fff3cd;color:#856404;' : 'background:#f8d7da;color:#721c24;'); ?>">
                  <?php 
                  echo $mat['trang_thai'] === 'con_hang' ? 'Còn hàng' : 
                       ($mat['trang_thai'] === 'het_hang' ? 'Hết hàng' : 'Ngừng nhập');
                  ?>
                </span>
              </td>
              <td style="padding:12px;text-align:center;">
                <a href="<?php echo getBaseUrl(); ?>app/sanpham/sua_nguyenlieu.php?id=<?php echo $mat['id']; ?>" style="color:#2196f3;text-decoration:none;">Sửa</a>
              </td>
            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    <?php endif; ?>
  </div>
  
  <!-- Lịch sử phiếu nhập -->
  <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
      <i class="fas fa-history" style="color:#ff9800;"></i>
      Lịch sử phiếu nhập (<?php echo $stats['total_imports']; ?>)
    </h3>
    <?php if (empty($importHistory)): ?>
      <p style="color:#64748b;padding:20px;text-align:center;">Chưa có phiếu nhập nào từ nhà cung cấp này.</p>
    <?php else: ?>
      <table style="width:100%;border-collapse:collapse;margin-top:15px;">
        <thead>
          <tr style="background:#f7fafc;border-bottom:2px solid #e0e0e0;">
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã phiếu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày nhập</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Nhân viên</th>
            <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tổng tiền</th>
            <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;">Trạng thái</th>
            <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;">Tác vụ</th>
          </tr>
        </thead>
        <tbody>
          <?php foreach($importHistory as $imp): ?>
            <tr style="border-bottom:1px solid #f0f0f0;">
              <td style="padding:12px;color:#22334b;font-weight:600;"><?php echo htmlspecialchars($imp['ma_phieu_nhap']); ?></td>
              <td style="padding:12px;color:#64748b;"><?php echo date('d/m/Y', strtotime($imp['ngay_nhap'])); ?></td>
              <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($imp['ten_nhanvien'] ?? '-'); ?></td>
              <td style="padding:12px;text-align:right;color:#28a745;font-weight:600;"><?php echo number_format($imp['tong_tien_nhap'], 0, ',', '.'); ?>₫</td>
              <td style="padding:12px;text-align:center;">
                <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php 
                  echo $imp['trang_thai'] === 'da_duyet' ? 'background:#d4edda;color:#155724;' : 
                       ($imp['trang_thai'] === 'cho_duyet' ? 'background:#fff3cd;color:#856404;' : 'background:#f8d7da;color:#721c24;'); 
                ?>">
                  <?php 
                  echo $imp['trang_thai'] === 'da_duyet' ? 'Đã duyệt' : 
                       ($imp['trang_thai'] === 'cho_duyet' ? 'Chờ duyệt' : 'Đã hủy');
                  ?>
                </span>
              </td>
              <td style="padding:12px;text-align:center;">
                <a href="<?php echo getBaseUrl(); ?>app/kho/view.php?id=<?php echo $imp['id']; ?>" style="color:#2196f3;text-decoration:none;">Xem</a>
              </td>
            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    <?php endif; ?>
  </div>
  
  <!-- Thống kê -->
  <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
      <i class="fas fa-chart-bar" style="color:#9c27b0;"></i>
      Thống kê
    </h3>
    <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:15px;margin-top:15px;">
      <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #2196f3;">
        <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng số nguyên liệu</div>
        <div style="font-size:24px;font-weight:700;color:#2196f3;"><?php echo $stats['total_materials']; ?></div>
      </div>
      <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #28a745;">
        <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng số phiếu nhập</div>
        <div style="font-size:24px;font-weight:700;color:#28a745;"><?php echo $stats['total_imports']; ?></div>
      </div>
      <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #ff9800;">
        <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng giá trị nhập</div>
        <div style="font-size:24px;font-weight:700;color:#ff9800;"><?php echo number_format($stats['total_value'], 0, ',', '.'); ?>₫</div>
      </div>
    </div>
  </div>
  
  <div style="margin-top:20px;">
    <a href="index.php" class="btn btn-secondary" style="text-decoration:none;display:inline-block;padding:10px 20px;background:#64748b;color:#fff;border-radius:6px;">
      <i class="fas fa-arrow-left"></i> Về danh sách
    </a>
  </div>
</div>

