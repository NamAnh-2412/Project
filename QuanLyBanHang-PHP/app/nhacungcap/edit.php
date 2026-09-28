<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

$notice = '';

// Get supplier data
$row = db_fetch_one("SELECT * FROM nhacungcap WHERE id = ?", 'i', [$id]);
if (!$row) {
    setFlashMessage('error', 'Không tìm thấy nhà cung cấp.');
    redirect('index.php');
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $ten = post('ten_nhacungcap');
    $sdt = post('so_dien_thoai');
    $email = post('email', '', 'email');
    $dia_chi = post('dia_chi');
    $ma_so_thue = post('ma_so_thue');
    $nguoi_lien_he = post('nguoi_lien_he');
    $trang_thai = post('trang_thai', 'dang_hop_tac');
    $ghi_chu = post('ghi_chu');
    
    // Validation
    if (empty($ten)) {
        $notice = 'Vui lòng nhập tên nhà cung cấp.';
    } elseif (!empty($sdt) && !isValidPhone($sdt)) {
        $notice = 'Số điện thoại không hợp lệ.';
    } elseif (!empty($email) && !isValidEmail($email)) {
        $notice = 'Email không hợp lệ.';
    } else {
        try {
            $result = db_query(
                "UPDATE nhacungcap SET ten_nhacungcap = ?, so_dien_thoai = ?, email = ?, dia_chi = ?, ma_so_thue = ?, nguoi_lien_he = ?, trang_thai = ?, ghi_chu = ? WHERE id = ?",
                'ssssssssi',
                [$ten, $sdt, $email, $dia_chi, $ma_so_thue, $nguoi_lien_he, $trang_thai, $ghi_chu, $id]
            );
            
            if ($result) {
                setFlashMessage('success', 'Cập nhật nhà cung cấp thành công!');
                redirect('index.php');
            } else {
                $notice = 'Lỗi khi cập nhật nhà cung cấp. Vui lòng thử lại.';
            }
        } catch (Exception $e) {
            error_log("Error updating supplier: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
        }
    }
    
    // Reload data if there was an error
    $row = db_fetch_one("SELECT * FROM nhacungcap WHERE id = ?", 'i', [$id]);
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhacungcap/edit', [
    'row' => $row,
    'notice' => $notice,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Sửa nhà cung cấp',
    'page_css' => ['ui.css', 'customers.css']
]);
?>

