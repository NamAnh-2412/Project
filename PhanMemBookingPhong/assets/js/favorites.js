/**
 * Hàm toggleFavorite - Xử lý yêu thích/bỏ yêu thích phòng
 * @param {number} phongId - ID của phòng
 * @param {HTMLElement} btn - Nút yêu thích được click
 * @param {Object} options - Các tùy chọn
 * @param {boolean} options.removeCardOnUnfavorite - Xóa card khi bỏ yêu thích (dùng cho favorites.php)
 * @param {boolean} options.changeInnerHTML - Đổi innerHTML của nút (dùng cho room_detail.php)
 */
function toggleFavorite(phongId, btn, options = {}) {
    const { removeCardOnUnfavorite = false, changeInnerHTML = false } = options;
    
    fetch('favorite_action.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'phong_id=' + phongId + '&action=toggle'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            if (data.favorited) {
                btn.classList.add('active');
                if (changeInnerHTML) {
                    btn.innerHTML = '❤️ Đã yêu thích';
                }
                btn.title = 'Bỏ yêu thích';
            } else {
                btn.classList.remove('active');
                if (changeInnerHTML) {
                    btn.innerHTML = '🤍 Yêu thích';
                }
                btn.title = 'Yêu thích';
                
                // Xóa card nếu cần (dùng cho trang favorites.php)
                if (removeCardOnUnfavorite) {
                    btn.closest('.room-card').remove();
                    // Nếu không còn phòng nào, hiển thị thông báo
                    if (document.querySelectorAll('.room-card').length === 0) {
                        document.querySelector('.rooms-grid').innerHTML = '<p class="no-results">Bạn chưa có phòng yêu thích nào.</p><a href="index.php" class="btn btn-primary">Tìm phòng ngay</a>';
                    }
                }
            }
        } else {
            alert(data.message || 'Có lỗi xảy ra');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra');
    });
}

