<div class="header-bar">
    <h2><i class="fas fa-clipboard-check"></i> Tạo phiếu kiểm kho</h2>
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
    <a href="?reset=1" style="padding:8px 16px;background:#e2e8f0;color:#334155;text-decoration:none;border-radius:6px;font-weight:600;font-size:14px;">
      <i class="fas fa-sync-alt"></i> Làm mới danh sách
    </a>
  </div>

  <div style="background:#f8fafc;padding:16px;border-radius:8px;margin-bottom:20px;">
    <h3 style="margin-top:0;margin-bottom:12px;color:#1e293b;">Thông tin phiếu kiểm</h3>
    <div style="margin-bottom:12px;">
      <label style="display:block;margin-bottom:6px;font-weight:600;color:#334155;font-size:14px;">Ghi chú (tùy chọn)</label>
      <textarea id="ghi_chu" rows="2" style="width:100%;padding:10px 12px;border:1px solid #cbd5e1;border-radius:6px;background:#fff;color:#1e293b;resize:vertical;" placeholder="Ví dụ: Kiểm kho cuối tháng 6..."></textarea>
    </div>
  </div>

  <h3 style="margin-bottom:12px;color:#1e293b;">Chi tiết kiểm kê</h3>
  <p style="color:#64748b;margin-bottom:16px;font-size:14px;">Hệ thống đã tự động lấy toàn bộ mặt hàng. Bạn chỉ cần nhập số lượng kiểm đếm thực tế vào cột <b>"Tồn thực tế"</b>.</p>

  <div style="overflow-x:auto;border:1px solid #e2e8f0;border-radius:8px;max-height:600px;">
    <table style="width:100%;border-collapse:collapse;background:#fff;">
      <thead style="position:sticky;top:0;background:#f1f5f9;z-index:10;box-shadow:0 1px 2px rgba(0,0,0,0.05);">
        <tr>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;">Mã NVL</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;">Tên nguyên vật liệu</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;">Đơn vị</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;">Tồn hệ thống</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#0f172a;border-bottom:1px solid #cbd5e1;width:150px;">Tồn thực tế</th>
          <th style="padding:12px;text-align:right;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;">Chênh lệch</th>
          <th style="padding:12px;text-align:left;font-weight:600;color:#334155;border-bottom:1px solid #cbd5e1;width:250px;">Lý do (nếu có)</th>
        </tr>
      </thead>
      <tbody>
      <?php if(!empty($cart) && is_array($cart)): foreach($cart as $item): 
        $sys = $item['ton_he_thong'];
        $act = $item['ton_thuc_te'];
        $diff = $act - $sys;
      ?>
        <tr class="kiemkho-row" data-id="<?php echo $item['id']; ?>" style="border-bottom:1px solid #e2e8f0;">
          <td style="padding:12px;color:#64748b;"><?php echo $item['id']; ?></td>
          <td style="padding:12px;font-weight:600;color:#1e293b;"><?php echo htmlspecialchars($item['name']); ?></td>
          <td style="padding:12px;color:#64748b;"><?php echo htmlspecialchars($item['don_vi']); ?></td>
          <td style="padding:12px;text-align:right;color:#64748b;" class="sys-qty" data-val="<?php echo $sys; ?>">
            <?php echo number_format($sys, 2, ',', '.'); ?>
          </td>
          <td style="padding:8px;text-align:right;">
            <input type="number" class="act-qty" value="<?php echo $act; ?>" step="0.01" min="0" style="width:100%;padding:8px;border:1px solid #94a3b8;border-radius:4px;text-align:right;font-weight:bold;color:#0f172a;background:#f8fafc;">
          </td>
          <td style="padding:12px;text-align:right;font-weight:bold;" class="diff-qty">
            <span style="color:<?php echo $diff < 0 ? '#dc3545' : ($diff > 0 ? '#28a745' : '#94a3b8'); ?>">
              <?php echo ($diff > 0 ? '+' : '') . number_format($diff, 2, ',', '.'); ?>
            </span>
          </td>
          <td style="padding:8px;">
            <input type="text" class="reason" value="<?php echo htmlspecialchars($item['ly_do'] ?? ''); ?>" placeholder="Ghi chú hao hụt..." style="width:100%;padding:8px;border:1px solid #e2e8f0;border-radius:4px;background:#fff;">
          </td>
        </tr>
      <?php endforeach; else: ?>
        <tr><td colspan="7" style="padding:20px;text-align:center;color:#94a3b8;">Không có nguyên vật liệu nào để kiểm kê</td></tr>
      <?php endif; ?>
      </tbody>
    </table>
  </div>

  <div style="margin-top:20px;display:flex;justify-content:flex-end;">
    <button type="button" id="btn-commit-kiemkho" style="padding:12px 24px;background:#2196f3;color:#fff;border:none;border-radius:8px;font-weight:600;font-size:16px;cursor:pointer;box-shadow:0 4px 6px -1px rgba(33,150,243,0.3);">
      <i class="fas fa-check-circle"></i> Hoàn thành & Lưu phiếu kiểm kho
    </button>
  </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Tự động tính toán chênh lệch khi thay đổi Tồn thực tế
    const inputs = document.querySelectorAll('.act-qty');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            const row = this.closest('tr');
            const sysQty = parseFloat(row.querySelector('.sys-qty').getAttribute('data-val')) || 0;
            const actQty = parseFloat(this.value) || 0;
            const diff = actQty - sysQty;
            
            const diffCell = row.querySelector('.diff-qty span');
            
            // Format number
            let diffText = Math.abs(diff).toLocaleString('vi-VN', {minimumFractionDigits: 0, maximumFractionDigits: 2});
            if (diff > 0) diffText = '+' + diffText;
            else if (diff < 0) diffText = '-' + diffText;
            
            diffCell.textContent = diffText;
            
            // Color
            if (diff < 0) diffCell.style.color = '#dc3545';
            else if (diff > 0) diffCell.style.color = '#28a745';
            else diffCell.style.color = '#94a3b8';
        });
    });

    // Submit form
    document.getElementById('btn-commit-kiemkho').addEventListener('click', function() {
        if (!confirm('Bạn có chắc chắn muốn lưu phiếu kiểm kho này?')) return;
        
        const btn = this;
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        
        const items = [];
        document.querySelectorAll('.kiemkho-row').forEach(row => {
            const id = row.getAttribute('data-id');
            const actQty = parseFloat(row.querySelector('.act-qty').value) || 0;
            const reason = row.querySelector('.reason').value;
            items.push({ id: id, act_qty: actQty, reason: reason });
        });
        
        const data = {
            ghi_chu: document.getElementById('ghi_chu').value,
            items: items
        };
        
        fetch('<?php echo getBaseUrl(); ?>app/kho/commit_kiemkho.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert('Tạo phiếu kiểm kho thành công!');
                window.location.href = '<?php echo getBaseUrl(); ?>app/kho/index.php?tab=kiemkho';
            } else {
                alert('Lỗi: ' + (res.error || 'Có lỗi xảy ra'));
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-check-circle"></i> Hoàn thành & Lưu phiếu kiểm kho';
            }
        })
        .catch(err => {
            console.error(err);
            alert('Lỗi kết nối máy chủ!');
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-check-circle"></i> Hoàn thành & Lưu phiếu kiểm kho';
        });
    });
});
</script>
