<div class="header-bar">
    <h2><i class="fas fa-warehouse"></i> Quản lý nhập kho</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <?php if(isset($_GET['success'])): ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:#e7f7ed;color:#0c6b3d;">Đã cập nhật thành công!</div>
  <?php endif; ?>
  
  <!-- Tab Navigation -->
  <div style="display:flex;gap:8px;margin-bottom:24px;border-bottom:2px solid #eef2fa;">
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php?tab=phieu" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? 'phieu') === 'phieu' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;">
      <i class="fas fa-file-invoice"></i> Phiếu nhập kho
    </a>
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php?tab=expiry" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? '') === 'expiry' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;position:relative;">
      <i class="fas fa-calendar-times"></i> Hạn sử dụng
      <?php if(isset($expiry_stats) && ($expiry_stats['het_han'] > 0 || $expiry_stats['sap_het_han'] > 0)): ?>
        <span style="position:absolute;top:8px;right:8px;background:#dc3545;color:#fff;border-radius:50%;width:18px;height:18px;display:inline-flex;align-items:center;justify-content:center;font-size:10px;font-weight:bold;">
          <?php echo ($expiry_stats['het_han'] ?? 0) + ($expiry_stats['sap_het_han'] ?? 0); ?>
        </span>
      <?php endif; ?>
    </a>
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php?tab=tonkho" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? '') === 'tonkho' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;position:relative;">
      <i class="fas fa-boxes"></i> Tồn kho
      <?php $total_low = ($low_stock_5 ?? 0) + ($low_stock_10 ?? 0); if($total_low > 0): ?>
        <span style="position:absolute;top:8px;right:8px;background:#dc3545;color:#fff;border-radius:50%;width:18px;height:18px;display:inline-flex;align-items:center;justify-content:center;font-size:10px;font-weight:bold;">
          <?php echo $total_low; ?>
        </span>
      <?php endif; ?>
    </a>
    <a href="<?php echo getBaseUrl(); ?>app/kho/index.php?tab=kiemkho" 
       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid <?php echo ($tab ?? '') === 'kiemkho' ? '#2196f3' : 'transparent'; ?>;margin-bottom:-2px;">
      <i class="fas fa-clipboard-check"></i> Phiếu kiểm kho
    </a>
  </div>

  <?php if(($tab ?? 'phieu') === 'phieu'): ?>
    <!-- Tab: Phiếu nhập kho -->
    <!-- Action Button -->
    <div style="margin-bottom:20px;">
      <a href="<?php echo getBaseUrl(); ?>app/kho/create.php" class="main-link" style="display:inline-block;">
        <i class="fas fa-plus"></i> Tạo phiếu nhập kho mới
      </a>
    </div>

    <!-- Statistics Cards -->
    <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:16px;margin-bottom:24px;">
      <div style="background:#fff3cd;padding:16px;border-radius:8px;border-left:4px solid #ffc107;">
        <div style="font-size:14px;color:#856404;margin-bottom:4px;">Chờ duyệt</div>
        <div style="font-size:24px;font-weight:bold;color:#856404;"><?php echo $stats['cho_duyet'] ?? 0; ?></div>
      </div>
      <div style="background:#d1ecf1;padding:16px;border-radius:8px;border-left:4px solid #17a2b8;">
        <div style="font-size:14px;color:#0c5460;margin-bottom:4px;">Đã duyệt</div>
        <div style="font-size:24px;font-weight:bold;color:#0c5460;"><?php echo $stats['da_duyet'] ?? 0; ?></div>
      </div>
      <div style="background:#f8d7da;padding:16px;border-radius:8px;border-left:4px solid #dc3545;">
        <div style="font-size:14px;color:#721c24;margin-bottom:4px;">Đã hủy</div>
        <div style="font-size:24px;font-weight:bold;color:#721c24;"><?php echo $stats['da_huy'] ?? 0; ?></div>
      </div>
      <div style="background:#d4edda;padding:16px;border-radius:8px;border-left:4px solid #28a745;">
        <div style="font-size:14px;color:#155724;margin-bottom:4px;">Tổng tiền đã duyệt</div>
        <div style="font-size:24px;font-weight:bold;color:#155724;"><?php echo number_format($stats['tong_tien'] ?? 0, 0, ',', '.'); ?>₫</div>
      </div>
    </div>

    <!-- Filters -->
    <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
      <form method="get" style="display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:12px;align-items:center;">
        <input type="hidden" name="tab" value="phieu">
        <select name="status" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
          <option value="all" <?php echo ($status ?? 'all') === 'all' ? 'selected' : ''; ?>>Tất cả trạng thái</option>
          <option value="cho_duyet" <?php echo ($status ?? '') === 'cho_duyet' ? 'selected' : ''; ?>>Chờ duyệt</option>
          <option value="da_duyet" <?php echo ($status ?? '') === 'da_duyet' ? 'selected' : ''; ?>>Đã duyệt</option>
          <option value="da_huy" <?php echo ($status ?? '') === 'da_huy' ? 'selected' : ''; ?>>Đã hủy</option>
        </select>
        <input type="date" name="from" value="<?php echo htmlspecialchars($from ?? '');?>" placeholder="Từ ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
        <input type="date" name="to" value="<?php echo htmlspecialchars($to ?? '');?>" placeholder="Đến ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
        <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
      </form>
    </div>

    <!-- Table -->
    <div style="overflow-x:auto;">
      <table style="width:100%;border-collapse:collapse;">
        <thead>
          <tr style="background:#eef2ff;">
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã phiếu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày nhập</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Nhà cung cấp</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Nhân viên</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Số mặt hàng</th>
            <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Tổng tiền</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Trạng thái</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tác vụ</th>
          </tr>
        </thead>
        <tbody>
        <?php if(!empty($list) && is_array($list)): foreach($list as $row): ?>
          <tr style="border-bottom:1px solid #eef2fa;">
            <td style="padding:12px;"><?php echo htmlspecialchars($row['ma_phieu_nhap']);?></td>
            <td style="padding:12px;"><?php echo $row['ngay_nhap']; ?></td>
            <td style="padding:12px;"><?php echo htmlspecialchars($row['ten_nhacungcap'] ?? 'Không có'); ?></td>
            <td style="padding:12px;"><?php echo htmlspecialchars($row['ten_nhanvien'] ?? 'N/A'); ?></td>
            <td style="padding:12px;"><?php echo $row['so_mat_hang'] ?? 0; ?></td>
            <td style="padding:12px;text-align:right;font-weight:600;"><?php echo number_format($row['tong_tien_nhap'], 0, ',', '.'); ?>₫</td>
            <td style="padding:12px;">
              <?php
              $status_class = '';
              $status_text = '';
              switch($row['trang_thai']) {
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
            </td>
            <td style="padding:12px;">
              <a href="<?php echo getBaseUrl(); ?>app/kho/view.php?id=<?php echo $row['id']; ?>" style="padding:6px 12px;background:#2196f3;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;margin-right:4px;">
                <i class="fas fa-eye"></i> Xem
              </a>
              <?php if($row['trang_thai'] === 'cho_duyet'): ?>
                <?php if(isManager()): ?>
                  <a href="<?php echo getBaseUrl(); ?>app/kho/approve.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn duyệt phiếu nhập kho này?')" style="padding:6px 12px;background:#28a745;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;margin-right:4px;">
                    <i class="fas fa-check"></i> Duyệt
                  </a>
                <?php endif; ?>
                <a href="<?php echo getBaseUrl(); ?>app/kho/cancel.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn hủy phiếu nhập kho này?')" style="padding:6px 12px;background:#dc3545;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;">
                  <i class="fas fa-times"></i> Hủy
                </a>
              <?php endif; ?>
            </td>
          </tr>
        <?php endforeach; else: ?>
          <tr><td colspan="8" style="padding:20px;text-align:center;color:#999;">Chưa có phiếu nhập kho nào</td></tr>
        <?php endif; ?>
        </tbody>
      </table>
    </div>

  <?php elseif(($tab ?? '') === 'tonkho'): ?>
    <!-- Tab: Tồn kho -->
    
    <!-- Filters for Ton kho -->
    <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
      <form method="get" style="display:grid;grid-template-columns:1fr 1fr auto;gap:12px;align-items:center;">
        <input type="hidden" name="tab" value="tonkho">
        <input type="text" name="kw" value="<?php echo htmlspecialchars($kw ?? ''); ?>" placeholder="Tìm kiếm theo tên hoặc mã nguyên liệu..." style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;width:100%;">
        <select name="loai" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
          <option value="">Tất cả danh mục</option>
          <?php foreach(($danhmuc ?? []) as $key => $name): ?>
            <option value="<?php echo $key; ?>" <?php echo ($filter ?? '') === $key ? 'selected' : ''; ?>><?php echo $name; ?></option>
          <?php endforeach; ?>
        </select>
        <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;"><i class="fas fa-search"></i> Tìm kiếm</button>
      </form>
    </div>

    <!-- Ton kho Table -->
    <div style="overflow-x:auto;">
      <table style="width:100%;border-collapse:collapse;background:#fff;">
        <thead>
          <tr style="background:#eef2ff;">
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Mã nguyên liệu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Tên nguyên liệu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Danh mục</th>
            <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Số lượng tồn</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Đơn vị tính</th>
            <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;border-bottom:2px solid #d2d8ea;">Trạng thái</th>
          </tr>
        </thead>
        <tbody>
        <?php if(!empty($tonkho_list) && is_array($tonkho_list)): foreach($tonkho_list as $row): 
          $sl_ton = isset($row['so_luong_ton']) ? (float)$row['so_luong_ton'] : 0;
          
          $status_class = '';
          $status_text = '';
          $row_bg = '';
          
          if ($row['trang_thai'] === 'ngung_ban' || $row['trang_thai'] === 'ngung_nhap') {
            $status_class = 'background:#e2e3e5;color:#383d41;';
            $status_text = 'Ngừng nhập';
            $row_bg = 'background:#f8f9fa;';
          } elseif ($sl_ton <= 0) {
            $status_class = 'background:#f8d7da;color:#721c24;';
            $status_text = 'Hết hàng';
            $row_bg = 'background:#fff5f5;';
          } elseif ($sl_ton < 5) {
            $status_class = 'background:#f8d7da;color:#721c24;';
            $status_text = 'Sắp hết (<5)';
            $row_bg = 'background:#fff5f5;';
          } elseif ($sl_ton < 10) {
            $status_class = 'background:#fff3cd;color:#856404;';
            $status_text = 'Sắp hết (<10)';
            $row_bg = 'background:#fffbf0;';
          } else {
            $status_class = 'background:#d4edda;color:#155724;';
            $status_text = 'Còn hàng';
          }
        ?>
          <tr style="border-bottom:1px solid #eef2fa;<?php echo $row_bg; ?>;transition:background-color 0.2s;" onmouseover="this.style.backgroundColor='#f1f5f9';" onmouseout="this.style.backgroundColor='<?php echo $row_bg ? str_replace('background:', '', $row_bg) : 'transparent'; ?>';">
            <td style="padding:12px;"><?php echo htmlspecialchars($row['ma_nguyenlieu'] ?? ''); ?></td>
            <td style="padding:12px;font-weight:600;color:#1e293b;"><?php echo htmlspecialchars($row['ten_nguyenlieu'] ?? ''); ?></td>
            <td style="padding:12px;">
              <?php echo htmlspecialchars($danhmuc[$row['danhmuc_nl']] ?? $row['danhmuc_nl']); ?>
            </td>
            <td style="padding:12px;text-align:right;font-size:16px;font-weight:bold;color:<?php echo $sl_ton <= 0 ? '#dc3545' : ($sl_ton < 5 ? '#dc3545' : ($sl_ton < 10 ? '#ff9800' : '#28a745')); ?>;">
              <?php echo number_format($sl_ton, 2, ',', '.'); ?>
            </td>
            <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($row['don_vi_tinh'] ?? ''); ?></td>
            <td style="padding:12px;text-align:center;">
              <span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:12px;font-weight:600;<?php echo $status_class; ?>">
                <?php echo $status_text; ?>
              </span>
            </td>
          </tr>
        <?php endforeach; else: ?>
          <tr><td colspan="6" style="padding:32px;text-align:center;color:#64748b;font-size:15px;">
            <i class="fas fa-box-open" style="font-size:32px;color:#cbd5e1;display:block;margin-bottom:12px;"></i>
            Không tìm thấy nguyên vật liệu nào
          </td></tr>
        <?php endif; ?>
        </tbody>
      </table>
    </div>

  <?php elseif(($tab ?? '') === 'kiemkho'): ?>
    <!-- Tab: Phiếu kiểm kho -->
    <div style="margin-bottom:20px;">
      <a href="<?php echo getBaseUrl(); ?>app/kho/create_kiemkho.php" class="main-link" style="display:inline-block;">
        <i class="fas fa-plus"></i> Tạo phiếu kiểm kho mới
      </a>
    </div>

    <!-- Statistics Cards -->
    <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:16px;margin-bottom:24px;">
      <div style="background:#fff3cd;padding:16px;border-radius:8px;border-left:4px solid #ffc107;">
        <div style="font-size:14px;color:#856404;margin-bottom:4px;">Chờ duyệt</div>
        <div style="font-size:24px;font-weight:bold;color:#856404;"><?php echo $stats['cho_duyet'] ?? 0; ?></div>
      </div>
      <div style="background:#d1ecf1;padding:16px;border-radius:8px;border-left:4px solid #17a2b8;">
        <div style="font-size:14px;color:#0c5460;margin-bottom:4px;">Đã duyệt</div>
        <div style="font-size:24px;font-weight:bold;color:#0c5460;"><?php echo $stats['da_duyet'] ?? 0; ?></div>
      </div>
      <div style="background:#f8d7da;padding:16px;border-radius:8px;border-left:4px solid #dc3545;">
        <div style="font-size:14px;color:#721c24;margin-bottom:4px;">Đã hủy</div>
        <div style="font-size:24px;font-weight:bold;color:#721c24;"><?php echo $stats['da_huy'] ?? 0; ?></div>
      </div>
    </div>

    <!-- Filters -->
    <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
      <form method="get" style="display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:12px;align-items:center;">
        <input type="hidden" name="tab" value="kiemkho">
        <select name="status" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
          <option value="all" <?php echo ($status ?? 'all') === 'all' ? 'selected' : ''; ?>>Tất cả trạng thái</option>
          <option value="cho_duyet" <?php echo ($status ?? '') === 'cho_duyet' ? 'selected' : ''; ?>>Chờ duyệt</option>
          <option value="da_duyet" <?php echo ($status ?? '') === 'da_duyet' ? 'selected' : ''; ?>>Đã duyệt</option>
          <option value="da_huy" <?php echo ($status ?? '') === 'da_huy' ? 'selected' : ''; ?>>Đã hủy</option>
        </select>
        <input type="date" name="from" value="<?php echo htmlspecialchars($from ?? '');?>" placeholder="Từ ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
        <input type="date" name="to" value="<?php echo htmlspecialchars($to ?? '');?>" placeholder="Đến ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
        <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
      </form>
    </div>

    <!-- Table -->
    <div style="overflow-x:auto;">
      <table style="width:100%;border-collapse:collapse;">
        <thead>
          <tr style="background:#eef2ff;">
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã phiếu</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Ngày kiểm</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Nhân viên</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Trạng thái</th>
            <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tác vụ</th>
          </tr>
        </thead>
        <tbody>
        <?php if(!empty($kiemkho_list) && is_array($kiemkho_list)): foreach($kiemkho_list as $row): ?>
          <tr style="border-bottom:1px solid #eef2fa;">
            <td style="padding:12px;font-weight:bold;"><?php echo htmlspecialchars($row['ma_phieu_kiem']);?></td>
            <td style="padding:12px;"><?php echo date('d/m/Y', strtotime($row['ngay_kiem'])); ?></td>
            <td style="padding:12px;"><?php echo htmlspecialchars($row['ten_nhanvien'] ?? 'N/A'); ?></td>
            <td style="padding:12px;">
              <?php
              $status_class = '';
              $status_text = '';
              switch($row['trang_thai']) {
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
            </td>
            <td style="padding:12px;">
              <a href="<?php echo getBaseUrl(); ?>app/kho/view_kiemkho.php?id=<?php echo $row['id']; ?>" style="padding:6px 12px;background:#2196f3;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;margin-right:4px;">
                <i class="fas fa-eye"></i> Xem
              </a>
              <?php if($row['trang_thai'] === 'cho_duyet'): ?>
                <?php if(isManager()): ?>
                  <a href="<?php echo getBaseUrl(); ?>app/kho/approve_kiemkho.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn duyệt phiếu kiểm kho này? Số lượng tồn kho sẽ bị ghi đè thành số lượng thực tế trong phiếu!')" style="padding:6px 12px;background:#28a745;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;margin-right:4px;">
                    <i class="fas fa-check"></i> Duyệt
                  </a>
                <?php endif; ?>
                <a href="<?php echo getBaseUrl(); ?>app/kho/cancel_kiemkho.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc chắn muốn hủy phiếu kiểm kho này?')" style="padding:6px 12px;background:#dc3545;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;">
                  <i class="fas fa-times"></i> Hủy
                </a>
              <?php endif; ?>
            </td>
          </tr>
        <?php endforeach; else: ?>
          <tr><td colspan="5" style="padding:20px;text-align:center;color:#999;">Chưa có phiếu kiểm kho nào</td></tr>
        <?php endif; ?>
        </tbody>
      </table>
    </div>

  <?php else: ?>
    <!-- Tab: Hạn sử dụng -->
    <?php if(!($table_exists ?? false)): ?>
      <div style="padding:24px;text-align:center;background:#fff3cd;border-radius:8px;color:#856404;">
        <i class="fas fa-exclamation-triangle" style="font-size:48px;margin-bottom:16px;"></i>
        <h3 style="margin:0 0 8px 0;">Chưa có dữ liệu hạn sử dụng</h3>
        <p style="margin:0;">Vui lòng chạy migration để tạo bảng <code>kho_lot</code> trước khi sử dụng tính năng này.</p>
        <p style="margin:8px 0 0 0;font-size:14px;">File: <code>database/migrations/add_expiry_tables.sql</code></p>
      </div>
    <?php else: ?>
      <!-- Statistics Cards for Expiry -->
      <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:16px;margin-bottom:24px;">
        <div style="background:#fff3cd;padding:16px;border-radius:8px;border-left:4px solid #ffc107;">
          <div style="font-size:14px;color:#856404;margin-bottom:4px;">Sắp hết hạn (≤7 ngày)</div>
          <div style="font-size:24px;font-weight:bold;color:#856404;"><?php echo $expiry_stats['sap_het_han'] ?? 0; ?></div>
        </div>
        <div style="background:#f8d7da;padding:16px;border-radius:8px;border-left:4px solid #dc3545;">
          <div style="font-size:14px;color:#721c24;margin-bottom:4px;">Đã hết hạn</div>
          <div style="font-size:24px;font-weight:bold;color:#721c24;"><?php echo $expiry_stats['het_han'] ?? 0; ?></div>
        </div>
        <div style="background:#d1ecf1;padding:16px;border-radius:8px;border-left:4px solid #17a2b8;">
          <div style="font-size:14px;color:#0c5460;margin-bottom:4px;">Đã hủy</div>
          <div style="font-size:24px;font-weight:bold;color:#0c5460;"><?php echo $expiry_stats['da_huy'] ?? 0; ?></div>
        </div>
      </div>

      <!-- Filters for Expiry -->
      <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
        <form method="get" style="display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:12px;align-items:center;">
          <input type="hidden" name="tab" value="expiry">
          <select name="expiry_filter" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
            <option value="all" <?php echo ($expiry_filter ?? 'all') === 'all' ? 'selected' : ''; ?>>Tất cả</option>
            <option value="sap_het_han" <?php echo ($expiry_filter ?? '') === 'sap_het_han' ? 'selected' : ''; ?>>Sắp hết hạn (≤7 ngày)</option>
            <option value="het_han" <?php echo ($expiry_filter ?? '') === 'het_han' ? 'selected' : ''; ?>>Đã hết hạn</option>
            <option value="da_huy" <?php echo ($expiry_filter ?? '') === 'da_huy' ? 'selected' : ''; ?>>Đã hủy</option>
          </select>
          <input type="date" name="from" value="<?php echo htmlspecialchars($from ?? '');?>" placeholder="Từ ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
          <input type="date" name="to" value="<?php echo htmlspecialchars($to ?? '');?>" placeholder="Đến ngày" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
          <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
        </form>
      </div>

      <!-- Expiry Table -->
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;">
          <thead>
            <tr style="background:#eef2ff;">
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã nguyên liệu</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tên nguyên liệu</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Mã phiếu nhập</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Số lượng đã nhập</th>
              <th style="padding:12px;text-align:right;font-weight:600;color:#22334b;">Số lượng còn lại</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Đơn vị</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Hạn sử dụng</th>
              <th style="padding:12px;text-align:center;font-weight:600;color:#22334b;">Số ngày còn lại</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Trạng thái</th>
              <th style="padding:12px;text-align:left;font-weight:600;color:#22334b;">Tác vụ</th>
            </tr>
          </thead>
          <tbody>
          <?php if(!empty($expiry_list) && is_array($expiry_list)): foreach($expiry_list as $row): ?>
            <?php
            $so_ngay = (int)($row['so_ngay_con_lai'] ?? 0);
            $status_class = '';
            $status_text = '';
            $row_bg = '';
            
            if ($row['trang_thai'] === 'da_huy') {
              $status_class = 'background:#d1ecf1;color:#0c5460;';
              $status_text = 'Đã hủy';
              $row_bg = 'background:#f8f9fa;';
            } elseif ($so_ngay < 0) {
              $status_class = 'background:#f8d7da;color:#721c24;';
              $status_text = 'Đã hết hạn';
              $row_bg = 'background:#fff5f5;';
            } elseif ($so_ngay <= 7) {
              $status_class = 'background:#fff3cd;color:#856404;';
              $status_text = 'Sắp hết hạn';
              $row_bg = 'background:#fffbf0;';
            } else {
              $status_class = 'background:#d4edda;color:#155724;';
              $status_text = 'Còn hạn';
            }
            ?>
            <tr style="border-bottom:1px solid #eef2fa;<?php echo $row_bg; ?>">
              <td style="padding:12px;"><?php echo htmlspecialchars($row['ma_nguyenlieu'] ?? ''); ?></td>
              <td style="padding:12px;font-weight:600;"><?php echo htmlspecialchars($row['ten_nguyenlieu'] ?? ''); ?></td>
              <td style="padding:12px;"><?php echo htmlspecialchars($row['ma_phieu_nhap'] ?? ''); ?></td>
              <td style="padding:12px;text-align:right;color:#22334b;">
                <?php echo number_format($row['so_luong_nhap'] ?? 0, 2, ',', '.'); ?>
              </td>
              <td style="padding:12px;text-align:right;font-weight:600;color:<?php echo ($row['so_luong_con_lai'] ?? 0) < ($row['so_luong_nhap'] ?? 0) ? '#ff9800' : '#28a745'; ?>;">
                <?php echo number_format($row['so_luong_con_lai'] ?? 0, 2, ',', '.'); ?>
                <?php if(($row['so_luong_con_lai'] ?? 0) < ($row['so_luong_nhap'] ?? 0)): ?>
                  <span style="font-size:11px;color:#999;display:block;margin-top:2px;">
                    (Đã dùng: <?php echo number_format(($row['so_luong_nhap'] ?? 0) - ($row['so_luong_con_lai'] ?? 0), 2, ',', '.'); ?>)
                  </span>
                <?php endif; ?>
              </td>
              <td style="padding:12px;"><?php echo htmlspecialchars($row['don_vi_tinh'] ?? ''); ?></td>
              <td style="padding:12px;"><?php echo date('d/m/Y', strtotime($row['han_su_dung'])); ?></td>
              <td style="padding:12px;text-align:center;font-weight:600;color:<?php echo $so_ngay < 0 ? '#dc3545' : ($so_ngay <= 7 ? '#ffc107' : '#28a745'); ?>;">
                <?php 
                if ($so_ngay < 0) {
                  echo '<span style="color:#dc3545;">Đã hết ' . abs($so_ngay) . ' ngày</span>';
                } else {
                  echo $so_ngay . ' ngày';
                }
                ?>
              </td>
              <td style="padding:12px;">
                <span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php echo $status_class; ?>">
                  <?php echo $status_text; ?>
                </span>
              </td>
              <td style="padding:12px;">
                <?php if($row['trang_thai'] !== 'da_huy' && $so_ngay < 0): ?>
                  <button onclick="cancelExpiredLot(<?php echo $row['id']; ?>, '<?php echo htmlspecialchars($row['ten_nguyenlieu'], ENT_QUOTES); ?>')" 
                          style="padding:6px 12px;background:#dc3545;color:#fff;border:none;border-radius:4px;font-size:13px;cursor:pointer;">
                    <i class="fas fa-trash"></i> Hủy
                  </button>
                <?php elseif($row['trang_thai'] === 'da_huy'): ?>
                  <span style="color:#999;font-size:13px;">Đã hủy</span>
                <?php else: ?>
                  <span style="color:#999;font-size:13px;">-</span>
                <?php endif; ?>
              </td>
            </tr>
          <?php endforeach; else: ?>
            <tr><td colspan="10" style="padding:20px;text-align:center;color:#999;">Chưa có dữ liệu hạn sử dụng</td></tr>
          <?php endif; ?>
          </tbody>
        </table>
      </div>
    <?php endif; ?>
  <?php endif; ?>
</div>

<script>
function cancelExpiredLot(lotId, tenNguyenLieu) {
  if (!confirm('Bạn có chắc chắn muốn hủy nguyên liệu "' + tenNguyenLieu + '" đã hết hạn?\n\nSố lượng sẽ bị trừ khỏi tồn kho.')) {
    return;
  }
  
  var lyDo = prompt('Nhập lý do hủy (tùy chọn):', 'Hết hạn sử dụng');
  if (lyDo === null) {
    return; // User cancelled
  }
  
  fetch('<?php echo getBaseUrl(); ?>app/kho/expiry_cancel.php', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      lot_id: lotId,
      ly_do: lyDo || 'Hết hạn sử dụng'
    })
  })
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      alert('Đã hủy nguyên liệu thành công!');
      location.reload();
    } else {
      alert('Lỗi: ' + (data.error || 'Không thể hủy nguyên liệu'));
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Có lỗi xảy ra khi hủy nguyên liệu');
  });
}
</script>
