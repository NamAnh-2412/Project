<div class="container-main">
    <div class="header-bar-nglieu">
      <div class="left">
        <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
        <h1><span class="ic"><i class="fas fa-flask"></i></span> NGUYÊN LIỆU</h1>
      </div>
      <div class="action">
        <a class="btn-add" href="nguyenlieu.php?add=1"><i class="fa fa-plus"></i> Thêm nguyên liệu</a>
      </div>
    </div>
    <div class="content-wrap">
      <form method="get" class="filter-box">
        <div>
          <label>Loại nguyên liệu:</label>
          <select name="loai" onchange="this.form.submit()">
            <option value="">Tất cả</option>
            <?php if(isset($danhmuc)): foreach($danhmuc as $k=>$v): ?>
              <option value="<?php echo $k; ?>" <?php if(isset($filter) && $filter===$k)echo 'selected';?> ><?php echo $v;?></option>
            <?php endforeach; endif; ?>
          </select>
        </div>
      </form>
      <div class="tbl-wrap">
        <?php 
        // Hiển thị flash messages
        $flashMessages = getFlashMessages();
        foreach ($flashMessages as $msg): 
        ?>
          <div class="alert alert-<?php echo $msg['type'] === 'success' ? 'success' : 'danger'; ?>">
            <?php echo htmlspecialchars($msg['message']); ?>
          </div>
        <?php endforeach; ?>
        
        <?php if(isset($_GET['success'])): ?>
          <div class="alert alert-success">Thao tác thành công!</div>
        <?php endif; ?>
        <?php if(isset($_GET['deleted'])): ?>
          <div class="alert alert-success">Đã xóa nguyên liệu thành công!</div>
        <?php endif; ?>
        <?php if(isset($notice) && $notice): ?>
          <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
        <?php endif; ?>
        <?php if(isset($showAddForm) && $showAddForm): ?>
        <div class="add-form">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
            <h3>Thêm nguyên liệu</h3>
            <a href="nguyenlieu.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : '') . (!empty($filter) ? (!empty($kw) ? '&' : '?') . 'loai=' . urlencode($filter) : ''); ?>" style="color:#666;text-decoration:none;font-size:14px;">
              <i class="fa fa-times"></i> Đóng
            </a>
          </div>
          <form method="post">
            <input type="hidden" name="action" value="add_nguyenlieu">
            <?php 
            // Giữ lại giá trị form nếu có lỗi
            $ten_nguyenlieu = isset($_POST['ten_nguyenlieu']) ? htmlspecialchars($_POST['ten_nguyenlieu']) : '';
            $danhmuc_nl = isset($_POST['danhmuc_nl']) ? $_POST['danhmuc_nl'] : '';
            $don_vi_tinh = isset($_POST['don_vi_tinh']) ? htmlspecialchars($_POST['don_vi_tinh']) : '';
            $trang_thai = isset($_POST['trang_thai']) ? $_POST['trang_thai'] : 'con_hang';
            ?>
            <input type="text" name="ten_nguyenlieu" placeholder="Tên nguyên liệu" required value="<?php echo $ten_nguyenlieu; ?>">
            <select name="danhmuc_nl" required>
              <option value="">-- Chọn nhóm --</option>
              <?php if(isset($danhmuc)): foreach($danhmuc as $k=>$v): ?>
                <option value="<?php echo $k; ?>" <?php echo ($danhmuc_nl === $k) ? 'selected' : ''; ?>><?php echo $v; ?></option>
              <?php endforeach; endif; ?>
            </select>
            <input type="text" name="don_vi_tinh" placeholder="Đơn vị (vd: g, ml, cốc)" required value="<?php echo $don_vi_tinh; ?>">
            <select name="trang_thai">
              <option value="con_hang" <?php echo ($trang_thai === 'con_hang') ? 'selected' : ''; ?>>Còn hàng</option>
              <option value="het_hang" <?php echo ($trang_thai === 'het_hang') ? 'selected' : ''; ?>>Hết hàng</option>
              <option value="ngung_nhap" <?php echo ($trang_thai === 'ngung_nhap') ? 'selected' : ''; ?>>Ngừng nhập</option>
            </select>
            <div style="display:flex;gap:10px;margin-top:10px;">
              <button class="btn btn-primary" type="submit">Lưu</button>
              <a href="nguyenlieu.php<?php echo (!empty($kw) ? '?kw=' . urlencode($kw) : '') . (!empty($filter) ? (!empty($kw) ? '&' : '?') . 'loai=' . urlencode($filter) : ''); ?>" class="btn" style="text-decoration:none;display:inline-block;">Hủy</a>
            </div>
          </form>
        </div>
        <?php endif; ?>
        <div class="searchbar">
          <form method="get" style="display:inline;">
            <input type="hidden" name="loai" value="<?php echo htmlspecialchars($filter ?? '');?>"/>
            <input type="text" name="kw" placeholder="Tìm tên nguyên liệu..." value="<?php echo htmlspecialchars($kw ?? '');?>"/>
          </form>
        </div>
        <table>
          <thead><tr><th>STT</th><th>Tên nguyên liệu</th><th>Nhóm</th><th>Đơn vị</th><th>Tiêu chuẩn lưu</th><th>Trạng thái</th><th>Tác vụ</th></tr></thead>
          <tbody>
          <?php if(isset($ds) && $ds): $i=1; foreach($ds as $row): ?>
            <tr>
              <td><?php echo $i++;?></td>
              <td><?php echo htmlspecialchars($row['ten_nguyenlieu']);?></td>
              <td><?php echo isset($danhmuc) && isset($danhmuc[$row['danhmuc_nl']]) ? $danhmuc[$row['danhmuc_nl']] : 'Khác';?></td>
              <td><?php echo $row['don_vi_tinh'];?></td>
              <td>-</td>
              <td class="status">
                <span<?php if($row['trang_thai']!='con_hang')echo ' class="off"';?>><?php echo $row['trang_thai']=='con_hang'?'Hoạt động':'Ngừng';?></span>
              </td>
              <td class="act-link">
                <a href="sua_nguyenlieu.php?id=<?php echo $row['id']; ?>">Sửa</a>
                <a href="xoa_nguyenlieu.php?id=<?php echo $row['id']; ?>" onclick="return confirm('Bạn có chắc muốn xóa nguyên liệu này?')">Xóa</a>
              </td>
            </tr>
          <?php endforeach; endif; ?>
          </tbody>
        </table>
      </div>
    </div>
  </div>

