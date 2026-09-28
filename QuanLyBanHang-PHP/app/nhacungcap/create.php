<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$notice = '';

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
                "INSERT INTO nhacungcap (ten_nhacungcap, so_dien_thoai, email, dia_chi, ma_so_thue, nguoi_lien_he, trang_thai, ghi_chu) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                'ssssssss',
                [$ten, $sdt, $email, $dia_chi, $ma_so_thue, $nguoi_lien_he, $trang_thai, $ghi_chu]
            );
            
            if ($result) {
                setFlashMessage('success', 'Thêm nhà cung cấp thành công!');
                redirect('index.php');
            } else {
                $notice = 'Lỗi khi lưu nhà cung cấp. Vui lòng thử lại.';
            }
        } catch (Exception $e) {
            error_log("Error creating supplier: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
        }
    }
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhacungcap/create', [
    'notice' => $notice,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Thêm nhà cung cấp',
    'page_css' => ['ui.css', 'customers.css']
]);
?>

