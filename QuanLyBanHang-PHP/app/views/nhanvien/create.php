<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <form method="post">
        <div class="add-form">
          <h3>Thông tin nhân viên</h3>
          <label>Tên nhân viên</label>
          <input type="text" name="ten_nhanvien" required value="<?php echo htmlspecialchars($_POST['ten_nhanvien']??''); ?>" />
          <label>Chức vụ</label>
          <select name="chuc_vu">
            <option value="nhan_vien">Nhân viên</option>
            <option value="cua_hang_truong">Cửa hàng trưởng</option>
          </select>
          <label>Số điện thoại</label>
          <input type="text" name="so_dien_thoai" value="<?php echo htmlspecialchars($_POST['so_dien_thoai']??''); ?>" />
          <label>Email</label>
          <input type="email" name="email" value="<?php echo htmlspecialchars($_POST['email']??''); ?>" />
          <label>Ngày vào làm <span style="color:red;">*</span></label>
          <input type="date" name="ngay_vao_lam" required value="<?php echo htmlspecialchars($_POST['ngay_vao_lam']??''); ?>" />
          <label>Ngày sinh</label>
          <input type="date" name="ngay_sinh" value="<?php echo htmlspecialchars($_POST['ngay_sinh']??''); ?>" />
          <label>Trạng thái</label>
          <select name="trang_thai">
            <option value="dang_lam">Đang làm việc</option>
            <option value="da_nghi">Đã nghỉ việc</option>
          </select>
          <div style="margin-top:12px;">
            <button class="btn btn-primary" type="submit">Lưu</button> 
            <a class="btn" href="index.php">Hủy</a>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>

