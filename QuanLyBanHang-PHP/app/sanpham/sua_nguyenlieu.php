<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('nguyenlieu.php');
}

$notice = '';
$danhmuc = ['tra' => 'Trà', 'mứt' => 'Mứt', 'thạch' => 'Thạch', 'sữa' => 'Sữa', 'đường' => 'Đường', 'topping' => 'Topping', 'ly_cốc' => 'Ly/cốc', 'khác' => 'Khác'];

try {
    // Get ingredient data
    $row = db_fetch_one("SELECT * FROM nguyenvatlieu WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nguyên liệu.');
        redirect('nguyenlieu.php');
    }
    
    // Handle form submission
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $ten = post('ten_nguyenlieu');
        $nhom = post('danhmuc_nl');
        $donvi = post('don_vi_tinh');
        $trangthai = post('trang_thai', 'con_hang');
        $so_luong_toi_thieu = post('so_luong_toi_thieu', 10.00, 'float');
        $gia_nhap_raw = post('gia_nhap', '', 'raw');
        $gia_nhap = !empty($gia_nhap_raw) ? (float)$gia_nhap_raw : null;
        
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên nguyên liệu.';
        } else {
            try {
                $result = db_query(
                    "UPDATE nguyenvatlieu SET ten_nguyenlieu = ?, danhmuc_nl = ?, don_vi_tinh = ?, trang_thai = ?, so_luong_toi_thieu = ?, gia_nhap = ? WHERE id = ?",
                    'ssssddi',
                    [$ten, $nhom, $donvi, $trangthai, $so_luong_toi_thieu, $gia_nhap, $id]
                );
                
                if ($result) {
                    setFlashMessage('success', 'Cập nhật nguyên liệu thành công!');
                    redirect('nguyenlieu.php');
                } else {
                    $notice = 'Lỗi khi cập nhật nguyên liệu.';
                }
            } catch (Exception $e) {
                error_log("Error updating ingredient: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
            }
        }
        
        // Reload data if there was an error
        $row = db_fetch_one("SELECT * FROM nguyenvatlieu WHERE id = ?", 'i', [$id]);
    }
} catch (Exception $e) {
    error_log("Error in sanpham/sua_nguyenlieu: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('nguyenlieu.php');
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('sanpham/sua_nguyenlieu', [
    'row' => $row,
    'notice' => $notice,
    'danhmuc' => $danhmuc,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Sửa nguyên liệu',
    'page_css' => ['ui.css', 'products.css'],
    'body_class' => 'products-page',
    'show_header' => false
]);
?>

