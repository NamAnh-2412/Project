<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

$notice = '';

// Get employee data
try {
    $row = db_fetch_one("SELECT * FROM nhanvien WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nhân viên.');
        redirect('index.php');
    }
    
    // Handle form submission
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $ma = post('ma_nhanvien');
        $ten = post('ten_nhanvien');
        $chuc = post('chuc_vu', ROLE_STAFF);
        $sdt = post('so_dien_thoai');
        $email = post('email', '', 'email');
        $ngay = post('ngay_vao_lam');
        $ngay_sinh = post('ngay_sinh');
        $trang = post('trang_thai', EMPLOYEE_ACTIVE);
        
        // Validation
        if (empty($ma) || empty($ten)) {
            $notice = 'Vui lòng nhập đầy đủ thông tin.';
        } elseif (!empty($sdt) && !isValidPhone($sdt)) {
            $notice = 'Số điện thoại không hợp lệ.';
        } elseif (!empty($email) && !isValidEmail($email)) {
            $notice = 'Email không hợp lệ.';
        } else {
            // Check if mã is duplicate (excluding current employee)
            $existing = db_fetch_one(
                "SELECT id FROM nhanvien WHERE ma_nhanvien = ? AND id != ? LIMIT 1",
                'si',
                [$ma, $id]
            );
            
            if ($existing) {
                $notice = 'Mã nhân viên đã tồn tại, vui lòng dùng mã khác.';
            } else {
                // Update employee
                $result = db_query(
                    "UPDATE nhanvien SET ma_nhanvien = ?, ten_nhanvien = ?, chuc_vu = ?, so_dien_thoai = ?, email = ?, ngay_vao_lam = ?, ngay_sinh = ?, trang_thai = ? WHERE id = ?",
                    'ssssssssi',
                    [$ma, $ten, $chuc, $sdt, $email ?: null, $ngay, $ngay_sinh ?: null, $trang, $id]
                );
                
                if ($result) {
                    setFlashMessage('success', 'Cập nhật nhân viên thành công!');
                    redirect('index.php');
                } else {
                    $notice = 'Lỗi khi cập nhật nhân viên. Vui lòng thử lại.';
                }
            }
        }
        
        // Reload data if there was an error
        $row = db_fetch_one("SELECT * FROM nhanvien WHERE id = ?", 'i', [$id]);
    }
} catch (Exception $e) {
    error_log("Error in nhanvien/edit: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php');
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhanvien/edit', [
    'row' => $row,
    'notice' => $notice,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Sửa nhân viên',
    'page_css' => ['ui.css', 'employees.css']
]);
?>
