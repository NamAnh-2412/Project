<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

$notice = '';
$hasEmail = false;
$hasGhiChu = false;
$hasNgaySinh = false;
$hasDiaChi = false;

// Check optional columns
try {
    $emailCol = db_fetch_one("SHOW COLUMNS FROM khachhang LIKE 'email'");
    $hasEmail = !empty($emailCol);
    
    $ghiChuCol = db_fetch_one("SHOW COLUMNS FROM khachhang LIKE 'ghi_chu'");
    $hasGhiChu = !empty($ghiChuCol);

    $ngaySinhCol = db_fetch_one("SHOW COLUMNS FROM khachhang LIKE 'ngay_sinh'");
    $hasNgaySinh = !empty($ngaySinhCol);

    $diaChiCol = db_fetch_one("SHOW COLUMNS FROM khachhang LIKE 'dia_chi'");
    $hasDiaChi = !empty($diaChiCol);
} catch (Exception $e) {
    error_log("Error checking columns: " . $e->getMessage());
}

// Get customer data
$row = db_fetch_one("SELECT * FROM khachhang WHERE id = ?", 'i', [$id]);
if (!$row) {
    setFlashMessage('error', 'Không tìm thấy khách hàng.');
    redirect('index.php');
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $ten = post('ten_khachhang');
    $sdt = post('so_dien_thoai');
    $email = $hasEmail ? post('email', '', 'email') : null;
    $ghi = $hasGhiChu ? post('ghi_chu') : null;
    $ngay_sinh = $hasNgaySinh ? post('ngay_sinh', '', 'raw') : null;
    $dia_chi = $hasDiaChi ? post('dia_chi') : null;
    
    // Validation
    if (empty($ten)) {
        $notice = 'Vui lòng nhập tên khách hàng.';
    } elseif (!empty($sdt) && !isValidPhone($sdt)) {
        $notice = 'Số điện thoại không hợp lệ.';
    } elseif ($hasEmail && !empty($email) && !isValidEmail($email)) {
        $notice = 'Email không hợp lệ.';
    } else {
        try {
            // Build SQL based on available columns
            $sets = ['ten_khachhang = ?', 'so_dien_thoai = ?'];
            $values = [$ten, $sdt];
            $types = 'ss';
            
            if ($hasEmail) {
                $sets[] = 'email = ?';
                $values[] = $email ?: null;
                $types .= 's';
            }
            
            if ($hasGhiChu) {
                $sets[] = 'ghi_chu = ?';
                $values[] = $ghi ?: null;
                $types .= 's';
            }

            if ($hasNgaySinh) {
                $sets[] = 'ngay_sinh = ?';
                $values[] = !empty($ngay_sinh) ? $ngay_sinh : null;
                $types .= 's';
            }

            if ($hasDiaChi) {
                $sets[] = 'dia_chi = ?';
                $values[] = $dia_chi ?: null;
                $types .= 's';
            }
            
            $values[] = $id;
            $types .= 'i';
            
            $sql = "UPDATE khachhang SET " . implode(', ', $sets) . " WHERE id = ?";
            $result = db_query($sql, $types, $values);
            
            if ($result) {
                setFlashMessage('success', 'Cập nhật khách hàng thành công!');
                redirect('index.php');
            } else {
                $notice = 'Lỗi khi cập nhật khách hàng. Vui lòng thử lại.';
            }
        } catch (Exception $e) {
            error_log("Error updating customer: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
        }
    }
    
    // Reload data if there was an error
    $row = db_fetch_one("SELECT * FROM khachhang WHERE id = ?", 'i', [$id]);
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('khachhang/edit', [
    'row' => $row,
    'notice' => $notice,
    'hasEmail' => $hasEmail,
    'hasGhiChu' => $hasGhiChu,
    'hasNgaySinh' => $hasNgaySinh,
    'hasDiaChi' => $hasDiaChi,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Sửa khách hàng',
    'page_css' => ['ui.css', 'forms.css', 'customers.css']
]);
?>
