/**
 * Quản lý size khi thêm sản phẩm
 */

// State quản lý sizes
let sizes = [];

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', function() {
    const hasSizesCheckbox = document.getElementById('hasSizes');
    const sizesSection = document.getElementById('sizesSection');
    const btnAddSize = document.getElementById('btnAddSize');
    const addProductForm = document.getElementById('addProductForm');
    
    if (!hasSizesCheckbox || !sizesSection || !btnAddSize || !addProductForm) {
        return; // Không có form thêm sản phẩm
    }
    
    // Toggle hiển thị section size
    hasSizesCheckbox.addEventListener('change', function() {
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
});

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
function editSize(index) {
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
}

// Xóa size
function deleteSize(index) {
    if (index < 0 || index >= sizes.length) return;
    
    if (confirm('Bạn có chắc muốn xóa size này?')) {
        sizes.splice(index, 1);
        renderSizesList();
        updateSizesInput();
    }
}

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

