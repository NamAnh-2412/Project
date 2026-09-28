<div class="container-main">
  <div class="header-bar-nhom">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-th-large"></i></span> NHÓM LỰA CHỌN</h1>
    </div>
    <a class="btn-add" href="nhomluachon.php?add=1"><i class="fa fa-plus"></i> Thêm nhóm lựa chọn</a>
  </div>
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
    
    <?php if(isset($_GET['success'])): ?>
      <div class="alert alert-success">Đã thêm nhóm lựa chọn!</div>
    <?php endif; ?>
    <?php if(isset($notice) && $notice): ?>
      <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
    <?php endif; ?>
    <?php if(isset($showAddForm) && $showAddForm): ?>
    <div class="add-form">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
        <h3>Thêm nhóm lựa chọn</h3>
        <a href="nhomluachon.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : ''); ?>" style="color:#666;text-decoration:none;font-size:14px;">
          <i class="fa fa-times"></i> Đóng
        </a>
      </div>
      <form method="post">
        <input type="hidden" name="action" value="add_nhom">
        <?php 
        // Giữ lại giá trị form nếu có lỗi
        $ten_nhom = isset($_POST['ten_nhom']) ? htmlspecialchars($_POST['ten_nhom']) : '';
        $mota = isset($_POST['mota']) ? htmlspecialchars($_POST['mota']) : '';
        $thu_tu = isset($_POST['thu_tu']) ? (int)$_POST['thu_tu'] : 0;
        ?>
        <input type="text" name="ten_nhom" placeholder="Tên nhóm" required value="<?php echo $ten_nhom; ?>">
        <textarea name="mota" placeholder="Mô tả (tùy chọn)" rows="2"><?php echo $mota; ?></textarea>
        <input type="number" name="thu_tu" placeholder="Thứ tự" min="0" value="<?php echo $thu_tu; ?>">
        <div style="display:flex;gap:10px;margin-top:10px;">
          <button class="btn btn-primary" type="submit">Lưu</button>
          <a href="nhomluachon.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : ''); ?>" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
        </div>
      </form>
    </div>
    <?php endif; ?>
    <div class="searchbar">
      <form method="get" style="display:inline;">
        <input type="text" name="kw" placeholder="Tìm nhóm hoặc tên lựa chọn..." value="<?php echo htmlspecialchars($kw ?? '');?>"/>
      </form>
    </div>
    <table>
      <thead><tr><th>STT</th><th>Nhóm lựa chọn</th><th>Chi tiết lựa chọn</th><th>Sản phẩm liên kết</th><th>Tác vụ</th></tr></thead>
      <tbody>
      <?php if(isset($ds) && $ds): $i=1; foreach($ds as $row): ?>
        <tr>
          <td><?php echo $i++;?></td>
          <td><?php echo htmlspecialchars($row['ten_nhom']);?></td>
          <td>
            <?php if(!empty($row['danh_sach_lua_chon'])): ?>
              <?php 
              $lua_chon_names = array_map(function($lc) { 
                return htmlspecialchars($lc['ten_lua_chon']); 
              }, $row['danh_sach_lua_chon']); 
              echo implode(', ', $lua_chon_names);
              ?>
              <span style="color:#666;margin-left:5px;">(<?php echo count($row['danh_sach_lua_chon']); ?> lựa chọn)</span>
            <?php else: ?>
              <span style="color:#999;">Chưa có lựa chọn</span>
            <?php endif; ?>
          </td>
          <td class="prod-link">
            <?php 
            $so_sp = isset($row['so_sanpham']) ? (int)$row['so_sanpham'] : 0;
            if ($so_sp > 0): 
            ?>
              <a href="../sanpham/index.php?nhom_lua_chon=<?php echo $row['id']; ?>" style="color:#4caf50;text-decoration:none;">
                <?php echo $so_sp; ?> sản phẩm
              </a>
            <?php else: ?>
              <span style="color:#999;">0 sản phẩm</span>
            <?php endif; ?>
          </td>
          <td class="act-link">
            <a href="sua_nhomluachon.php?id=<?php echo $row['id']; ?>" title="Sửa nhóm lựa chọn">Sửa</a>
            <a href="xoa_nhomluachon.php?id=<?php echo $row['id']; ?>" title="Xóa nhóm lựa chọn" onclick="return confirm('Bạn có chắc chắn muốn xóa nhóm lựa chọn này?');">Xóa</a>
          </td>
        </tr>
      <?php endforeach; endif; ?>
      </tbody>
    </table>
  </div>
</div>

