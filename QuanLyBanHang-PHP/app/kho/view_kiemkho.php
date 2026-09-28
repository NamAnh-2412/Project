<?php
require_once '../core/bootstrap.php';
requireLogin();

try {
    $id = (int)get('id', 0);
    if ($id <= 0) {
        setFlashMessage('error', 'ID phiếu không hợp lệ.');
        redirect('index.php?tab=kiemkho');
    }
    
    // Lấy thông tin phiếu
    $sql = "SELECT kk.*, nv.ten_nhanvien 
            FROM kiemkho kk 
            LEFT JOIN nhanvien nv ON kk.nhanvien_kiem = nv.id 
            WHERE kk.id = ?";
    $phieu = db_fetch_one($sql, 'i', [$id]);
    
    if (!$phieu) {
        setFlashMessage('error', 'Không tìm thấy phiếu kiểm kho.');
        redirect('index.php?tab=kiemkho');
    }
    
    // Lấy chi tiết
    $sql_chitiet = "SELECT ck.*, nl.ma_nguyenlieu, nl.ten_nguyenlieu, nl.don_vi_tinh 
                    FROM chitiet_kiemkho ck 
                    JOIN nguyenvatlieu nl ON ck.nguyenlieu_id = nl.id 
                    WHERE ck.kiemkho_id = ? 
                    ORDER BY nl.ten_nguyenlieu";
    $chitiet = db_fetch_all($sql_chitiet, 'i', [$id]);
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('kho/view_kiemkho', [
        'phieu' => $phieu,
        'chitiet' => $chitiet,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Chi tiết phiếu kiểm kho: ' . $phieu['ma_phieu_kiem'],
        'page_css' => ['inventory.css']
    ]);
} catch (Exception $e) {
    error_log("Error in view_kiemkho: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php?tab=kiemkho');
}
?>
