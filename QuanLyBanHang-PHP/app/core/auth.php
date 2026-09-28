<?php
/**
 * Authentication Helper Functions
 * Xử lý authentication và authorization
 */

// Load constants nếu chưa được load
if (!defined('SESSION_USER_ID')) {
    require_once __DIR__ . '/constants.php';
}

/**
 * Kiểm tra user đã đăng nhập chưa
 * 
 * @return bool
 */
function isLoggedIn() {
    return isset($_SESSION[SESSION_USER_ID]) && !empty($_SESSION[SESSION_USER_ID]);
}

/**
 * Lấy thông tin user hiện tại từ session
 * 
 * @return array|null
 */
function getCurrentUser() {
    if (!isLoggedIn()) {
        return null;
    }
    
    return [
        'id' => $_SESSION[SESSION_USER_ID] ?? null,
        'username' => $_SESSION[SESSION_USERNAME] ?? null,
        'role' => $_SESSION[SESSION_ROLE] ?? null
    ];
}

/**
 * Lấy user ID hiện tại
 * 
 * @return int|null
 */
function getCurrentUserId() {
    return $_SESSION[SESSION_USER_ID] ?? null;
}

/**
 * Lấy username hiện tại
 * 
 * @return string|null
 */
function getCurrentUsername() {
    return $_SESSION[SESSION_USERNAME] ?? null;
}

/**
 * Lấy role hiện tại
 * 
 * @return string|null
 */
function getCurrentRole() {
    return $_SESSION[SESSION_ROLE] ?? null;
}

/**
 * Kiểm tra user có role cụ thể không
 * 
 * @param string $role Role cần kiểm tra
 * @return bool
 */
function hasRole($role) {
    return getCurrentRole() === $role;
}

/**
 * Kiểm tra user có phải admin không
 * 
 * @return bool
 */
function isAdmin() {
    return hasRole(ROLE_ADMIN);
}

/**
 * Kiểm tra user có phải cửa hàng trưởng không
 * 
 * @return bool
 */
function isManager() {
    return hasRole(ROLE_MANAGER);
}

/**
 * Yêu cầu đăng nhập - redirect nếu chưa đăng nhập
 * 
 * @param string $redirectTo URL để redirect về sau khi đăng nhập
 * @return void
 */
function requireLogin($redirectTo = null) {
    if (!isLoggedIn()) {
        $redirect = $redirectTo ?? getLoginUrl();
        header('Location: ' . $redirect);
        exit;
    }
}

/**
 * Yêu cầu role cụ thể - redirect nếu không có quyền
 * 
 * @param string|array $roles Role(s) cần có
 * @return void
 */
function requireRole($roles) {
    requireLogin();
    
    if (is_string($roles)) {
        $roles = [$roles];
    }
    
    $currentRole = getCurrentRole();
    if (!in_array($currentRole, $roles)) {
        header('HTTP/1.1 403 Forbidden');
        die('Bạn không có quyền truy cập trang này.');
    }
}

/**
 * Yêu cầu admin
 * 
 * @return void
 */
function requireAdmin() {
    requireRole(ROLE_ADMIN);
}

/**
 * Yêu cầu cửa hàng trưởng
 * 
 * @return void
 */
function requireManager() {
    requireRole(ROLE_MANAGER);
}

/**
 * Đăng nhập user
 * 
 * @param int $userId User ID
 * @param string $username Username
 * @param string $role Role
 * @return void
 */
function loginUser($userId, $username, $role = null) {
    $_SESSION[SESSION_USER_ID] = $userId;
    $_SESSION[SESSION_USERNAME] = $username;
    if ($role !== null) {
        $_SESSION[SESSION_ROLE] = $role;
    }
}

/**
 * Đăng xuất user
 * 
 * @return void
 */
function logoutUser() {
    $_SESSION = [];
    if (ini_get("session.use_cookies")) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000,
            $params["path"], $params["domain"],
            $params["secure"], $params["httponly"]
        );
    }
    session_destroy();
}

/**
 * Lấy URL đăng nhập
 * 
 * @return string
 */
function getLoginUrl() {
    return getSiteUrl() . '/app/auth/dangnhap.php';
}

/**
 * Lấy full site URL (http://host/path)
 * Khác với getBaseUrl() trong view.php (chỉ trả về relative path)
 * 
 * @return string
 */
function getSiteUrl() {
    static $baseUrl = null;
    
    if ($baseUrl === null) {
        $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
        $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
        $script = $_SERVER['SCRIPT_NAME'] ?? '';
        
        // Tính toán base path từ SCRIPT_NAME
        if (strpos($script, '/app/') !== false) {
            $basePath = substr($script, 0, strpos($script, '/app/'));
        } else {
            $basePath = dirname($script);
            if ($basePath === '.') {
                $basePath = '';
            }
        }
        
        $baseUrl = $protocol . '://' . $host . $basePath;
    }
    
    return $baseUrl;
}
?>
