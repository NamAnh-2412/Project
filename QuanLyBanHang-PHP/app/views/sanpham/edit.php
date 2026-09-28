<div class="container-main">
  <div class="header-bar-sp">
    <div class="left">
      <a href="index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách sản phẩm</a>
      <h1><span class="ic"><i class="fas fa-edit"></i></span> SỬA SẢN PHẨM</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php 
      $flashMessages = getFlashMessages();
      foreach ($flashMessages as $msg): 
      ?>
        <div class="alert alert-<?php echo $msg['type'] === 'success' ? 'success' : 'danger'; ?>">
          <?php echo htmlspecialchars($msg['message']); ?>
        </div>
      <?php endforeach; ?>
      
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      
      <form method="post" enctype="multipart/form-data">
        <input type="hidden" name="action" value="update_sp">
        <div class="add-form">
          <h3>Thông tin sản phẩm</h3>
          
          <label>Ảnh sản phẩm</label>
          <?php if (!empty($product['anh_url'])): ?>
            <div style="margin-bottom:10px;">
              <img src="<?php echo getBaseUrl() . htmlspecialchars($product['anh_url']); ?>" 
                   alt="Ảnh hiện tại" 
                   style="max-width:200px;max-height:200px;border-radius:8px;border:1px solid #d2d8ea;display:block;margin-bottom:10px;">
              <small style="color:#64748b;">Ảnh hiện tại</small>
            </div>
          <?php endif; ?>
          <input type="file" name="anh_url" id="anh_url" accept="image/jpeg,image/png,image/gif,image/webp" onchange="previewImage(this)">
          <div id="imagePreview" style="margin-top:10px;display:none;">
            <img id="previewImg" src="" alt="Preview" style="max-width:200px;max-height:200px;border-radius:8px;border:1px solid #d2d8ea;">
            <small style="color:#64748b;display:block;margin-top:5px;">Ảnh mới (sẽ thay thế ảnh cũ)</small>
          </div>
          <small style="color:#64748b;display:block;margin-top:5px;">Chấp nhận: JPG, PNG, GIF, WEBP (tối đa 5MB)</small>
          
          <label>Mã sản phẩm</label>
          <input type="text" value="<?php echo htmlspecialchars($product['ma_sanpham']); ?>" disabled style="background:#f5f5f5">
          
          <label>Tên sản phẩm <span style="color:red">*</span></label>
          <input type="text" name="ten_sanpham" required value="<?php echo htmlspecialchars($product['ten_sanpham']); ?>" placeholder="Nhập tên sản phẩm">
          
          <label>Danh mục <span style="color:red">*</span></label>
          <select name="danhmuc_id" required>
            <option value="">-- Chọn danh mục --</option>
            <?php if(isset($dm_arr)): foreach($dm_arr as $dm): ?>
              <option value="<?php echo $dm['id']; ?>" <?php echo ($product['danhmuc_id'] == $dm['id']) ? 'selected' : ''; ?>>
                <?php echo htmlspecialchars($dm['ten_danhmuc']); ?>
              </option>
            <?php endforeach; endif; ?>
          </select>
          
          <label>Giá bán <span style="color:red">*</span></label>
          <input type="number" step="0.01" name="gia_ban" required value="<?php echo $product['gia_ban']; ?>" placeholder="0" min="0">
          
          <label>Mô tả</label>
          <textarea name="mota" rows="3" placeholder="Mô tả sản phẩm (tùy chọn)"><?php echo htmlspecialchars($product['mota'] ?? ''); ?></textarea>
          
          <label>Trạng thái</label>
          <select name="dang_hoat_dong">
            <option value="1" <?php echo $product['dang_hoat_dong'] ? 'selected' : ''; ?>>Đang bán</option>
            <option value="0" <?php echo !$product['dang_hoat_dong'] ? 'selected' : ''; ?>>Ngừng bán</option>
          </select>
          
          <!-- Section quản lý size -->
          <div style="margin-top:30px; padding:20px; border:1px solid #d2d8ea; border-radius:8px; background:#f7fafc;">
            <h4 style="margin-top:0; margin-bottom:15px; color:#22334b;">
              <i class="fas fa-ruler"></i> Quản lý kích thước
            </h4>
            
            <div id="sizesList">
              <?php if(empty($sizes)): ?>
                <p style="color:#64748b; margin:10px 0;">Chưa có size nào. Click "Thêm size mới" để thêm.</p>
              <?php else: ?>
                <table style="width:100%; border-collapse:collapse; background:#fff; border-radius:6px; overflow:hidden;">
                  <thead>
                    <tr style="background:#f7fafc; border-bottom:1px solid #d2d8ea;">
                      <th style="padding:10px; text-align:left; font-weight:600; color:#22334b;">Tên size</th>
                      <th style="padding:10px; text-align:right; font-weight:600; color:#22334b;">Giá tăng thêm</th>
                      <th style="padding:10px; text-align:center; font-weight:600; color:#22334b;">Trạng thái</th>
                      <th style="padding:10px; text-align:center; font-weight:600; color:#22334b;">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <?php foreach($sizes as $size): ?>
                      <tr style="border-bottom:1px solid #f0f0f0;" data-size-id="<?php echo $size['id']; ?>">
                        <td style="padding:12px; font-weight:600; color:#22334b;"><?php echo htmlspecialchars($size['ten_size']); ?></td>
                        <td style="padding:12px; text-align:right; color:#28a745; font-weight:600;"><?php echo number_format($size['gia'], 0, ',', '.'); ?>₫</td>
                        <td style="padding:12px; text-align:center;">
                          <span style="padding:4px 12px; border-radius:999px; font-size:.88em; font-weight:600; <?php echo $size['dang_hoat_dong'] ? 'background:#d4edda; color:#155724;' : 'background:#f8d7da; color:#721c24;'; ?>">
                            <?php echo $size['dang_hoat_dong'] ? 'Hoạt động' : 'Đã ẩn'; ?>
                          </span>
                        </td>
                        <td style="padding:12px; text-align:center;">
                          <button type="button" onclick="editSize(<?php echo $size['id']; ?>, '<?php echo htmlspecialchars($size['ten_size'], ENT_QUOTES); ?>', <?php echo $size['gia']; ?>)" class="btn-small" style="padding:4px 12px; background:#2196f3; color:#fff; border:none; border-radius:4px; cursor:pointer; margin-right:4px;">Sửa</button>
                          <button type="button" onclick="deleteSize(<?php echo $size['id']; ?>)" class="btn-small btn-danger" style="padding:4px 12px; background:#dc3545; color:#fff; border:none; border-radius:4px; cursor:pointer;">Xóa</button>
                        </td>
                      </tr>
                    <?php endforeach; ?>
                  </tbody>
                </table>
              <?php endif; ?>
            </div>
            
            <button type="button" id="btnAddSize" class="btn btn-secondary" style="margin-top:15px;">
              <i class="fa fa-plus"></i> Thêm size mới
            </button>
          </div>
          
          <!-- Section nhóm lựa chọn -->
          <div style="margin-top:30px; padding:20px; border:1px solid #d2d8ea; border-radius:8px; background:#f7fafc;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
              <h4 style="margin:0; color:#22334b;">
                <i class="fas fa-list-check"></i> Nhóm lựa chọn đã liên kết
              </h4>
              <a href="lienket_luachon.php?id=<?php echo $product['id']; ?>" class="btn btn-secondary" style="text-decoration:none;display:inline-block;padding:6px 12px;font-size:14px;">
                <i class="fa fa-link"></i> Quản lý liên kết
              </a>
            </div>
            
            <?php if(empty($linkedGroups)): ?>
              <p style="color:#64748b; margin:10px 0;">Chưa có nhóm lựa chọn nào được liên kết. 
                <a href="lienket_luachon.php?id=<?php echo $product['id']; ?>" style="color:#2196f3;text-decoration:underline;">Liên kết ngay</a>
              </p>
            <?php else: ?>
              <div style="display:grid;grid-template-columns:repeat(auto-fill, minmax(280px, 1fr));gap:12px;margin-top:15px;">
                <?php foreach($linkedGroups as $group): ?>
                  <div style="border:1px solid #d2d8ea;border-radius:6px;padding:12px;background:#fff;">
                    <div style="display:flex;align-items:start;justify-content:space-between;">
                      <div style="flex:1;">
                        <div style="font-weight:600;color:#22334b;margin-bottom:4px;">
                          <?php echo htmlspecialchars($group['ten_nhom']); ?>
                        </div>
                        <?php if(!empty($group['mota'])): ?>
                          <div style="font-size:12px;color:#64748b;margin-bottom:6px;">
                            <?php echo htmlspecialchars($group['mota']); ?>
                          </div>
                        <?php endif; ?>
                        <?php if($group['bat_buoc']): ?>
                          <span style="display:inline-block;padding:2px 8px;background:#fff3cd;color:#856404;border-radius:999px;font-size:11px;font-weight:600;margin-top:4px;">
                            <i class="fa fa-exclamation-circle"></i> Bắt buộc
                          </span>
                        <?php else: ?>
                          <span style="display:inline-block;padding:2px 8px;background:#e7f3ff;color:#004085;border-radius:999px;font-size:11px;font-weight:600;margin-top:4px;">
                            Tùy chọn
                          </span>
                        <?php endif; ?>
                      </div>
                    </div>
                  </div>
                <?php endforeach; ?>
              </div>
            <?php endif; ?>
          </div>
          
          <div style="margin-top:20px;display:flex;gap:10px">
            <button class="btn btn-primary" type="submit">Lưu thay đổi</button>
            <a href="index.php" class="btn btn-secondary" style="text-decoration:none;display:inline-block;">Hủy</a>
          </div>
        </div>
      </form>
      
      <script>
      const productId = <?php echo $product['id']; ?>;
      const basePath = '<?php echo getBaseUrl(); ?>';
      
      // Thêm size mới
      document.getElementById('btnAddSize').addEventListener('click', function() {
        const tenSize = prompt('Nhập tên size (ví dụ: M, L, XL):');
        if (!tenSize || !tenSize.trim()) return;
        
        const giaSize = prompt('Nhập giá tăng thêm (₫):', '0');
        const gia = parseFloat(giaSize) || 0;
        
        if (gia < 0) {
          alert('Giá không được âm!');
          return;
        }
        
        fetch(basePath + 'app/api/manage_size.php', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            action: 'add',
            product_id: productId,
            ten_size: tenSize.trim(),
            gia: gia
          })
        })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            location.reload();
          } else {
            alert(data.message || 'Có lỗi xảy ra');
          }
        })
        .catch(err => {
          console.error('Error:', err);
          alert('Có lỗi xảy ra khi thêm size');
        });
      });
      
      // Sửa size
      function editSize(sizeId, currentTenSize, currentGia) {
        const tenSize = prompt('Sửa tên size:', currentTenSize);
        if (!tenSize || !tenSize.trim()) return;
        
        const giaSize = prompt('Sửa giá tăng thêm (₫):', currentGia);
        const gia = parseFloat(giaSize);
        
        if (isNaN(gia) || gia < 0) {
          alert('Giá không hợp lệ!');
          return;
        }
        
        fetch(basePath + 'app/api/manage_size.php', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            action: 'update',
            product_id: productId,
            size_id: sizeId,
            ten_size: tenSize.trim(),
            gia: gia
          })
        })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            location.reload();
          } else {
            alert(data.message || 'Có lỗi xảy ra');
          }
        })
        .catch(err => {
          console.error('Error:', err);
          alert('Có lỗi xảy ra khi sửa size');
        });
      }
      
      // Xóa size
      function deleteSize(sizeId) {
        if (!confirm('Bạn có chắc muốn xóa size này?')) return;
        
        fetch(basePath + 'app/api/manage_size.php', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            action: 'delete',
            product_id: productId,
            size_id: sizeId
          })
        })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            alert(data.message);
            location.reload();
          } else {
            alert(data.message || 'Có lỗi xảy ra');
          }
        })
        .catch(err => {
          console.error('Error:', err);
          alert('Có lỗi xảy ra khi xóa size');
        });
      }
      
      // Preview image function
      function previewImage(input) {
        const preview = document.getElementById('imagePreview');
        const previewImg = document.getElementById('previewImg');
        
        if (input.files && input.files[0]) {
          const reader = new FileReader();
          reader.onload = function(e) {
            previewImg.src = e.target.result;
            preview.style.display = 'block';
          };
          reader.readAsDataURL(input.files[0]);
        } else {
          preview.style.display = 'none';
        }
      }
      </script>
    </div>
  </div>
</div>

