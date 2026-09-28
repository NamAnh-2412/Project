<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <div class="alert alert-danger">
        <strong>Bạn có chắc chắn muốn xóa nhân viên này?</strong><br>
        Mã NV: <?php echo htmlspecialchars($row['ma_nhanvien']); ?><br>
        Tên: <?php echo htmlspecialchars($row['ten_nhanvien']); ?>
      </div>
      <form method="post">
        <input type="hidden" name="confirm" value="1">
        <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
        <a class="btn btn-secondary" href="index.php">Hủy</a>
      </form>
    </div>
  </div>
</div>

