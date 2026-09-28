<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php 
      $flashMessages = getFlashMessages();
      foreach ($flashMessages as $msg): 
      ?>
        <div style="padding:12px 16px;border-radius:8px;margin-bottom:20px;background:<?php echo $msg['type'] === 'success' ? '#e7f7ed' : '#fee2e2'; ?>;color:<?php echo $msg['type'] === 'success' ? '#0c6b3d' : '#991b1b'; ?>;">
          <?php echo htmlspecialchars($msg['message']); ?>
        </div>
      <?php endforeach; ?>
      
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      
      <form method="post">
        <div class="add-form">
          <h3>Thông tin nhà cung cấp</h3>
          <label>Tên nhà cung cấp <span style="color:red">*</span></label>
          <input type="text" name="ten_nhacungcap" required value="<?php echo htmlspecialchars($row['ten_nhacungcap']); ?>" placeholder="Nhập tên nhà cung cấp" />
          
          <label>Số điện thoại</label>
          <input type="text" name="so_dien_thoai" value="<?php echo htmlspecialchars($row['so_dien_thoai'] ?? ''); ?>" placeholder="0123456789" />
          
          <label>Email</label>
          <input type="email" name="email" value="<?php echo htmlspecialchars($row['email'] ?? ''); ?>" placeholder="contact@example.com" />
          
          <label>Địa chỉ</label>
          <textarea name="dia_chi" rows="2" placeholder="Nhập địa chỉ..."><?php echo htmlspecialchars($row['dia_chi'] ?? ''); ?></textarea>
          
          <label>Mã số thuế</label>
          <input type="text" name="ma_so_thue" value="<?php echo htmlspecialchars($row['ma_so_thue'] ?? ''); ?>" placeholder="Mã số thuế (nếu có)" />
          
          <label>Người liên hệ</label>
          <input type="text" name="nguoi_lien_he" value="<?php echo htmlspecialchars($row['nguoi_lien_he'] ?? ''); ?>" placeholder="Tên người liên hệ" />
          
          <label>Trạng thái</label>
          <select name="trang_thai">
            <option value="dang_hop_tac" <?php echo $row['trang_thai'] === 'dang_hop_tac' ? 'selected' : ''; ?>>Đang hợp tác</option>
            <option value="ngung_hop_tac" <?php echo $row['trang_thai'] === 'ngung_hop_tac' ? 'selected' : ''; ?>>Ngừng hợp tác</option>
          </select>
          
          <label>Ghi chú</label>
          <textarea name="ghi_chu" rows="3" placeholder="Ghi chú về nhà cung cấp..."><?php echo htmlspecialchars($row['ghi_chu'] ?? ''); ?></textarea>
          
          <div style="margin-top:12px;">
            <button class="btn btn-primary" type="submit">Lưu thay đổi</button> 
            <a class="btn btn-secondary" href="index.php">Hủy</a>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>

