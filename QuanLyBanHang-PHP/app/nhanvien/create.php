<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

$notice = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $ten = post('ten_nhanvien');
    $chuc = post('chuc_vu', ROLE_STAFF); // 'nhan_vien' hoặc 'cua_hang_truong'
    $sdt = post('so_dien_thoai');
    $email = post('email', '', 'email');
    $ngay = post('ngay_vao_lam');
    $ngay_sinh = post('ngay_sinh');
    $trang = post('trang_thai', EMPLOYEE_ACTIVE); // 'dang_lam' hoặc 'da_nghi'
    
    // Validation
    if (empty($ten)) {
        $notice = 'Vui lòng nhập Tên nhân viên.';
    } elseif (empty($ngay)) {
        $notice = 'Vui lòng chọn Ngày vào làm.';
    } elseif (!empty($sdt) && !isValidPhone($sdt)) {
        $notice = 'Số điện thoại không hợp lệ.';
    } elseif (!empty($email) && !isValidEmail($email)) {
        $notice = 'Email không hợp lệ.';
    } else {
        try {
            // Kiểm tra xem đã có nhân viên nào vào cùng ngày chưa
            $existing_same_date = db_fetch_one(
                "SELECT id FROM nhanvien WHERE ngay_vao_lam = ? LIMIT 1",
                's',
                [$ngay]
            );
            
            // Quyết định dùng ngày nào để sinh mã
            $date_for_code = null;
            if (!$existing_same_date) {
                // Chưa có nhân viên nào vào cùng ngày → dùng ngày vào làm
                $date_for_code = $ngay;
            } else {
                // Đã có nhân viên vào cùng ngày → dùng ngày sinh
                if (empty($ngay_sinh)) {
                    $notice = 'Đã có nhân viên vào cùng ngày này. Vui lòng nhập ngày sinh để sinh mã nhân viên.';
                } else {
                    $date_for_code = $ngay_sinh;
                }
            }
            
            if (!empty($date_for_code)) {
                // Tự động sinh mã nhân viên từ ngày (ngày vào làm hoặc ngày sinh)
                // Format: NV + ddmmyy (ví dụ: 24/12/2025 → NV241225)
                $dateObj = new DateTime($date_for_code);
                $dd = $dateObj->format('d');    // 24
                $mm = $dateObj->format('m');    // 12
                $yy = $dateObj->format('y');    // 25 (2 số cuối của năm)
                $ma_base = 'NV' . $dd . $mm . $yy;   // NV241225
                
                // Check duplicate mã
                $existing = db_fetch_one(
                    "SELECT id FROM nhanvien WHERE ma_nhanvien = ? LIMIT 1",
                    's',
                    [$ma_base]
                );
                
                if ($existing) {
                    // Trùng mã (trùng ngày/tháng/năm) → sinh mã ngẫu nhiên 6 số
                    // Format: NV + 6 số ngẫu nhiên (ví dụ: NV123456)
                    do {
                        $ma = 'NV' . str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
                        // Kiểm tra xem mã ngẫu nhiên có trùng không
                        $existing_random = db_fetch_one(
                            "SELECT id FROM nhanvien WHERE ma_nhanvien = ? LIMIT 1",
                            's',
                            [$ma]
                        );
                    } while ($existing_random); // Lặp lại nếu trùng
                } else {
                    // Không trùng → dùng mã từ ngày
                    $ma = $ma_base;
                }
                
                // Insert new employee với mã đã được xử lý
                // Lưu ý: ngay_sinh có thể NULL nếu không được nhập (nhân viên đầu tiên vào ngày đó)
                $result = db_query(
                    "INSERT INTO nhanvien (ma_nhanvien, ten_nhanvien, chuc_vu, so_dien_thoai, email, ngay_vao_lam, ngay_sinh, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    'ssssssss',
                    [$ma, $ten, $chuc, $sdt, $email ?: null, $ngay, $ngay_sinh ?: null, $trang]
                );
                
                if ($result) {
                    setFlashMessage('success', 'Thêm nhân viên thành công!');
                    redirect('index.php');
                } else {
                    $notice = 'Lỗi khi lưu nhân viên. Vui lòng thử lại.';
                }
            }
        } catch (Exception $e) {
            error_log("Error creating employee: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
        }
    }
}

// Get current user info
$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhanvien/create', [
    'notice' => $notice,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Thêm nhân viên',
    'page_css' => ['ui.css', 'employees.css']
]);
?>
