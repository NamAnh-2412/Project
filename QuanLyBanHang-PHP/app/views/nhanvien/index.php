<div class="header-bar">
    <h2><i class="fas fa-user-tie"></i> Quản lý nhân viên</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <?php if(isset($_GET['success'])): ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:#e7f7ed;color:#0c6b3d;">Đã cập nhật thành công!</div>
  <?php endif; ?>
  <?php if(isset($_GET['deleted'])): ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:#fee2e2;color:#991b1b;">Đã xóa thành công!</div>
  <?php endif; ?>
  <?php if(isManager()): ?>
    <a href="<?php echo getBaseUrl(); ?>app/nhanvien/create.php" class="main-link"><i class="fas fa-plus"></i> Thêm nhân viên mới</a>
  <?php endif; ?>
  <table class="table-responsive">
    <thead>
      <tr>
        <th>ID</th>
        <?php if(isManager()): ?><th>Mã NV</th><?php endif; ?>
        <th>Tên NV</th>
        <th>Chức vụ</th>
        <th>SĐT</th>
        <?php if(isManager()): ?><th>Email</th><?php endif; ?>
        <?php if(isManager()): ?><th>Ngày vào</th><?php endif; ?>
        <?php if(isManager()): ?><th>Ngày sinh</th><?php endif; ?>
        <th>Trạng thái</th>
        <th>Tác vụ</th>
      </tr>
    </thead>
    <tbody>
    <?php if(!empty($list) && is_array($list)): foreach($list as $row): ?>
      <tr>
        <td><?php echo $row['id']; ?></td>
        <?php if(isManager()): ?>
          <td><?php echo htmlspecialchars($row['ma_nhanvien']); ?></td>
        <?php endif; ?>
        <td><?php echo htmlspecialchars($row['ten_nhanvien']); ?></td>
        <td>
          <span id="role-<?php echo $row['id']; ?>">
            <?php 
            $chuc_vu_text = $row['chuc_vu'] === 'cua_hang_truong' ? 'Cửa hàng trưởng' : 'Nhân viên';
            echo htmlspecialchars($chuc_vu_text); 
            ?>
          </span>
          <?php if(isManager() && $row['id'] != getCurrentUserId()): ?>
            <button id="btn-role-<?php echo $row['id']; ?>" onclick="changeRole(<?php echo $row['id']; ?>, '<?php echo htmlspecialchars($row['ten_nhanvien'], ENT_QUOTES); ?>', '<?php echo $row['chuc_vu']; ?>', this)" 
                    style="margin-left:8px;padding:4px 8px;background:#f97316;color:#fff;border:none;border-radius:4px;font-size:12px;cursor:pointer;" 
                    title="Đổi chức vụ">
              <i class="fas fa-exchange-alt"></i> Đổi
            </button>
          <?php endif; ?>
        </td>
        <td><?php echo htmlspecialchars($row['so_dien_thoai']); ?></td>
        <?php if(isManager()): ?>
          <td><?php echo htmlspecialchars($row['email']??''); ?></td>
        <?php endif; ?>
        <?php if(isManager()): ?>
          <td><?php echo $row['ngay_vao_lam'] ? formatDate($row['ngay_vao_lam'], 'd/m/Y') : ''; ?></td>
        <?php endif; ?>
        <?php if(isManager()): ?>
          <td><?php echo $row['ngay_sinh'] ? formatDate($row['ngay_sinh'], 'd/m/Y') : ''; ?></td>
        <?php endif; ?>
        <td><?php echo htmlspecialchars($row['trang_thai']); ?></td>
        <td>
          <?php if(isManager()): ?>
            <a href="<?php echo getBaseUrl(); ?>app/nhanvien/edit.php?id=<?php echo $row['id']; ?>">Sửa</a> | <a href="<?php echo getBaseUrl(); ?>app/nhanvien/delete.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Xóa nhân viên?')">Xóa</a>
          <?php else: ?>
            <span style="color:#999;">Không có quyền</span>
          <?php endif; ?>
        </td>
      </tr>
    <?php endforeach; else: ?>
      <tr>
        <td colspan="<?php echo isManager() ? '10' : '6'; ?>">Không có dữ liệu</td>
      </tr>
    <?php endif; ?>
    </tbody>
  </table>
</div>

<script>
function changeRole(employeeId, employeeName, currentRole, buttonElement) {
  const currentRoleText = currentRole === 'cua_hang_truong' ? 'Cửa hàng trưởng' : 'Nhân viên';
  const newRoleText = currentRole === 'cua_hang_truong' ? 'Nhân viên' : 'Cửa hàng trưởng';
  
  if (!confirm('Bạn có chắc chắn muốn đổi chức vụ của "' + employeeName + '" từ "' + currentRoleText + '" sang "' + newRoleText + '"?')) {
    return;
  }
  
  // Disable button during request
  const button = buttonElement || document.getElementById('btn-role-' + employeeId);
  if (button) {
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
  }
  
  fetch('<?php echo getBaseUrl(); ?>app/nhanvien/change_role.php', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      id: employeeId
    })
  })
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      // Update the role display
      const roleElement = document.getElementById('role-' + employeeId);
      if (roleElement) {
        const newRoleText = data.new_role === 'cua_hang_truong' ? 'Cửa hàng trưởng' : 'Nhân viên';
        roleElement.textContent = newRoleText;
      }
      
      // Update button for next toggle
      if (button) {
        button.disabled = false;
        button.innerHTML = '<i class="fas fa-exchange-alt"></i> Đổi';
        button.setAttribute('onclick', 'changeRole(' + employeeId + ', \'' + employeeName.replace(/'/g, "\\'") + '\', \'' + data.new_role + '\', this)');
      }
      
      // Show success message
      alert('Đã đổi chức vụ thành công!');
    } else {
      alert('Lỗi: ' + (data.error || 'Không thể đổi chức vụ'));
      if (button) {
        button.disabled = false;
        button.innerHTML = '<i class="fas fa-exchange-alt"></i> Đổi';
      }
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Có lỗi xảy ra khi đổi chức vụ');
    if (button) {
      button.disabled = false;
      button.innerHTML = '<i class="fas fa-exchange-alt"></i> Đổi';
    }
  });
}
</script>

