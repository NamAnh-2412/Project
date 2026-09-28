<?php
/**
 * Constants - Các hằng số hệ thống
 */

// Database constants
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'quanlybanhang');
define('DB_CHARSET', 'utf8');

// Session keys
define('SESSION_USER_ID', 'nhanvien_id');
define('SESSION_USERNAME', 'ten_dang_nhap');
define('SESSION_ROLE', 'chuc_vu');

// Order status (phải khớp với enum trong SQL)
define('ORDER_STATUS_PENDING', 'cho_xac_nhan');      // Chờ xác nhận
define('ORDER_STATUS_CONFIRMED', 'da_xac_nhan');     // Đã xác nhận
define('ORDER_STATUS_PREPARING', 'dang_chuan_bi');   // Đang chuẩn bị
define('ORDER_STATUS_READY', 'san_sang');            // Sẵn sàng
define('ORDER_STATUS_COMPLETED', 'hoan_thanh');      // Hoàn thành
define('ORDER_STATUS_CANCELLED', 'da_huy');          // Đã hủy

// Payment methods (phải khớp với enum trong SQL)
define('PAYMENT_CASH', 'tien_mat');
define('PAYMENT_BANK', 'chuyen_khoan');
define('PAYMENT_CARD', 'the');
define('PAYMENT_APP_ONLINE', 'app_online'); // App online (cần chạy migration trước)

// Employee roles (phải khớp với enum trong SQL: 'nhan_vien','cua_hang_truong')
define('ROLE_STAFF', 'nhan_vien');                    // Nhân viên
define('ROLE_MANAGER', 'cua_hang_truong');           // Cửa hàng trưởng
// Lưu ý: Không có role 'quan_ly' trong database, dùng 'cua_hang_truong' làm admin role
define('ROLE_ADMIN', 'cua_hang_truong');             // Alias cho admin (sử dụng cua_hang_truong)

// Employee status (phải khớp với enum trong SQL: 'dang_lam','da_nghi')
define('EMPLOYEE_ACTIVE', 'dang_lam');               // Đang làm
define('EMPLOYEE_INACTIVE', 'da_nghi');              // Đã nghỉ

// Pagination
define('ITEMS_PER_PAGE', 20);

// Default values
define('DEFAULT_PASSWORD_MIN_LENGTH', 6);

