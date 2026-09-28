<div class="container-main">
  <div class="header-bar-thucdon">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-bars"></i></span> SỬA THỰC ĐƠN</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <div class="add-form">
        <h3>Sửa thực đơn</h3>
        <form method="post">
          <input type="text" name="ten_danhmuc" placeholder="Tên thực đơn" required value="<?php echo htmlspecialchars($row['ten_danhmuc']); ?>">
          <input type="number" name="thu_tu" placeholder="Thứ tự" min="0" value="<?php echo $row['thu_tu'] ?? 0; ?>">
          <textarea name="mota" placeholder="Mô tả" rows="2"><?php echo htmlspecialchars($row['mota'] ?? ''); ?></textarea>
          <div style="display:flex;gap:10px;margin-top:10px;">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a href="thucdon.php" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

