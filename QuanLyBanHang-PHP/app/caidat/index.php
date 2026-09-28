<?php
require_once '../core/bootstrap.php';

// Require authentication and admin role
requireLogin();
requireAdmin();

$notice = '';
$noticeType = 'success'; // success, error

try {
    // Get all settings
    $settings = getAllSettings();
    
    // Default values
    $defaults = [
        'ten_cua_hang' => '',
        'dia_chi' => '',
        'so_dien_thoai' => '',
        'email' => '',
        'logo_path' => '',
        'items_per_page' => '20',
        'password_min_length' => '6',
        'session_timeout' => '60',
        'expiry_alert_days_7' => '7',
        'expiry_alert_days_1' => '1',
        'low_stock_threshold' => '10',
        'default_order_status' => ORDER_STATUS_PENDING,
        'default_payment_method' => PAYMENT_CASH,
        'allow_cancel_completed_order' => '0'
    ];
    
    // Merge with defaults
    foreach ($defaults as $key => $default) {
        if (!isset($settings[$key])) {
            $settings[$key] = $default;
        }
    }
    
    // Handle form submission
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $action = post('action', 'save');
        
        try {
            if ($action === 'save_shop_info') {
                // Save shop information
                $ten_cua_hang = post('ten_cua_hang');
                $dia_chi = post('dia_chi');
                $so_dien_thoai = post('so_dien_thoai');
                $email = post('email');
                
                // Validation
                if (empty($ten_cua_hang)) {
                    $notice = 'Vui lòng nhập tên cửa hàng.';
                    $noticeType = 'error';
                } elseif (!empty($email) && !isValidEmail($email)) {
                    $notice = 'Email không hợp lệ.';
                    $noticeType = 'error';
                } elseif (!empty($so_dien_thoai) && !isValidPhone($so_dien_thoai)) {
                    $notice = 'Số điện thoại không hợp lệ.';
                    $noticeType = 'error';
                } else {
                    setSetting('ten_cua_hang', $ten_cua_hang);
                    setSetting('dia_chi', $dia_chi);
                    setSetting('so_dien_thoai', $so_dien_thoai);
                    setSetting('email', $email);
                    
                    $notice = 'Đã lưu thông tin cửa hàng thành công!';
                    $noticeType = 'success';
                }
                
            } elseif ($action === 'save_system') {
                // Save system settings
                $items_per_page = post('items_per_page', 20, 'int');
                
                if ($items_per_page < 5 || $items_per_page > 200) {
                    $notice = 'Số mục mỗi trang phải từ 5 đến 200.';
                    $noticeType = 'error';
                } else {
                    setSetting('items_per_page', $items_per_page);
                    $notice = 'Đã lưu cài đặt hệ thống thành công!';
                    $noticeType = 'success';
                }
                
            } elseif ($action === 'save_security') {
                // Save security settings
                $password_min_length = post('password_min_length', 6, 'int');
                $session_timeout = post('session_timeout', 60, 'int');
                
                if ($password_min_length < 4 || $password_min_length > 20) {
                    $notice = 'Độ dài mật khẩu tối thiểu phải từ 4 đến 20 ký tự.';
                    $noticeType = 'error';
                } elseif ($session_timeout < 5 || $session_timeout > 1440) {
                    $notice = 'Thời gian timeout session phải từ 5 đến 1440 phút.';
                    $noticeType = 'error';
                } else {
                    setSetting('password_min_length', $password_min_length);
                    setSetting('session_timeout', $session_timeout);
                    $notice = 'Đã lưu cài đặt bảo mật thành công!';
                    $noticeType = 'success';
                }
                
            } elseif ($action === 'save_inventory') {
                // Save inventory settings
                $expiry_alert_days_7 = post('expiry_alert_days_7', 7, 'int');
                $expiry_alert_days_1 = post('expiry_alert_days_1', 1, 'int');
                $low_stock_threshold = post('low_stock_threshold', 10, 'float');
                
                if ($expiry_alert_days_7 < 1 || $expiry_alert_days_7 > 30) {
                    $notice = 'Cảnh báo 7 ngày phải từ 1 đến 30 ngày.';
                    $noticeType = 'error';
                } elseif ($expiry_alert_days_1 < 1 || $expiry_alert_days_1 > 30) {
                    $notice = 'Cảnh báo 1 ngày phải từ 1 đến 30 ngày.';
                    $noticeType = 'error';
                } elseif ($low_stock_threshold < 0) {
                    $notice = 'Ngưỡng tồn kho thấp không được âm.';
                    $noticeType = 'error';
                } else {
                    setSetting('expiry_alert_days_7', $expiry_alert_days_7);
                    setSetting('expiry_alert_days_1', $expiry_alert_days_1);
                    setSetting('low_stock_threshold', $low_stock_threshold);
                    $notice = 'Đã lưu cài đặt kho thành công!';
                    $noticeType = 'success';
                }
                
            } elseif ($action === 'save_orders') {
                // Save order settings
                $default_order_status = post('default_order_status');
                $default_payment_method = post('default_payment_method');
                $allow_cancel_completed_order = post('allow_cancel_completed_order', '0');
                
                // Validate order status
                $validStatuses = [
                    ORDER_STATUS_PENDING,
                    ORDER_STATUS_CONFIRMED,
                    ORDER_STATUS_PREPARING,
                    ORDER_STATUS_READY
                ];
                
                // Validate payment method
                $validPayments = [
                    PAYMENT_CASH,
                    PAYMENT_BANK,
                    PAYMENT_CARD,
                    PAYMENT_APP_ONLINE
                ];
                
                if (!in_array($default_order_status, $validStatuses)) {
                    $notice = 'Trạng thái đơn hàng mặc định không hợp lệ.';
                    $noticeType = 'error';
                } elseif (!in_array($default_payment_method, $validPayments)) {
                    $notice = 'Phương thức thanh toán mặc định không hợp lệ.';
                    $noticeType = 'error';
                } else {
                    setSetting('default_order_status', $default_order_status);
                    setSetting('default_payment_method', $default_payment_method);
                    setSetting('allow_cancel_completed_order', $allow_cancel_completed_order === '1' ? '1' : '0');
                    $notice = 'Đã lưu cài đặt đơn hàng thành công!';
                    $noticeType = 'success';
                }
            }
            
            // Reload settings after save
            if ($noticeType === 'success') {
                $settings = getAllSettings();
                foreach ($defaults as $key => $default) {
                    if (!isset($settings[$key])) {
                        $settings[$key] = $default;
                    }
                }
            }
            
        } catch (Exception $e) {
            error_log("Error saving settings: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra khi lưu cài đặt: ' . $e->getMessage();
            $noticeType = 'error';
        }
    }
    
    // Get current user info
    $userInfo = getCurrentUserInfo();
    
    renderView('caidat/index', [
        'settings' => $settings,
        'notice' => $notice,
        'noticeType' => $noticeType,
        'ten_nv' => $userInfo['ten_nv'],
        'first_char' => $userInfo['first_char'],
        'page_title' => 'Cài đặt hệ thống',
        'page_css' => ['settings.css'],
        'page_js' => ['settings.js']
    ]);
    
} catch (Exception $e) {
    error_log("Error in caidat/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang cài đặt.');
    redirect('../index.php');
}
?>
