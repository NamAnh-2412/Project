<?php
require_once '../core/bootstrap.php';

// Redirect nếu đã đăng nhập
if (isLoggedIn()) {
    redirect('../../index.php');
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = post('username');
    $password = post('password', '', 'raw');
    
    // Validation
    if (empty($username) || empty($password)) {
        $error = 'Vui lòng nhập đầy đủ thông tin.';
    } else {
        try {
            // Get user from database
            $user = db_fetch_one(
                "SELECT id, ten_dang_nhap, mat_khau, chuc_vu FROM nhanvien WHERE ten_dang_nhap = ? LIMIT 1",
                's',
                [$username]
            );
            
            $login_status = 'that_bai';
            
            // Verify password
            if ($user && password_verify($password, $user['mat_khau'])) {
                // Login successful
                loginUser($user['id'], $user['ten_dang_nhap'], $user['chuc_vu'] ?? null);
                $login_status = 'thanh_cong';
                
                // Log login history
                try {
                    $ip = $_SERVER['REMOTE_ADDR'] ?? '';
                    db_query(
                        "INSERT INTO login_history (nhanvien_id, ten_dang_nhap, ip, trang_thai) VALUES (?, ?, ?, ?)",
                        'isss',
                        [$user['id'], $username, $ip, $login_status]
                    );
                } catch (Exception $e) {
                    error_log("Error logging login history: " . $e->getMessage());
                    // Don't fail login if logging fails
                }
                
                // Redirect to home page
                redirect('../../index.php');
            } else {
                $error = 'Sai tên đăng nhập hoặc mật khẩu.';
                
                // Log failed login attempt
                try {
                    $ip = $_SERVER['REMOTE_ADDR'] ?? '';
                    $uid = $user['id'] ?? null;
                    db_query(
                        "INSERT INTO login_history (nhanvien_id, ten_dang_nhap, ip, trang_thai) VALUES (?, ?, ?, ?)",
                        'isss',
                        [$uid, $username, $ip, $login_status]
                    );
                } catch (Exception $e) {
                    error_log("Error logging failed login: " . $e->getMessage());
                }
            }
        } catch (Exception $e) {
            error_log("Error during login: " . $e->getMessage());
            $error = 'Có lỗi xảy ra. Vui lòng thử lại sau.';
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

renderView('auth/dangnhap', [
    'error' => $error,
    'page_title' => 'Đăng Nhập',
    'shop_name' => $shop_name
]);
?>
