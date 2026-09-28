<div class="container-main">
  <div class="header-bar-sp">
    <div class="left">
      <a href="../../index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
      <h1><span class="ic"><i class="fas fa-box"></i></span> SẢN PHẨM</h1>
    </div>
    <a class="btn-add" href="index.php?add=1"><i class="fa fa-plus"></i> Thêm sản phẩm</a>
  </div>
  <div class="content-wrap">
    <form method="get" class="filter-box">
      <div>
        <label>Danh mục:</label>
        <select name="cat" onchange="this.form.submit()">
          <option value="">Tất cả</option>
          <?php if(isset($dm_arr)): foreach($dm_arr as $id=>$ten): ?>
            <option value="<?php echo $id; ?>" <?php if(isset($cat) && $cat==$id)echo 'selected';?> ><?php echo $ten;?></option>
          <?php endforeach; endif; ?>
        </select>
      </div>
    </form>
    <div class="tbl-wrap">
      <?php if(isset($_GET['success'])): ?>
        <div class="alert alert-success">Đã thêm sản phẩm!</div>
      <?php endif; ?>
      <?php if(isset($notice) && $notice): ?>
        <div class="alert alert-danger"><?php echo htmlspecialchars($notice); ?></div>
      <?php endif; ?>
      <?php if(isset($_GET['add'])): ?>
      <div class="add-form">
        <h3>Thêm sản phẩm</h3>
        <form method="post" id="addProductForm" enctype="multipart/form-data">
          <input type="hidden" name="action" value="add_sp">
          <input type="hidden" name="sizes" id="sizesInput" value="[]">
          
          <div class="form-group">
            <label>Ảnh sản phẩm</label>
            <input type="file" name="anh_url" id="anh_url" accept="image/jpeg,image/png,image/gif,image/webp" onchange="previewImage(this)">
            <div id="imagePreview" style="margin-top:10px;display:none;">
              <img id="previewImg" src="" alt="Preview" style="max-width:200px;max-height:200px;border-radius:8px;border:1px solid #d2d8ea;">
            </div>
            <small style="color:#64748b;display:block;margin-top:5px;">Chấp nhận: JPG, PNG, GIF, WEBP (tối đa 5MB)</small>
          </div>
          
          <div class="form-group">
            <label>Tên sản phẩm *</label>
            <input type="text" name="ten_sanpham" placeholder="Tên sản phẩm" required>
          </div>
          
          <div class="form-group">
            <label>Danh mục *</label>
            <select name="danhmuc_id" required>
              <option value="">-- Chọn danh mục --</option>
              <?php if(isset($dm_arr)): foreach($dm_arr as $id=>$ten): ?>
                <option value="<?php echo $id; ?>"><?php echo $ten;?></option>
              <?php endforeach; endif; ?>
            </select>
          </div>
          
          <div class="form-group">
            <label>Giá bán (giá size nhỏ nhất) *</label>
            <input type="number" step="0.01" name="gia_ban" placeholder="Giá bán" value="0" required>
          </div>
          
          <!-- Section quản lý size -->
          <div class="form-group">
            <label>
              <input type="checkbox" id="hasSizes" name="has_sizes" value="1">
              Sản phẩm có nhiều size
            </label>
          </div>
          
          <div id="sizesSection" style="display:none; margin-top:20px; padding:15px; border:1px solid #d2d8ea; border-radius:8px; background:#f7fafc;">
            <h4 style="margin-top:0; margin-bottom:15px; color:#22334b;">
              <i class="fas fa-ruler"></i> Quản lý kích thước
            </h4>
            
            <div id="sizesList">
              <p style="color:#64748b; margin:10px 0;">Chưa có size nào. Click "Thêm size mới" để thêm.</p>
            </div>
            
            <button type="button" id="btnAddSize" class="btn btn-secondary" style="margin-top:10px;">
              <i class="fa fa-plus"></i> Thêm size mới
            </button>
          </div>
          
          <div class="form-group">
            <label>Trạng thái</label>
            <select name="dang_hoat_dong">
              <option value="1">Đang bán</option>
              <option value="0">Ngừng</option>
            </select>
          </div>
          
          <div class="form-actions">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a href="index.php" class="btn btn-secondary">Hủy</a>
          </div>
        </form>
      </div>
      <?php endif; ?>
      <div class="searchbar">
        <form method="get" style="display:inline;">
          <input type="hidden" name="cat" value="<?php echo htmlspecialchars($cat ?? '');?>"/>
          <input type="text" name="kw" placeholder="Tìm tên sản phẩm..." value="<?php echo htmlspecialchars($kw ?? '');?>"/>
        </form>
      </div>
      <table>
        <thead>
          <tr><th>STT</th><th>Ảnh</th><th>Mã SP</th><th>Tên sản phẩm</th><th>Danh mục</th><th>Đơn vị</th><th>Loại SP</th><th>Trạng thái</th><th>Tác vụ</th></tr>
        </thead>
        <tbody>
        <?php if(isset($ds) && $ds): $i=1; foreach($ds as $row): ?>
          <tr>
            <td><?php echo $i++;?></td>
            <td style="text-align:center;">
              <?php if (!empty($row['anh_url'])): ?>
                <img src="<?php echo getBaseUrl() . htmlspecialchars($row['anh_url']); ?>" 
                     alt="<?php echo htmlspecialchars($row['ten_sanpham']); ?>" 
                     style="width:50px;height:50px;object-fit:cover;border-radius:4px;border:1px solid #d2d8ea;">
              <?php else: ?>
                <span style="color:#999;font-size:12px;">-</span>
              <?php endif; ?>
            </td>
            <td><?php echo htmlspecialchars($row['ma_sanpham']);?></td>
            <td><?php echo htmlspecialchars($row['ten_sanpham']);?></td>
            <td><?php echo isset($dm_arr) && isset($dm_arr[$row['danhmuc_id']]) ? $dm_arr[$row['danhmuc_id']] : '-';?></td>
            <td><?php echo $row['danhmuc_id']? 'Cốc':'-';?></td>
            <td><?php echo $row['danhmuc_id']? 'Đồ uống':'Khác';?></td>
            <td class="status">
              <span<?php if(!$row['dang_hoat_dong'])echo ' class="off"';?>><?php echo $row['dang_hoat_dong']?'Hoạt động':'Ngừng';?></span>
            </td>
            <td class="act-link">
              <a href="lienket_luachon.php?id=<?php echo $row['id']; ?>" title="Liên kết nhóm lựa chọn">
                <i class="fa fa-link"></i> Lựa chọn
              </a>
              <a href="edit.php?id=<?php echo $row['id']; ?>" title="Sửa sản phẩm">Sửa</a>
              <a href="delete.php?id=<?php echo $row['id']; ?>" title="Xóa sản phẩm" onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm này?');">Xóa</a>
            </td>
          </tr>
        <?php endforeach; endif; ?>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script>
// Inline script để đảm bảo chạy được
(function() {
  'use strict';
  
  // State quản lý sizes
  let sizes = [];
  
  // Khởi tạo khi DOM ready
  function initSizeManager() {
    const hasSizesCheckbox = document.getElementById('hasSizes');
    const sizesSection = document.getElementById('sizesSection');
    const btnAddSize = document.getElementById('btnAddSize');
    const addProductForm = document.getElementById('addProductForm');
    
    if (!hasSizesCheckbox || !sizesSection || !btnAddSize || !addProductForm) {
      console.log('Size manager elements not found');
      return; // Không có form thêm sản phẩm
    }
    
    console.log('Size manager initialized');
    
    // Toggle hiển thị section size
    hasSizesCheckbox.addEventListener('change', function() {
      console.log('Checkbox changed:', this.checked);
      sizesSection.style.display = this.checked ? 'block' : 'none';
      
      if (!this.checked) {
        sizes = [];
        renderSizesList();
      }
    });
    
    // Thêm size mới
    btnAddSize.addEventListener('click', function() {
      addNewSize();
    });
    
    // Submit form
    addProductForm.addEventListener('submit', function(e) {
      const hasSizes = hasSizesCheckbox.checked;
      
      if (hasSizes && sizes.length === 0) {
        e.preventDefault();
        alert('Vui lòng thêm ít nhất một size!');
        return false;
      }
      
      // Validate tên size không trùng
      const sizeNames = sizes.map(s => s.ten_size.toLowerCase().trim());
      const duplicates = sizeNames.filter((name, index) => sizeNames.indexOf(name) !== index);
      if (duplicates.length > 0) {
        e.preventDefault();
        alert('Có size bị trùng tên. Vui lòng kiểm tra lại!');
        return false;
      }
      
      updateSizesInput();
    });
    
    // Render lần đầu
    renderSizesList();
  }
  
  // Thêm size mới
  function addNewSize() {
    const tenSize = prompt('Nhập tên size (ví dụ: M, L, XL):');
    if (!tenSize || !tenSize.trim()) {
      return;
    }
    
    const tenSizeTrimmed = tenSize.trim();
    
    // Kiểm tra trùng tên
    const existing = sizes.find(s => s.ten_size.toLowerCase() === tenSizeTrimmed.toLowerCase());
    if (existing) {
      alert('Size này đã tồn tại!');
      return;
    }
    
    const giaSize = prompt('Nhập giá tăng thêm (₫):', '0');
    const gia = parseFloat(giaSize) || 0;
    
    if (gia < 0) {
      alert('Giá không được âm!');
      return;
    }
    
    sizes.push({
      ten_size: tenSizeTrimmed,
      gia: gia
    });
    
    renderSizesList();
    updateSizesInput();
  }
  
  // Render danh sách size
  function renderSizesList() {
    const container = document.getElementById('sizesList');
    if (!container) return;
    
    if (sizes.length === 0) {
      container.innerHTML = '<p style="color:#64748b; margin:10px 0;">Chưa có size nào. Click "Thêm size mới" để thêm.</p>';
      return;
    }
    
    let html = '<table style="width:100%; border-collapse:collapse; background:#fff; border-radius:6px; overflow:hidden;">';
    html += '<thead><tr style="background:#f7fafc; border-bottom:1px solid #d2d8ea;">';
    html += '<th style="padding:10px; text-align:left; font-weight:600; color:#22334b;">Tên size</th>';
    html += '<th style="padding:10px; text-align:right; font-weight:600; color:#22334b;">Giá tăng thêm</th>';
    html += '<th style="padding:10px; text-align:center; font-weight:600; color:#22334b;">Thao tác</th>';
    html += '</tr></thead>';
    html += '<tbody>';
    
    sizes.forEach((size, index) => {
      html += `
        <tr style="border-bottom:1px solid #f0f0f0;">
          <td style="padding:12px; font-weight:600; color:#22334b;">${escapeHtml(size.ten_size)}</td>
          <td style="padding:12px; text-align:right; color:#28a745; font-weight:600;">${formatMoney(size.gia)}₫</td>
          <td style="padding:12px; text-align:center;">
            <button type="button" onclick="editSize(${index})" class="btn-small" style="padding:4px 12px; background:#2196f3; color:#fff; border:none; border-radius:4px; cursor:pointer; margin-right:4px;">Sửa</button>
            <button type="button" onclick="deleteSize(${index})" class="btn-small btn-danger" style="padding:4px 12px; background:#dc3545; color:#fff; border:none; border-radius:4px; cursor:pointer;">Xóa</button>
          </td>
        </tr>
      `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
  }
  
  // Sửa size
  window.editSize = function(index) {
    if (index < 0 || index >= sizes.length) return;
    
    const size = sizes[index];
    const tenSize = prompt('Sửa tên size:', size.ten_size);
    if (!tenSize || !tenSize.trim()) {
      return;
    }
    
    const tenSizeTrimmed = tenSize.trim();
    
    // Kiểm tra trùng tên (trừ chính nó)
    const existing = sizes.find((s, i) => i !== index && s.ten_size.toLowerCase() === tenSizeTrimmed.toLowerCase());
    if (existing) {
      alert('Size này đã tồn tại!');
      return;
    }
    
    const giaSize = prompt('Sửa giá tăng thêm (₫):', size.gia);
    const gia = parseFloat(giaSize);
    
    if (isNaN(gia) || gia < 0) {
      alert('Giá không hợp lệ!');
      return;
    }
    
    sizes[index] = {
      ten_size: tenSizeTrimmed,
      gia: gia
    };
    
    renderSizesList();
    updateSizesInput();
  };
  
  // Xóa size
  window.deleteSize = function(index) {
    if (index < 0 || index >= sizes.length) return;
    
    if (confirm('Bạn có chắc muốn xóa size này?')) {
      sizes.splice(index, 1);
      renderSizesList();
      updateSizesInput();
    }
  };
  
  // Cập nhật input hidden
  function updateSizesInput() {
    const input = document.getElementById('sizesInput');
    if (input) {
      input.value = JSON.stringify(sizes);
    }
  }
  
  // Format money
  function formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
  }
  
  // Escape HTML
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
  
  // Khởi tạo khi DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      // Đợi một chút để đảm bảo DOM đã render xong
      setTimeout(initSizeManager, 100);
    });
  } else {
    // Đợi một chút để đảm bảo DOM đã render xong
    setTimeout(initSizeManager, 100);
  }
})();

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

