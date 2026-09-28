<div class="container-main">
    <div class="header-bar-nglieu">
      <div class="left">
        <a href="nguyenlieu.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách</a>
        <h1><span class="ic"><i class="fas fa-edit"></i></span> SỬA NGUYÊN LIỆU</h1>
      </div>
    </div>
    <div class="content-wrap">
      <div class="tbl-wrap">
        <?php if(isset($notice) && $notice): ?>
          <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
        <?php endif; ?>
        <form method="post">
          <div class="add-form">
            <h3>Thông tin nguyên liệu</h3>
            <label>Tên nguyên liệu *</label>
            <input type="text" name="ten_nguyenlieu" required value="<?php echo htmlspecialchars($row['ten_nguyenlieu']); ?>" />
            
            <label>Nhóm nguyên liệu *</label>
            <select name="danhmuc_nl" required>
              <?php if(isset($danhmuc)): foreach($danhmuc as $k=>$v): ?>
                <option value="<?php echo $k; ?>" <?php if($row['danhmuc_nl']==$k)echo 'selected';?>><?php echo $v; ?></option>
              <?php endforeach; endif; ?>
            </select>
            
            <label>Đơn vị tính *</label>
            <input type="text" name="don_vi_tinh" required value="<?php echo htmlspecialchars($row['don_vi_tinh']); ?>" placeholder="vd: g, ml, cốc" />
            
            <label>Số lượng tối thiểu</label>
            <input type="number" step="0.01" name="so_luong_toi_thieu" value="<?php echo $row['so_luong_toi_thieu'] ?? 10.00; ?>" />
            
            <label>Giá nhập (tùy chọn)</label>
            <input type="number" step="0.01" name="gia_nhap" value="<?php echo $row['gia_nhap'] ?? ''; ?>" placeholder="VND" />
            
            <label>Trạng thái</label>
            <select name="trang_thai">
              <option value="con_hang" <?php if($row['trang_thai']=='con_hang')echo 'selected';?>>Còn hàng</option>
              <option value="het_hang" <?php if($row['trang_thai']=='het_hang')echo 'selected';?>>Hết hàng</option>
              <option value="ngung_nhap" <?php if($row['trang_thai']=='ngung_nhap')echo 'selected';?>>Ngừng nhập</option>
            </select>
            
            <div style="margin-top:12px;">
              <button class="btn btn-primary" type="submit">Lưu thay đổi</button>
              <a class="btn btn-secondary" href="nguyenlieu.php">Hủy</a>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>

