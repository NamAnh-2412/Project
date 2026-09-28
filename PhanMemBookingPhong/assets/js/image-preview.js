/**
 * Khởi tạo preview ảnh trước khi upload
 * @param {string} fileInputId - ID của input file
 * @param {string} previewDivId - ID của div hiển thị preview
 * @param {Object} options - Các tùy chọn
 * @param {boolean} options.showWarning - Hiển thị cảnh báo (dùng cho edit_room.php)
 */
function initImagePreview(fileInputId, previewDivId, options = {}) {
    const { showWarning = false } = options;
    const fileInput = document.getElementById(fileInputId);
    const previewDiv = document.getElementById(previewDivId);
    
    if (!fileInput || !previewDiv) {
        console.warn('Image preview: Elements not found');
        return;
    }
    
    fileInput.addEventListener('change', function(e) {
        previewDiv.innerHTML = '';
        const files = e.target.files;
        
        if (files.length > 0) {
            const title = document.createElement('p');
            title.style.fontWeight = 'bold';
            title.style.marginBottom = '10px';
            
            if (showWarning) {
                title.style.color = '#e74c3c';
                title.textContent = `⚠️ Ảnh mới sẽ thay thế ảnh cũ. Đã chọn ${files.length} ảnh mới:`;
            } else {
                title.textContent = `Đã chọn ${files.length} ảnh:`;
            }
            
            previewDiv.appendChild(title);
            
            const previewContainer = document.createElement('div');
            previewContainer.style.display = 'grid';
            previewContainer.style.gridTemplateColumns = 'repeat(auto-fill, minmax(150px, 1fr))';
            previewContainer.style.gap = '10px';
            previewContainer.style.marginTop = '10px';
            
            Array.from(files).forEach((file, index) => {
                if (file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const imgContainer = document.createElement('div');
                        imgContainer.style.position = 'relative';
                        imgContainer.style.border = '2px solid #ddd';
                        imgContainer.style.borderRadius = '4px';
                        imgContainer.style.overflow = 'hidden';
                        
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.style.width = '100%';
                        img.style.height = '150px';
                        img.style.objectFit = 'cover';
                        img.style.display = 'block';
                        
                        const label = document.createElement('div');
                        label.style.padding = '5px';
                        label.style.background = index === 0 ? '#3498db' : '#95a5a6';
                        label.style.color = 'white';
                        label.style.fontSize = '12px';
                        label.style.textAlign = 'center';
                        label.textContent = index === 0 ? 'Ảnh chính' : `Ảnh ${index + 1}`;
                        
                        imgContainer.appendChild(img);
                        imgContainer.appendChild(label);
                        previewContainer.appendChild(imgContainer);
                    };
                    reader.readAsDataURL(file);
                }
            });
            
            previewDiv.appendChild(previewContainer);
        }
    });
}

// Tự động khởi tạo khi DOM ready (nếu có các element với ID mặc định và data attribute)
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('images_upload');
    const previewDiv = document.getElementById('image-preview');
    
    if (fileInput && previewDiv) {
        // Kiểm tra xem có phải trang edit không qua data attribute hoặc URL
        const isEditPage = previewDiv.dataset.showWarning === 'true' || 
                          window.location.pathname.includes('edit_room.php');
        initImagePreview('images_upload', 'image-preview', { 
            showWarning: isEditPage 
        });
    }
});

