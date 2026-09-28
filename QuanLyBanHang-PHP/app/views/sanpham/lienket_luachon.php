<div class="container-main">
  <div class="header-bar-nhom">
    <div class="left">
      <a href="index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách sản phẩm</a>
      <h1><span class="ic"><i class="fas fa-link"></i></span> LIÊN KẾT NHÓM LỰA CHỌN</h1>
    </div>
  </div>
  <div class="tbl-wrap">
    <?php 
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
    
    <div style="background:#f7fafc;padding:15px;border-radius:8px;margin-bottom:20px">
      <strong>Sản phẩm:</strong> <?php echo htmlspecialchars($product['ten_sanpham']); ?>
    </div>
    
    <form method="post">
      <input type="hidden" name="action" value="update_links">
      
      <div style="margin-bottom:20px">
        <h3 style="margin-bottom:15px">Chọn các nhóm lựa chọn cho sản phẩm:</h3>
        
        <?php if(empty($allGroups)): ?>
          <p style="color:#999;padding:20px;text-align:center">Chưa có nhóm lựa chọn nào. <a href="nhomluachon.php">Tạo nhóm lựa chọn</a></p>
        <?php else: ?>
          <div style="display:grid;grid-template-columns:repeat(auto-fill, minmax(300px, 1fr));gap:15px">
            <?php foreach($allGroups as $group): ?>
              <div style="border:1px solid #e5e7eb;border-radius:8px;padding:15px;background:#fff">
                <label style="display:flex;align-items:center;cursor:pointer">
                  <input type="checkbox" 
                         name="nhom_lua_chon[]" 
                         value="<?php echo $group['id']; ?>"
                         <?php echo in_array($group['id'], $linkedGroupIds) ? 'checked' : ''; ?>
                         onchange="toggleRequired(this, <?php echo $group['id']; ?>)"
                         style="margin-right:10px;width:18px;height:18px">
                  <div style="flex:1">
                    <div style="font-weight:600;margin-bottom:5px">
                      <?php echo htmlspecialchars($group['ten_nhom']); ?>
                    </div>
                    <?php if($group['mota']): ?>
                      <div style="font-size:12px;color:#666"><?php echo htmlspecialchars($group['mota']); ?></div>
                    <?php endif; ?>
                  </div>
                </label>
                <div style="margin-top:10px;margin-left:28px">
                  <label style="display:flex;align-items:center;cursor:pointer;font-size:13px">
                    <input type="checkbox" 
                           name="bat_buoc[]" 
                           value="<?php echo $group['id']; ?>"
                           id="required_<?php echo $group['id']; ?>"
                           <?php echo in_array($group['id'], $requiredGroupIds) ? 'checked' : ''; ?>
                           style="margin-right:5px;width:16px;height:16px">
                    <span style="color:#666">Bắt buộc phải chọn</span>
                  </label>
                </div>
              </div>
            <?php endforeach; ?>
          </div>
        <?php endif; ?>
      </div>
      
      <div style="display:flex;gap:10px;margin-top:20px">
        <button class="btn btn-primary" type="submit">Lưu liên kết</button>
        <a href="index.php" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
      </div>
    </form>
  </div>
</div>
<script>
function toggleRequired(checkbox, groupId) {
  const requiredCheckbox = document.getElementById('required_' + groupId);
  if (!checkbox.checked) {
    requiredCheckbox.checked = false;
    requiredCheckbox.disabled = true;
  } else {
    requiredCheckbox.disabled = false;
  }
}

// Khởi tạo trạng thái ban đầu
document.querySelectorAll('input[name="nhom_lua_chon[]"]').forEach(cb => {
  const groupId = cb.value;
  toggleRequired(cb, groupId);
});
</script>

