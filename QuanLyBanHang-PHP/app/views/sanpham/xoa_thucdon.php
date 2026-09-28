<div class="container-main">
  <div class="header-bar-thucdon">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-bars"></i></span> XÓA THỰC ĐƠN</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      
      <?php if($hasProducts || $hasChildren): ?>
        <div class="alert alert-danger">
          <strong>Không thể xóa thực đơn này!</strong><br>
          <?php if($hasProducts): ?>
            Thực đơn này đang được sử dụng bởi <?php echo $productCountNum; ?> sản phẩm. Vui lòng xóa hoặc chuyển các sản phẩm sang thực đơn khác trước khi xóa.<br>
          <?php endif; ?>
          <?php if($hasChildren): ?>
            Thực đơn này còn <?php echo $childCountNum; ?> danh mục con. Vui lòng xóa các danh mục con trước.<br>
          <?php endif; ?>
        </div>
      <?php else: ?>
        <div class="alert alert-warning" style="background:#fff3cd;color:#856404;padding:15px;border-radius:8px;margin-bottom:15px;">
          <strong>Bạn có chắc chắn muốn xóa thực đơn này?</strong><br><br>
          <strong>Tên thực đơn:</strong> <?php echo htmlspecialchars($row['ten_danhmuc']); ?><br>
          <?php if(!empty($row['mota'])): ?>
            <strong>Mô tả:</strong> <?php echo htmlspecialchars($row['mota']); ?><br>
          <?php endif; ?>
          <strong>Thứ tự:</strong> <?php echo $row['thu_tu'] ?? 0; ?>
        </div>
        <form method="post">
          <input type="hidden" name="confirm" value="1">
          <div style="display:flex;gap:10px;">
            <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
            <a href="thucdon.php" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
          </div>
        </form>
      <?php endif; ?>
    </div>
  </div>
</div>

