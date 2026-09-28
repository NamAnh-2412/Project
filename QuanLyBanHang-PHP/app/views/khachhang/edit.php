<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <form method="post">
        <div class="add-form">
          <h3>Thông tin khách hàng</h3>
          <div class="form-group">
            <label class="form-label required">Tên khách hàng</label>
            <input type="text" name="ten_khachhang" class="form-input" required value="<?php echo htmlspecialchars($row['ten_khachhang']); ?>" />
          </div>
          <div class="form-group">
            <label class="form-label">Số điện thoại</label>
            <input type="tel" name="so_dien_thoai" class="form-input" value="<?php echo htmlspecialchars($row['so_dien_thoai']); ?>" />
          </div>
          <?php if(isset($hasEmail) && $hasEmail): ?>
            <div class="form-group">
              <label class="form-label">Email (tuỳ chọn)</label>
              <input type="email" name="email" class="form-input" value="<?php echo htmlspecialchars($row['email']??''); ?>" />
            </div>
          <?php endif; ?>
          <?php if(isset($hasNgaySinh) && $hasNgaySinh): ?>
            <div class="form-group">
              <label class="form-label">Ngày sinh</label>
              <input type="date" name="ngay_sinh" class="form-input" value="<?php echo htmlspecialchars($row['ngay_sinh']??''); ?>" />
            </div>
          <?php endif; ?>
          <?php if(isset($hasDiaChi) && $hasDiaChi): ?>
            <div class="form-group">
              <label class="form-label">Địa chỉ</label>
              <input type="text" name="dia_chi" class="form-input" value="<?php echo htmlspecialchars($row['dia_chi']??''); ?>" />
            </div>
          <?php endif; ?>
          <?php if(isset($hasGhiChu) && $hasGhiChu): ?>
            <div class="form-group">
              <label class="form-label">Ghi chú</label>
              <textarea name="ghi_chu" class="form-input"><?php echo htmlspecialchars($row['ghi_chu']??''); ?></textarea>
            </div>
          <?php endif; ?>
          <div style="margin-top:12px;display:flex;gap:8px;">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="<?php echo getBaseUrl(); ?>app/khachhang/view.php?id=<?php echo $row['id']; ?>">Xem chi tiết</a>
            <a class="btn" href="index.php">Hủy</a>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>
