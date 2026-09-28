<div class="container-main">
  <div class="header-bar-thucdon">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-bars"></i></span> THỰC ĐƠN</h1>
    </div>
    <a class="btn-add" href="thucdon.php?add=1"><i class="fa fa-plus"></i> Thêm thực đơn</a>
  </div>
  <div class="content-wrap">
    <form method="get" class="filter-box">
      <div>
        <label>Chi nhánh:</label>
        <select disabled><option>Tất cả</option></select>
      </div>
      <div style="margin:12px 0 0 0;">
        <label>Trạng thái:</label>
        <select disabled><option>Hoạt động</option></select>
      </div>
    </form>
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
        <div class="alert alert-success">Đã thêm thực đơn thành công!</div>
      <?php endif; ?>
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <?php if(isset($showAddForm) && $showAddForm): ?>
      <div class="add-form">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
          <h3>Thêm thực đơn</h3>
          <a href="thucdon.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : ''); ?>" style="color:#666;text-decoration:none;font-size:14px;">
            <i class="fa fa-times"></i> Đóng
          </a>
        </div>
        <form method="post">
          <input type="hidden" name="action" value="add_thucdon">
          <?php 
          // Giữ lại giá trị form nếu có lỗi
          $ten_danhmuc = isset($_POST['ten_danhmuc']) ? htmlspecialchars($_POST['ten_danhmuc']) : '';
          $mota = isset($_POST['mota']) ? htmlspecialchars($_POST['mota']) : '';
          $thu_tu = isset($_POST['thu_tu']) ? (int)$_POST['thu_tu'] : 0;
          ?>
          <input type="text" name="ten_danhmuc" placeholder="Tên thực đơn" required value="<?php echo $ten_danhmuc; ?>">
          <input type="number" name="thu_tu" placeholder="Thứ tự" min="0" value="<?php echo $thu_tu; ?>">
          <textarea name="mota" placeholder="Mô tả" rows="2"><?php echo $mota; ?></textarea>
          <div style="display:flex;gap:10px;margin-top:10px;">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a href="thucdon.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : ''); ?>" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
          </div>
        </form>
      </div>
      <?php endif; ?>
      <div class="searchbar">
        <form method="get" style="display:inline;">
          <input type="text" name="kw" placeholder="Tìm tên thực đơn..." value="<?php echo htmlspecialchars($kw ?? '');?>"/>
        </form>
      </div>
      <table>
        <thead><tr><th>STT</th><th>Tên thực đơn</th><th>Sản phẩm</th><th>Áp dụng</th><th>Kênh bán</th><th>Tác vụ</th></tr></thead>
        <tbody>
        <?php if(isset($ds) && $ds): $i=1; foreach($ds as $row): ?>
          <tr>
            <td><?php echo $i++;?></td>
            <td><?php echo htmlspecialchars($row['ten_danhmuc']);?></td>
            <td><?php echo ($row['so_sanpham'] ?? 0); ?> sản phẩm</td>
            <td class="aply">Tất cả chi nhánh</td>
            <td class="kenh">Trực tiếp, Online</td>
            <td class="act-link">
              <a href="sua_thucdon.php?id=<?php echo $row['id']; ?>">Sửa</a>
              <a href="xoa_thucdon.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc muốn xóa thực đơn này?')">Xóa</a>
            </td>
          </tr>
        <?php endforeach; endif; ?>
        </tbody>
      </table>
    </div>
  </div>
</div>

