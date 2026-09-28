<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <form method="post">
        <div class="add-form">
          <h3>Thông tin nhân viên</h3>
          <label>Mã nhân viên</label>
          <input type="text" name="ma_nhanvien" required value="<?php echo htmlspecialchars($row['ma_nhanvien']); ?>" />
          <label>Tên nhân viên</label>
          <input type="text" name="ten_nhanvien" required value="<?php echo htmlspecialchars($row['ten_nhanvien']); ?>" />
          <label>Chức vụ</label>
          <select name="chuc_vu">
            <option value="nhan_vien" <?php echo $row['chuc_vu']=='nhan_vien'?'selected':''; ?>>Nhân viên</option>
            <option value="cua_hang_truong" <?php echo $row['chuc_vu']=='cua_hang_truong'?'selected':''; ?>>Cửa hàng trưởng</option>
          </select>
          <label>Số điện thoại</label>
          <input type="text" name="so_dien_thoai" value="<?php echo htmlspecialchars($row['so_dien_thoai']??''); ?>" />
          <label>Email</label>
          <input type="email" name="email" value="<?php echo htmlspecialchars($row['email']??''); ?>" />
          <label>Ngày vào làm</label>
          <input type="date" name="ngay_vao_lam" value="<?php echo htmlspecialchars($row['ngay_vao_lam']??''); ?>" />
          <label>Ngày sinh</label>
          <input type="date" name="ngay_sinh" value="<?php echo htmlspecialchars($row['ngay_sinh']??''); ?>" />
          <label>Trạng thái</label>
          <select name="trang_thai">
            <option value="dang_lam" <?php echo ($row['trang_thai']??'dang_lam')=='dang_lam'?'selected':''; ?>>Đang làm việc</option>
            <option value="da_nghi" <?php echo ($row['trang_thai']??'')=='da_nghi'?'selected':''; ?>>Nghỉ việc</option>
          </select>
          <div style="margin-top:12px;">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="index.php">Hủy</a>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>

