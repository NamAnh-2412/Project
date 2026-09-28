<?php
require_once '../core/bootstrap.php';

// Redirect nếu đã đăng nhập
if (isLoggedIn()) {
    redirect('../../index.php');
}

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = post('username');
    $password = post('password', '', 'raw');
    $password2 = post('password2', '', 'raw');
    $email = post('email', '', 'email');
    $ten_nv = post('ten_nv');
    $sdt = post('sdt');
    
    // Validation
    if (empty($username) || empty($password) || empty($password2) || empty($email) || empty($ten_nv) || empty($sdt)) {
        $error = 'Vui lòng nhập đầy đủ thông tin.';
    } elseif (!isValidEmail($email)) {
        $error = 'Email không hợp lệ.';
    } elseif (strlen($password) < 6) { // Để cứng 6 ký tự cho đơn giản
        $error = 'Mật khẩu phải có ít nhất 6 ký tự.';
    } elseif ($password !== $password2) {
        $error = 'Nhập lại mật khẩu không khớp.';
    } elseif (!isValidPhone($sdt)) {
        $error = 'Số điện thoại không hợp lệ.';
    } else {
        try {
            // Check if username or email already exists
            $existing = db_fetch_one(
                "SELECT id FROM nhanvien WHERE ten_dang_nhap = ? OR email = ? LIMIT 1",
                'ss',
                [$username, $email]
            );
            
            if ($existing) {
                $error = 'Tên đăng nhập hoặc email đã tồn tại.';
            } else {
                // 3. Xử lý dữ liệu trước khi lưu
                
                // Mã hóa mật khẩu
                $hash = password_hash($password, PASSWORD_DEFAULT);
                
                // Tự động sinh Mã nhân viên (Tránh lỗi Duplicate key 'ma_nhanvien')
                // Ví dụ: NV + số ngẫu nhiên từ 10000 đến 99999 -> NV83921
                $ma_nv = 'NV' . rand(10000, 99999);

                // Thiết lập quyền và trạng thái mặc định
                $chuc_vu_mac_dinh = 'nhan_vien'; // Phải khớp với enum trong database
                $trang_thai_mac_dinh = 'dang_lam'; // Phải khớp với enum trong database
                
                // 4. Thực hiện INSERT vào database
                // Lưu ý: Câu lệnh này đã bao gồm đầy đủ các cột bắt buộc
                $sql = "INSERT INTO nhanvien (ma_nhanvien, ten_dang_nhap, mat_khau, email, ten_nhanvien, so_dien_thoai, chuc_vu, trang_thai) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                $result = db_query(
                    $sql,
                    'ssssssss', // 8 tham số dạng string
                    [
                        $ma_nv,
                        $username,
                        $hash,
                        $email,
                        $ten_nv,
                        $sdt,
                        $chuc_vu_mac_dinh,
                        $trang_thai_mac_dinh
                    ]
                );
                
                if ($result) {
                    $success = 'Đăng ký thành công! <a href="dangnhap.php" style="color: white; text-decoration: underline;">Đăng nhập ngay</a>';
                } else {
                    $error = 'Có lỗi hệ thống khi lưu dữ liệu.';
                }
            }
        } catch (Exception $e) {
            // Hiển thị lỗi chi tiết để dễ sửa (sau này web chạy thật thì nên ẩn đi)
            error_log("Error during registration: " . $e->getMessage());
            $error = 'Lỗi SQL: ' . $e->getMessage();
        }
    }
}

// Get shop name from settings
try {
    $shopName = db_fetch_one("SELECT value FROM settings WHERE `key` = 'ten_cua_hang'");
    $shopNameOld = db_fetch_one("SELECT value FROM settings WHERE `key` = 'shop_name'");
    $shop_name = ($shopName['value'] ?? $shopNameOld['value'] ?? 'CHILL GUYS');
} catch (Exception $e) {
    $shop_name = 'CHILL GUYS'; // Fallback nếu có lỗi
}

// Render View
renderView('auth/dangky', [
    'error' => $error,
    'success' => $success,
    'shop_name' => $shop_name
]);
?>
