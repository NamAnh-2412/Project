<?php
// Bảo vệ file - không cho truy cập trực tiếp
if (basename($_SERVER['PHP_SELF']) === 'config.php') {
    header('HTTP/1.0 403 Forbidden');
    die('Access Denied - This file cannot be accessed directly.');
}

// Cấu hình chung
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// URL encode tên thư mục để xử lý khoảng trống và ký tự đặc biệt
$folder_name = 'PhanMemBookingPhong';
define('BASE_URL', 'http://localhost/' . rawurlencode($folder_name) . '/');
define('UPLOAD_DIR', __DIR__ . '/../uploads/');

// Kiểm tra đăng nhập
function isLoggedIn() {
    return isset($_SESSION['user_id']) || isset($_SESSION['admin_id']);
}

// Lấy thông tin user hiện tại (nguoi_dung)
function getCurrentUser() {
    if (!isset($_SESSION['user_id'])) {
        return null;
    }
    
    if (isset($_SESSION['user']) && !empty($_SESSION['user'])) {
        return $_SESSION['user'];
    }
    
    if (function_exists('getDBConnection')) {
        $pdo = getDBConnection();
        $stmt = $pdo->prepare("SELECT * FROM nguoi_dung WHERE id = ?");
        $stmt->execute([$_SESSION['user_id']]);
        $user = $stmt->fetch();
        
        if ($user) {
            $_SESSION['user'] = $user;
        }
        return $user;
    }
    
    return null;
}

// Lấy thông tin admin hiện tại
function getCurrentAdmin() {
    if (!isset($_SESSION['admin_id'])) {
        return null;
    }
    
    if (isset($_SESSION['admin']) && !empty($_SESSION['admin'])) {
        return $_SESSION['admin'];
    }
    
    if (function_exists('getDBConnection')) {
        $pdo = getDBConnection();
        $stmt = $pdo->prepare("SELECT * FROM admin WHERE id = ?");
        $stmt->execute([$_SESSION['admin_id']]);
        $admin = $stmt->fetch();
        
        if ($admin) {
            $_SESSION['admin'] = $admin;
        }
        return $admin;
    }
    
    return null;
}

// Kiểm tra quyền chủ nhà
function isChuNha() {
    $user = getCurrentUser();
    return $user && $user['vai_tro'] === 'chu_nha';
}

// Kiểm tra quyền người thuê
function isNguoiThue() {
    $user = getCurrentUser();
    return $user && $user['vai_tro'] === 'nguoi_thue';
}

// Redirect nếu chưa đăng nhập
function requireLogin() {
    if (!isLoggedIn()) {
        header('Location: ' . BASE_URL . 'auth/login.php');
        exit;
    }
}

// Redirect nếu không phải chủ nhà
function requireChuNha() {
    requireLogin();
    if (!isChuNha()) {
        echo "<script>alert('Trang này chỉ dành cho Chủ nhà!'); window.location.href='" . BASE_URL . "index.php';</script>";
        exit;
    }
}

// Redirect nếu không phải người thuê
function requireNguoiThue() {
    requireLogin();
    if (!isNguoiThue()) {
        header('Location: ' . BASE_URL . 'index.php');
        exit;
    }
}

// Kiểm tra quyền Admin (từ bảng admin riêng)
function isAdmin() {
    return isset($_SESSION['admin_id']) && getCurrentAdmin() !== null;
}

function requireAdmin() {
    if (!isAdmin()) {
        echo "<script>alert('Bạn không có quyền truy cập trang Quản trị!'); window.location.href='" . BASE_URL . "index.php';</script>";
        exit;
    }
}

// Helper function: Lấy text trạng thái phòng
function getTrangThaiPhongText($trang_thai) {
    $trang_thai_text = [
        'con_trong' => 'Còn trống',
        'da_thue' => 'Đã thuê'
    ];
    return $trang_thai_text[$trang_thai] ?? $trang_thai;
}

// Helper function: Lấy text và màu trạng thái booking
function getTrangThaiBookingInfo($trang_thai) {
    $trang_thai_colors = [
        'cho_xac_nhan' => '#f39c12',
        'da_xac_nhan' => '#27ae60',
        'da_huy' => '#e74c3c'
    ];
    $trang_thai_text = [
        'cho_xac_nhan' => 'Chờ xác nhận',
        'da_xac_nhan' => 'Đã xác nhận',
        'da_huy' => 'Đã hủy'
    ];
    return [
        'text' => $trang_thai_text[$trang_thai] ?? $trang_thai,
        'color' => $trang_thai_colors[$trang_thai] ?? '#7f8c8d'
    ];
}

// Helper function: Lấy ảnh chính của phòng (SQL subquery)
function getAnhChinhSubquery() {
    return "(SELECT duong_dan_anh FROM anh_phong WHERE phong_id = p.id AND la_anh_chinh = 1 LIMIT 1) as anh_chinh";
}

// Helper function: Lấy ảnh đầu tiên của phòng (SQL subquery)
function getAnhDauTienSubquery() {
    return "(SELECT duong_dan_anh FROM anh_phong WHERE phong_id = p.id LIMIT 1) as anh_dau_tien";
}
?>

