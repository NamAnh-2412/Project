<div class="container-main">
  <div class="header-bar-nhom">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-th-large"></i></span> XÓA NHÓM LỰA CHỌN</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      
      <?php if($hasProducts || $hasChoices): ?>
        <div class="alert alert-danger">
          <strong>Không thể xóa nhóm lựa chọn này!</strong><br>
          <?php if($hasProducts): ?>
            Nhóm lựa chọn này đang được sử dụng bởi <?php echo $productCountNum; ?> sản phẩm. Vui lòng gỡ liên kết các sản phẩm trước khi xóa.<br>
          <?php endif; ?>
          <?php if($hasChoices): ?>
            Nhóm lựa chọn này còn <?php echo $choiceCountNum; ?> lựa chọn. Vui lòng xóa các lựa chọn trước.<br>
          <?php endif; ?>
        </div>
      <?php else: ?>
        <div class="alert alert-warning" style="background:#fff3cd;color:#856404;padding:15px;border-radius:8px;margin-bottom:15px;">
          <strong>Bạn có chắc chắn muốn xóa nhóm lựa chọn này?</strong><br><br>
          <strong>Tên nhóm:</strong> <?php echo htmlspecialchars($row['ten_nhom']); ?><br>
          <?php if(!empty($row['mota'])): ?>
            <strong>Mô tả:</strong> <?php echo htmlspecialchars($row['mota']); ?><br>
          <?php endif; ?>
          <strong>Thứ tự:</strong> <?php echo $row['thu_tu'] ?? 0; ?><br><br>
          <strong>Lưu ý:</strong> Hành động này không thể hoàn tác. Tất cả liên kết với nhóm lựa chọn cũng sẽ bị xóa.
        </div>
        <form method="post">
          <input type="hidden" name="confirm" value="1">
          <div style="display:flex;gap:10px;">
            <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
            <a href="nhomluachon.php" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
          </div>
        </form>
      <?php endif; ?>
    </div>
  </div>
</div>

