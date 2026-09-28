/**
 * Hàm changeMainImage - Đổi ảnh chính khi click vào thumbnail
 * @param {string} src - Đường dẫn ảnh mới
 */
function changeMainImage(src) {
    const mainImage = document.getElementById('mainImage');
    if (!mainImage) {
        console.warn('Room images: Main image element not found');
        return;
    }
    
    mainImage.src = src;
    
    // Update active thumbnail
    document.querySelectorAll('.thumbnail-images img').forEach(img => {
        img.classList.remove('active');
        if (img.src.includes(src.split('/').pop())) {
            img.classList.add('active');
        }
    });
}

