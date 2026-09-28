<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <div class="alert alert-danger">
        <strong>Bạn có chắc chắn muốn xóa khách hàng này?</strong><br>
        Tên: <?php echo htmlspecialchars($row['ten_khachhang']); ?><br>
        SĐT: <?php echo htmlspecialchars($row['so_dien_thoai']); ?>
      </div>
      <form method="post">
        <input type="hidden" name="confirm" value="1">
        <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
        <a class="btn btn-secondary" href="index.php">Hủy</a>
      </form>
    </div>
  </div>
</div>

