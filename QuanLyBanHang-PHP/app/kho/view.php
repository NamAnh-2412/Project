<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $id = (int)get('id', 0);
    
    if ($id <= 0) {
        setFlashMessage('error', 'ID phiếu nhập kho không hợp lệ.');
        redirect('index.php');
    }
    
    // Get phiếu nhập kho
    $phieu = db_fetch_one(
        "SELECT nk.*, nv.ten_nhanvien, ncc.ten_nhacungcap, ncc.so_dien_thoai as ncc_sdt, ncc.email as ncc_email
         FROM nhapkho nk
         LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id
         LEFT JOIN nhacungcap ncc ON nk.nha_cung_cap_id = ncc.id
         WHERE nk.id = ?",
        'i',
        [$id]
    );
    
    if (!$phieu) {
        setFlashMessage('error', 'Không tìm thấy phiếu nhập kho.');
        redirect('index.php');
    }
    
    // Get chi tiết phiếu nhập kho
    $chitiet = db_fetch_all(
        "SELECT ct.*, nl.ten_nguyenlieu, nl.don_vi_tinh
         FROM chitiet_nhapkho ct
         JOIN nguyenvatlieu nl ON ct.nguyenlieu_id = nl.id
         WHERE ct.nhapkho_id = ?
         ORDER BY nl.ten_nguyenlieu",
        'i',
        [$id]
    );
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('kho/view', [
        'phieu' => $phieu,
        'chitiet' => $chitiet,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Chi tiết phiếu nhập kho',
        'page_css' => ['inventory.css']
    ]);
} catch (Exception $e) {
    error_log("Error in kho/view: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải chi tiết phiếu nhập kho.');
    redirect('index.php');
}
?>

