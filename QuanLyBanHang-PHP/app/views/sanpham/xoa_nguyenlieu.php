<div class="container-main">
    <div class="header-bar-nglieu">
      <div class="left">
        <a href="nguyenlieu.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách</a>
        <h1><span class="ic"><i class="fas fa-trash"></i></span> XÓA NGUYÊN LIỆU</h1>
      </div>
    </div>
    <div class="content-wrap">
      <div class="tbl-wrap">
        <div class="alert alert-danger">
          <strong>⚠️ Bạn có chắc chắn muốn xóa nguyên liệu này?</strong><br><br>
          <strong>Tên:</strong> <?php echo htmlspecialchars($row['ten_nguyenlieu']); ?><br>
          <strong>Mã:</strong> <?php echo htmlspecialchars($row['ma_nguyenlieu']); ?><br>
          <strong>Nhóm:</strong> <?php echo htmlspecialchars($row['danhmuc_nl']); ?><br>
          <strong>Số lượng tồn:</strong> <?php echo $row['so_luong_ton']; ?> <?php echo htmlspecialchars($row['don_vi_tinh']); ?><br><br>
          <span style="color: #d32f2f;">Hành động này không thể hoàn tác!</span>
        </div>
        <form method="post">
          <input type="hidden" name="confirm" value="1">
          <div style="margin-top:12px;">
            <button class="btn btn-danger" type="submit" onclick="return confirm('Bạn chắc chắn muốn xóa?')">Xác nhận xóa</button>
            <a class="btn btn-secondary" href="nguyenlieu.php">Hủy</a>
          </div>
        </form>
      </div>
    </div>
  </div>

