<div class="container-main">
  <div class="header-bar-nhom">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-th-large"></i></span> SỬA NHÓM LỰA CHỌN</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php 
      // Hiển thị flash messages
      $flashMessages = getFlashMessages();
      foreach ($flashMessages as $msg): 
      ?>
        <div class="alert alert-<?php echo $msg['type'] === 'success' ? 'success' : 'danger'; ?>">
          <?php echo htmlspecialchars($msg['message']); ?>
        </div>
      <?php endforeach; ?>
      
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      
      <!-- Form sửa thông tin nhóm -->
      <div class="add-form" style="margin-bottom:30px;">
        <h3>Sửa thông tin nhóm</h3>
        <form method="post">
          <input type="hidden" name="action" value="update_group">
          <input type="text" name="ten_nhom" placeholder="Tên nhóm lựa chọn" required value="<?php echo htmlspecialchars($row['ten_nhom']); ?>">
          <input type="number" name="thu_tu" placeholder="Thứ tự" min="0" value="<?php echo $row['thu_tu'] ?? 0; ?>">
          <textarea name="mota" placeholder="Mô tả (tùy chọn)" rows="2"><?php echo htmlspecialchars($row['mota'] ?? ''); ?></textarea>
          <div style="display:flex;gap:10px;margin-top:10px;">
            <button class="btn btn-primary" type="submit">Lưu thông tin nhóm</button>
            <a href="nhomluachon.php" class="btn" style="text-decoration:none;display:inline-block;">Về danh sách</a>
          </div>
        </form>
      </div>
      
      <!-- Phần quản lý lựa chọn -->
      <div class="add-form">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
          <h3>Quản lý lựa chọn</h3>
          <button type="button" class="btn btn-primary" onclick="showAddChoiceForm()" style="padding:8px 16px;font-size:14px;">
            <i class="fa fa-plus"></i> Thêm lựa chọn
          </button>
        </div>
        
        <!-- Form thêm lựa chọn mới -->
        <div id="addChoiceForm" style="display:none;padding:15px;background:#f8fafc;border-radius:8px;margin-bottom:20px;border:1px solid #e2e8f0;">
          <h4 style="margin-top:0;margin-bottom:15px;">Thêm lựa chọn mới</h4>
          <form method="post" id="formAddChoice">
            <input type="hidden" name="action" value="add_choice">
            <div style="display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:10px;align-items:end;">
              <input type="text" name="ten_lua_chon" placeholder="Tên lựa chọn (VD: Thạch trà)" required>
              <input type="number" name="gia_tang_them" placeholder="Giá tăng thêm" step="0.01" min="0" value="0">
              <input type="number" name="thu_tu_choice" placeholder="Thứ tự" min="0" value="0">
              <div style="display:flex;gap:8px;">
                <button class="btn btn-primary" type="submit" style="padding:10px 16px;">Thêm</button>
                <button type="button" class="btn" onclick="hideAddChoiceForm()" style="padding:10px 16px;">Hủy</button>
              </div>
            </div>
          </form>
        </div>
        
        <!-- Danh sách lựa chọn hiện có -->
        <?php if(!empty($choices) && is_array($choices)): ?>
          <table style="width:100%;margin-top:15px;">
            <thead>
              <tr>
                <th style="text-align:left;padding:10px;background:#f1f5f9;border-bottom:2px solid #e2e8f0;">Tên lựa chọn</th>
                <th style="text-align:left;padding:10px;background:#f1f5f9;border-bottom:2px solid #e2e8f0;">Giá tăng thêm</th>
                <th style="text-align:left;padding:10px;background:#f1f5f9;border-bottom:2px solid #e2e8f0;">Thứ tự</th>
                <th style="text-align:center;padding:10px;background:#f1f5f9;border-bottom:2px solid #e2e8f0;">Tác vụ</th>
              </tr>
            </thead>
            <tbody>
              <?php foreach($choices as $choice): ?>
                <tr class="choice-row" data-choice-id="<?php echo $choice['id']; ?>">
                  <td style="padding:12px;border-bottom:1px solid #e2e8f0;">
                    <span class="choice-name"><?php echo htmlspecialchars($choice['ten_lua_chon']); ?></span>
                    <input type="text" class="choice-input" name="ten_lua_chon" value="<?php echo htmlspecialchars($choice['ten_lua_chon']); ?>" style="display:none;width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:4px;">
                  </td>
                  <td style="padding:12px;border-bottom:1px solid #e2e8f0;">
                    <span class="choice-price"><?php echo number_format($choice['gia_tang_them'] ?? 0, 0, ',', '.'); ?>₫</span>
                    <input type="number" class="choice-input" name="gia_tang_them" value="<?php echo $choice['gia_tang_them'] ?? 0; ?>" step="0.01" min="0" style="display:none;width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:4px;">
                  </td>
                  <td style="padding:12px;border-bottom:1px solid #e2e8f0;">
                    <span class="choice-order"><?php echo $choice['thu_tu'] ?? 0; ?></span>
                    <input type="number" class="choice-input" name="thu_tu_choice" value="<?php echo $choice['thu_tu'] ?? 0; ?>" min="0" style="display:none;width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:4px;">
                  </td>
                  <td style="padding:12px;border-bottom:1px solid #e2e8f0;text-align:center;">
                    <div class="choice-actions">
                      <a href="#" class="btn-edit-choice" onclick="editChoice(<?php echo $choice['id']; ?>); return false;" style="color:#3b82f6;text-decoration:none;margin-right:10px;">
                        <i class="fa fa-edit"></i> Sửa
                      </a>
                      <a href="#" class="btn-save-choice" onclick="saveChoice(<?php echo $choice['id']; ?>); return false;" style="display:none;color:#10b981;text-decoration:none;margin-right:10px;">
                        <i class="fa fa-check"></i> Lưu
                      </a>
                      <a href="#" class="btn-cancel-choice" onclick="cancelEditChoice(<?php echo $choice['id']; ?>); return false;" style="display:none;color:#64748b;text-decoration:none;margin-right:10px;">
                        <i class="fa fa-times"></i> Hủy
                      </a>
                      <a href="#" class="btn-delete-choice" onclick="deleteChoice(<?php echo $choice['id']; ?>); return false;" style="color:#ef4444;text-decoration:none;">
                        <i class="fa fa-trash"></i> Xóa
                      </a>
                    </div>
                  </td>
                </tr>
              <?php endforeach; ?>
            </tbody>
          </table>
        <?php else: ?>
          <div style="padding:30px;text-align:center;color:#64748b;background:#f8fafc;border-radius:8px;margin-top:15px;">
            <i class="fa fa-info-circle" style="font-size:24px;margin-bottom:10px;display:block;"></i>
            Chưa có lựa chọn nào. Click "Thêm lựa chọn" để thêm.
          </div>
        <?php endif; ?>
      </div>
    </div>
  </div>
</div>

<script>
// Show/hide add choice form
function showAddChoiceForm() {
  document.getElementById('addChoiceForm').style.display = 'block';
  document.querySelector('#formAddChoice input[name="ten_lua_chon"]').focus();
}

function hideAddChoiceForm() {
  document.getElementById('addChoiceForm').style.display = 'none';
  document.getElementById('formAddChoice').reset();
}

// Edit choice
var originalChoiceData = {};

function editChoice(choiceId) {
  const row = document.querySelector(`tr.choice-row[data-choice-id="${choiceId}"]`);
  if (!row) return;
  
  // Store original values
  originalChoiceData[choiceId] = {
    name: row.querySelector('.choice-name').textContent,
    price: row.querySelector('.choice-price').textContent,
    order: row.querySelector('.choice-order').textContent
  };
  
  // Show inputs, hide spans
  row.querySelectorAll('.choice-input').forEach(input => input.style.display = 'block');
  row.querySelector('.choice-name').style.display = 'none';
  row.querySelector('.choice-price').style.display = 'none';
  row.querySelector('.choice-order').style.display = 'none';
  
  // Show/hide buttons
  row.querySelector('.btn-edit-choice').style.display = 'none';
  row.querySelector('.btn-delete-choice').style.display = 'none';
  row.querySelector('.btn-save-choice').style.display = 'inline';
  row.querySelector('.btn-cancel-choice').style.display = 'inline';
}

function cancelEditChoice(choiceId) {
  const row = document.querySelector(`tr.choice-row[data-choice-id="${choiceId}"]`);
  if (!row || !originalChoiceData[choiceId]) return;
  
  // Restore original values
  const original = originalChoiceData[choiceId];
  row.querySelector('input[name="ten_lua_chon"]').value = original.name;
  row.querySelector('input[name="gia_tang_them"]').value = parseFloat(original.price.replace(/[^\d]/g, '')) || 0;
  row.querySelector('input[name="thu_tu_choice"]').value = parseInt(original.order) || 0;
  
  // Hide inputs, show spans
  row.querySelectorAll('.choice-input').forEach(input => input.style.display = 'none');
  row.querySelector('.choice-name').style.display = 'inline';
  row.querySelector('.choice-price').style.display = 'inline';
  row.querySelector('.choice-order').style.display = 'inline';
  
  // Show/hide buttons
  row.querySelector('.btn-edit-choice').style.display = 'inline';
  row.querySelector('.btn-delete-choice').style.display = 'inline';
  row.querySelector('.btn-save-choice').style.display = 'none';
  row.querySelector('.btn-cancel-choice').style.display = 'none';
  
  delete originalChoiceData[choiceId];
}

function saveChoice(choiceId) {
  const row = document.querySelector(`tr.choice-row[data-choice-id="${choiceId}"]`);
  if (!row) return;
  
  const form = document.createElement('form');
  form.method = 'POST';
  form.innerHTML = `
    <input type="hidden" name="action" value="update_choice">
    <input type="hidden" name="choice_id" value="${choiceId}">
    <input type="hidden" name="ten_lua_chon" value="${row.querySelector('input[name="ten_lua_chon"]').value}">
    <input type="hidden" name="gia_tang_them" value="${row.querySelector('input[name="gia_tang_them"]').value}">
    <input type="hidden" name="thu_tu_choice" value="${row.querySelector('input[name="thu_tu_choice"]').value}">
  `;
  document.body.appendChild(form);
  form.submit();
}

function deleteChoice(choiceId) {
  if (!confirm('Bạn có chắc chắn muốn xóa lựa chọn này?')) {
    return;
  }
  
  const form = document.createElement('form');
  form.method = 'POST';
  form.innerHTML = `
    <input type="hidden" name="action" value="delete_choice">
    <input type="hidden" name="choice_id" value="${choiceId}">
  `;
  document.body.appendChild(form);
  form.submit();
}
</script>

