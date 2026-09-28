<div class="header-bar">
    <h2><i class="fas fa-truck"></i> Quản lý nhà cung cấp</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<div class="content-card">
  <?php 
  $flashMessages = getFlashMessages();
  foreach ($flashMessages as $msg): 
  ?>
    <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:<?php echo $msg['type'] === 'success' ? '#e7f7ed' : '#fee2e2'; ?>;color:<?php echo $msg['type'] === 'success' ? '#0c6b3d' : '#991b1b'; ?>;">
      <?php echo htmlspecialchars($msg['message']); ?>
    </div>
  <?php endforeach; ?>
  
  <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/create.php" class="main-link"><i class="fas fa-plus"></i> Thêm nhà cung cấp</a>
  
  <!-- Search and filter -->
  <form method="get" style="margin:20px 0;display:flex;gap:10px;align-items:center;">
    <input type="text" name="kw" placeholder="Tìm kiếm (tên, SĐT, email)..." value="<?php echo htmlspecialchars($kw ?? ''); ?>" style="flex:1;padding:8px 12px;border:1px solid #d2d8ea;border-radius:6px;">
    <select name="status" style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:6px;">
      <option value="">Tất cả trạng thái</option>
      <option value="dang_hop_tac" <?php echo (isset($status) && $status === 'dang_hop_tac') ? 'selected' : ''; ?>>Đang hợp tác</option>
      <option value="ngung_hop_tac" <?php echo (isset($status) && $status === 'ngung_hop_tac') ? 'selected' : ''; ?>>Ngừng hợp tác</option>
    </select>
    <button type="submit" style="padding:8px 16px;background:#2196f3;color:#fff;border:none;border-radius:6px;cursor:pointer;">Tìm</button>
    <?php if (!empty($kw) || !empty($status)): ?>
      <a href="index.php" style="padding:8px 16px;background:#64748b;color:#fff;text-decoration:none;border-radius:6px;">Xóa bộ lọc</a>
    <?php endif; ?>
  </form>
  
  <table>
    <thead>
      <tr>
        <th>ID</th>
        <th>Tên nhà cung cấp</th>
        <th>SĐT</th>
        <th>Email</th>
        <th>Trạng thái</th>
        <th>Ngày tạo</th>
        <th>Tác vụ</th>
      </tr>
    </thead>
    <tbody>
    <?php if(!empty($list) && is_array($list)): foreach($list as $row): ?>
      <tr>
        <td><?php echo $row['id']; ?></td>
        <td><?php echo htmlspecialchars($row['ten_nhacungcap']); ?></td>
        <td><?php echo htmlspecialchars($row['so_dien_thoai'] ?? '-'); ?></td>
        <td><?php echo htmlspecialchars($row['email'] ?? '-'); ?></td>
        <td>
          <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;<?php echo $row['trang_thai'] === 'dang_hop_tac' ? 'background:#d4edda;color:#155724;' : 'background:#f8d7da;color:#721c24;'; ?>">
            <?php echo $row['trang_thai'] === 'dang_hop_tac' ? 'Đang hợp tác' : 'Ngừng hợp tác'; ?>
          </span>
        </td>
        <td><?php echo date('d/m/Y', strtotime($row['ngay_tao'])); ?></td>
        <td>
          <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/view.php?id=<?php echo $row['id']; ?>" style="color:#2196f3;">Xem</a> | 
          <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/edit.php?id=<?php echo $row['id']; ?>" style="color:#ff9800;">Sửa</a> | 
          <a href="<?php echo getBaseUrl(); ?>app/nhacungcap/delete.php?id=<?php echo $row['id']; ?>" style="color:#f44336;" onclick="return confirm('Xóa nhà cung cấp?')">Xóa</a>
        </td>
      </tr>
    <?php endforeach; else: ?>
      <tr><td colspan="7">Không có dữ liệu</td></tr>
    <?php endif; ?>
    </tbody>
  </table>
</div>

