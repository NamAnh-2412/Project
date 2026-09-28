<div class="header-bar">
    <h2><i class="fas fa-plus-circle"></i> Tạo phiếu nhập kho</h2>
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

  <div style="display:grid;grid-template-columns:1fr 400px;gap:24px;">
    <!-- Left: Danh sách nguyên liệu -->
    <div>
      <h3 style="margin-bottom:16px;color:#22334b;">Danh sách nguyên liệu</h3>
      <div style="margin-bottom:16px;">
        <input type="text" id="searchMaterial" placeholder="Tìm nguyên liệu..." style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;">
      </div>
      <div style="max-height:600px;overflow-y:auto;border:1px solid #e5e7eb;border-radius:8px;padding:8px;">
        <table style="width:100%;border-collapse:collapse;">
          <thead style="position:sticky;top:0;background:#eef2ff;z-index:10;">
            <tr>
              <th style="padding:10px;text-align:left;font-weight:600;color:#22334b;">Tên nguyên liệu</th>
              <th style="padding:10px;text-align:left;font-weight:600;color:#22334b;">Đơn vị</th>
              <th style="padding:10px;text-align:right;font-weight:600;color:#22334b;">SL tồn</th>
              <th style="padding:10px;text-align:center;font-weight:600;color:#22334b;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
          <?php if(!empty($list) && is_array($list)): foreach($list as $item): ?>
            <tr class="material-row" data-name="<?php echo strtolower(htmlspecialchars($item['ten_nguyenlieu'])); ?>">
              <td style="padding:10px;"><?php echo htmlspecialchars($item['ten_nguyenlieu']); ?></td>
              <td style="padding:10px;"><?php echo htmlspecialchars($item['don_vi_tinh']); ?></td>
              <td style="padding:10px;text-align:right;"><?php echo number_format($item['so_luong_ton'], 2, ',', '.'); ?></td>
              <td style="padding:10px;text-align:center;">
                <button type="button" class="btn-add-material" data-id="<?php echo $item['id']; ?>" data-name="<?php echo htmlspecialchars($item['ten_nguyenlieu']); ?>" data-donvi="<?php echo htmlspecialchars($item['don_vi_tinh']); ?>" data-gianhap="<?php echo $item['gia_nhap'] ?? 0; ?>" style="padding:6px 12px;background:#28a745;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:13px;">
                  <i class="fas fa-plus"></i> Thêm
                </button>
              </td>
            </tr>
          <?php endforeach; else: ?>
            <tr><td colspan="4" style="padding:20px;text-align:center;color:#999;">Không có nguyên liệu</td></tr>
          <?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Right: Giỏ hàng và form -->
    <div>
      <h3 style="margin-bottom:16px;color:#22334b;">Giỏ hàng nhập kho</h3>
      
      <!-- Form thông tin phiếu -->
      <div style="background:#f8fafc;padding:16px;border-radius:8px;margin-bottom:16px;">
        <div style="margin-bottom:12px;">
          <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Nhà cung cấp (tùy chọn)</label>
          <select id="nha_cung_cap_id" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
            <option value="">-- Chọn nhà cung cấp --</option>
            <?php if(!empty($suppliers) && is_array($suppliers)): foreach($suppliers as $sup): ?>
              <option value="<?php echo $sup['id']; ?>"><?php echo htmlspecialchars($sup['ten_nhacungcap']); ?></option>
            <?php endforeach; endif; ?>
          </select>
        </div>
        <div style="margin-bottom:12px;">
          <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Ghi chú (tùy chọn)</label>
          <textarea id="ghi_chu" rows="3" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;resize:vertical;" placeholder="Nhập ghi chú..."></textarea>
        </div>
      </div>

      <!-- Giỏ hàng -->
      <div id="cart-container" style="background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;max-height:400px;overflow-y:auto;margin-bottom:16px;">
        <div id="cart-items">
          <?php if(!empty($cart) && is_array($cart)): ?>
            <?php foreach($cart as $item): ?>
              <div class="cart-item" data-id="<?php echo $item['id']; ?>" style="display:flex;justify-content:space-between;align-items:center;padding:12px;border-bottom:1px solid #e5e7eb;margin-bottom:8px;">
                <div style="flex:1;">
                  <div style="font-weight:600;color:#22334b;margin-bottom:4px;"><?php echo htmlspecialchars($item['name']); ?></div>
                  <div style="font-size:13px;color:#64748b;">
                    SL: <?php echo number_format($item['qty'], 2, ',', '.'); ?> <?php echo htmlspecialchars($item['don_vi']); ?>
                    <?php if(isset($item['don_gia']) && $item['don_gia'] > 0): ?>
                      | Giá: <?php echo number_format($item['don_gia'], 0, ',', '.'); ?>₫
                    <?php endif; ?>
                  </div>
                </div>
                <button type="button" class="btn-remove-item" data-id="<?php echo $item['id']; ?>" style="padding:6px 10px;background:#dc3545;color:#fff;border:none;border-radius:4px;cursor:pointer;">
                  <i class="fas fa-times"></i>
                </button>
              </div>
            <?php endforeach; ?>
          <?php else: ?>
            <div style="text-align:center;padding:40px;color:#999;">Giỏ hàng trống</div>
          <?php endif; ?>
        </div>
      </div>

      <!-- Tổng tiền và nút -->
      <div style="background:#f8fafc;padding:16px;border-radius:8px;margin-bottom:16px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <span style="font-weight:600;color:#22334b;">Tổng tiền:</span>
          <span id="total-amount" style="font-size:20px;font-weight:700;color:#28a745;">0₫</span>
        </div>
        <button type="button" id="btn-commit" style="width:100%;padding:12px;background:#28a745;color:#fff;border:none;border-radius:8px;font-weight:600;cursor:pointer;font-size:16px;" <?php echo empty($cart) ? 'disabled' : ''; ?>>
          <i class="fas fa-check"></i> Tạo phiếu nhập kho
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Modal thêm nguyên liệu -->
<div id="addMaterialModal" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center;">
  <div style="background:#fff;border-radius:12px;padding:24px;min-width:400px;max-width:90%;">
    <h3 style="margin-top:0;margin-bottom:16px;color:#22334b;">Thêm nguyên liệu vào giỏ</h3>
    <form id="addMaterialForm">
      <input type="hidden" id="modal-material-id">
      <div style="margin-bottom:12px;">
        <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Tên nguyên liệu</label>
        <input type="text" id="modal-material-name" readonly style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#f8fafc;color:#22334b;">
      </div>
      <div style="margin-bottom:12px;">
        <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Số lượng *</label>
        <input type="number" id="modal-material-qty" step="0.01" min="0.01" required style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
      </div>
      <div style="margin-bottom:12px;">
        <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Đơn giá (₫)</label>
        <input type="number" id="modal-material-price" step="0.01" min="0" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
      </div>
      <div style="margin-bottom:16px;">
        <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Hạn sử dụng (tùy chọn)</label>
        <input type="date" id="modal-material-expiry" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
      </div>
      <div style="display:flex;gap:12px;">
        <button type="button" id="btn-cancel-modal" style="flex:1;padding:10px;background:#e5e7eb;color:#22334b;border:none;border-radius:8px;font-weight:600;cursor:pointer;">Hủy</button>
        <button type="submit" style="flex:1;padding:10px;background:#28a745;color:#fff;border:none;border-radius:8px;font-weight:600;cursor:pointer;">Thêm vào giỏ</button>
      </div>
    </form>
  </div>
</div>

<script>
// Base path for API calls
var basePath = window.APP_BASE_PATH || '<?php echo getBaseUrl(); ?>';
</script>
<script src="<?php echo getBaseUrl(); ?>js/inventory/create.js"></script>

