<?php
/**
 * Helper Functions
 * Các hàm tiện ích dùng chung
 */

/**
 * Sanitize input để tránh XSS
 * 
 * @param mixed $data Data cần sanitize
 * @param string $type Type của data (string, int, float, email, etc.)
 * @return mixed
 */
function sanitize($data, $type = 'string') {
    if ($data === null) {
        return null;
    }
    
    switch ($type) {
        case 'string':
            return htmlspecialchars(trim($data), ENT_QUOTES, 'UTF-8');
        case 'int':
            return (int)$data;
        case 'float':
            return (float)$data;
        case 'email':
            return filter_var(trim($data), FILTER_SANITIZE_EMAIL);
        case 'url':
            return filter_var(trim($data), FILTER_SANITIZE_URL);
        case 'array':
            // Xử lý array: sanitize từng phần tử
            if (!is_array($data)) {
                return [];
            }
            $result = [];
            foreach ($data as $key => $value) {
                if (is_array($value)) {
                    $result[$key] = sanitize($value, 'array');
                } else {
                    $result[$key] = htmlspecialchars(trim($value), ENT_QUOTES, 'UTF-8');
                }
            }
            return $result;
        case 'raw':
            return $data;
        default:
            // Kiểm tra nếu là array nhưng type không phải 'array'
            if (is_array($data)) {
                return sanitize($data, 'array');
            }
            return htmlspecialchars(trim($data), ENT_QUOTES, 'UTF-8');
    }
}

/**
 * Lấy POST parameter đã được sanitize
 * 
 * @param string $key Key của parameter
 * @param mixed $default Default value nếu không tồn tại
 * @param string $type Type để sanitize
 * @return mixed
 */
function post($key, $default = '', $type = 'string') {
    if (!isset($_POST[$key])) {
        // Nếu type là 'array' và không có giá trị, trả về mảng rỗng
        if ($type === 'array') {
            return [];
        }
        return $default;
    }
    return sanitize($_POST[$key], $type);
}

/**
 * Lấy GET parameter đã được sanitize
 * 
 * @param string $key Key của parameter
 * @param mixed $default Default value nếu không tồn tại
 * @param string $type Type để sanitize
 * @return mixed
 */
function get($key, $default = '', $type = 'string') {
    return isset($_GET[$key]) ? sanitize($_GET[$key], $type) : $default;
}

/**
 * Validate email
 * 
 * @param string $email Email cần validate
 * @return bool
 */
function isValidEmail($email) {
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

/**
 * Validate phone number (Vietnamese format)
 * 
 * @param string $phone Phone number
 * @return bool
 */
function isValidPhone($phone) {
    $phone = preg_replace('/[^0-9]/', '', $phone);
    return preg_match('/^(0|\+84)[0-9]{9,10}$/', $phone);
}

/**
 * Format money
 * 
 * @param float|int $amount Số tiền
 * @param string $currency Đơn vị tiền tệ
 * @return string
 */
function formatMoney($amount, $currency = 'đ') {
    return number_format($amount, 0, ',', '.') . ' ' . $currency;
}

/**
 * Format date
 * 
 * @param string $date Date string
 * @param string $format Format string
 * @return string
 */
function formatDate($date, $format = 'd/m/Y') {
    if (empty($date)) {
        return '';
    }
    
    try {
        $dateObj = new DateTime($date);
        return $dateObj->format($format);
    } catch (Exception $e) {
        return $date;
    }
}

/**
 * Redirect đến URL
 * 
 * @param string $url URL để redirect
 * @param int $code HTTP status code
 * @return void
 */
function redirect($url, $code = 302) {
    header("Location: {$url}", true, $code);
    exit;
}

/**
 * Return JSON response
 * 
 * @param mixed $data Data để return
 * @param int $status HTTP status code
 * @return void
 */
function jsonResponse($data, $status = 200) {
    // Clean any output buffers
    while (ob_get_level()) {
        ob_end_clean();
    }
    
    // Clear any previous output
    if (ob_get_contents()) {
        ob_clean();
    }
    
    // Set headers
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-cache, must-revalidate');
    
    // Output JSON
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

/**
 * Initialize API environment
 * Handles bootstrap loading, output buffering, and error handling for API endpoints
 * 
 * @param bool $enableErrorDisplay Whether to display errors (default: false for production)
 * @return void
 */
function initApi($enableErrorDisplay = false) {
    // Start output buffering early
    if (ob_get_level() === 0) {
        ob_start();
    }
    
    // Configure error reporting
    ini_set('display_errors', $enableErrorDisplay ? 1 : 0);
    ini_set('log_errors', 1);
    error_reporting(E_ALL);
    
    // Load bootstrap
    $bootstrapPath = __DIR__ . '/bootstrap.php';
    if (!file_exists($bootstrapPath)) {
        // Try alternative path if called from different location
        $bootstrapPath = dirname(__DIR__) . '/core/bootstrap.php';
    }
    
    if (!file_exists($bootstrapPath)) {
        ob_clean();
        error_log("FATAL: Cannot find bootstrap.php from " . __DIR__);
        http_response_code(500);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'error' => 'Lỗi khởi tạo hệ thống'], JSON_UNESCAPED_UNICODE);
        exit;
    }
    
    try {
        require_once $bootstrapPath;
    } catch (Exception $e) {
        ob_clean();
        error_log("FATAL: Failed to load bootstrap: " . $e->getMessage());
        error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
        http_response_code(500);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'error' => 'Lỗi khởi tạo hệ thống'], JSON_UNESCAPED_UNICODE);
        exit;
    } catch (Error $e) {
        ob_clean();
        error_log("FATAL ERROR: Failed to load bootstrap: " . $e->getMessage());
        error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
        http_response_code(500);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'error' => 'Lỗi nghiêm trọng khởi tạo hệ thống'], JSON_UNESCAPED_UNICODE);
        exit;
    }
    
    // Clear any output that might have been generated
    ob_clean();
}

/**
 * Require authentication for API endpoints (returns JSON error instead of redirect)
 * 
 * @return void
 */
function requireApiAuth() {
    if (!function_exists('isLoggedIn')) {
        ob_clean();
        error_log("ERROR: isLoggedIn function not found");
        jsonResponse(['success' => false, 'error' => 'Lỗi hệ thống: hàm xác thực không tồn tại'], 500);
    }
    
    if (!isLoggedIn()) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Chưa đăng nhập'], 401);
    }
}

/**
 * Require admin role for API endpoints (returns JSON error instead of redirect)
 * 
 * @return void
 */
function requireApiAdmin() {
    requireApiAuth();
    
    if (!function_exists('isAdmin')) {
        ob_clean();
        error_log("ERROR: isAdmin function not found");
        jsonResponse(['success' => false, 'error' => 'Lỗi hệ thống: hàm kiểm tra quyền không tồn tại'], 500);
    }
    
    if (!isAdmin()) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Không có quyền truy cập'], 403);
    }
}

/**
 * Require specific HTTP method for API endpoints
 * 
 * @param string|array $methods Allowed HTTP method(s)
 * @return void
 */
function requireApiMethod($methods) {
    if (is_string($methods)) {
        $methods = [$methods];
    }
    
    $currentMethod = $_SERVER['REQUEST_METHOD'] ?? 'GET';
    if (!in_array($currentMethod, $methods)) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Phương thức không được phép'], 405);
    }
}

/**
 * Generate CSRF token
 * 
 * @return string
 */
function generateCsrfToken() {
    if (!isset($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf_token'];
}

/**
 * Verify CSRF token
 * 
 * @param string $token Token cần verify
 * @return bool
 */
function verifyCsrfToken($token) {
    return isset($_SESSION['csrf_token']) && hash_equals($_SESSION['csrf_token'], $token);
}

/**
 * Flash message - lưu message vào session để hiển thị ở request tiếp theo
 * 
 * @param string $type Type của message (success, error, warning, info)
 * @param string $message Message
 * @return void
 */
function setFlashMessage($type, $message) {
    if (!isset($_SESSION['flash_messages'])) {
        $_SESSION['flash_messages'] = [];
    }
    $_SESSION['flash_messages'][] = [
        'type' => $type,
        'message' => $message
    ];
}

/**
 * Lấy và xóa flash messages
 * 
 * @return array
 */
function getFlashMessages() {
    $messages = $_SESSION['flash_messages'] ?? [];
    unset($_SESSION['flash_messages']);
    return $messages;
}

/**
 * Get current user info for views
 * Returns array with ten_nv and first_char
 * 
 * @return array
 */
function getCurrentUserInfo() {
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    return [
        'ten_nv' => $ten_nv,
        'first_char' => $first_char
    ];
}

/**
 * Resize image while maintaining aspect ratio
 * 
 * @param string $imagePath Full path to image file
 * @param int $maxWidth Maximum width
 * @param int $maxHeight Maximum height
 * @param int $quality Quality for JPEG/WEBP (0-100)
 * @return bool Success status
 */
function resizeImage($imagePath, $maxWidth = 1200, $maxHeight = 1200, $quality = 85) {
    if (!file_exists($imagePath)) {
        return false;
    }
    
    // Check if GD extension is available
    if (!function_exists('imagecreatetruecolor')) {
        error_log("GD extension not available for image resize");
        return false;
    }
    
    // Get image info
    $imageInfo = @getimagesize($imagePath);
    if ($imageInfo === false) {
        return false;
    }
    
    $originalWidth = $imageInfo[0];
    $originalHeight = $imageInfo[1];
    $mimeType = $imageInfo['mime'];
    
    // If image is smaller than max dimensions, no need to resize
    if ($originalWidth <= $maxWidth && $originalHeight <= $maxHeight) {
        return true;
    }
    
    // Calculate new dimensions maintaining aspect ratio
    $ratio = min($maxWidth / $originalWidth, $maxHeight / $originalHeight);
    $newWidth = (int)($originalWidth * $ratio);
    $newHeight = (int)($originalHeight * $ratio);
    
    // Create image resource based on type
    $source = null;
    switch ($mimeType) {
        case 'image/jpeg':
            $source = @imagecreatefromjpeg($imagePath);
            break;
        case 'image/png':
            $source = @imagecreatefrompng($imagePath);
            break;
        case 'image/gif':
            $source = @imagecreatefromgif($imagePath);
            break;
        case 'image/webp':
            $source = @imagecreatefromwebp($imagePath);
            break;
        default:
            return false;
    }
    
    if (!$source) {
        return false;
    }
    
    // Create new image
    $resized = imagecreatetruecolor($newWidth, $newHeight);
    
    // Preserve transparency for PNG/GIF
    if ($mimeType === 'image/png' || $mimeType === 'image/gif') {
        imagealphablending($resized, false);
        imagesavealpha($resized, true);
        $transparent = imagecolorallocatealpha($resized, 255, 255, 255, 127);
        imagefill($resized, 0, 0, $transparent);
    }
    
    // Resize image
    imagecopyresampled(
        $resized, $source,
        0, 0, 0, 0,
        $newWidth, $newHeight, $originalWidth, $originalHeight
    );
    
    // Save resized image
    $result = false;
    switch ($mimeType) {
        case 'image/jpeg':
            $result = @imagejpeg($resized, $imagePath, $quality);
            break;
        case 'image/png':
            $result = @imagepng($resized, $imagePath, 9);
            break;
        case 'image/gif':
            $result = @imagegif($resized, $imagePath);
            break;
        case 'image/webp':
            $result = @imagewebp($resized, $imagePath, $quality);
            break;
    }
    
    // Clean up
    imagedestroy($source);
    imagedestroy($resized);
    
    return $result;
}

/**
 * Crop image to square
 * 
 * @param string $imagePath Full path to image file
 * @param int $size Target size (width = height)
 * @return bool Success status
 */
function cropImageToSquare($imagePath, $size = 800) {
    if (!file_exists($imagePath)) {
        return false;
    }
    
    // Check if GD extension is available
    if (!function_exists('imagecreatetruecolor')) {
        error_log("GD extension not available for image crop");
        return false;
    }
    
    // Get image info
    $imageInfo = @getimagesize($imagePath);
    if ($imageInfo === false) {
        return false;
    }
    
    $width = $imageInfo[0];
    $height = $imageInfo[1];
    $mimeType = $imageInfo['mime'];
    
    // Create image resource based on type
    $source = null;
    switch ($mimeType) {
        case 'image/jpeg':
            $source = @imagecreatefromjpeg($imagePath);
            break;
        case 'image/png':
            $source = @imagecreatefrompng($imagePath);
            break;
        case 'image/gif':
            $source = @imagecreatefromgif($imagePath);
            break;
        case 'image/webp':
            $source = @imagecreatefromwebp($imagePath);
            break;
        default:
            return false;
    }
    
    if (!$source) {
        return false;
    }
    
    // Calculate crop dimensions (center crop)
    $minDimension = min($width, $height);
    $cropX = ($width - $minDimension) / 2;
    $cropY = ($height - $minDimension) / 2;
    
    // Create square image
    $square = imagecreatetruecolor($size, $size);
    
    // Preserve transparency for PNG/GIF
    if ($mimeType === 'image/png' || $mimeType === 'image/gif') {
        imagealphablending($square, false);
        imagesavealpha($square, true);
        $transparent = imagecolorallocatealpha($square, 255, 255, 255, 127);
        imagefill($square, 0, 0, $transparent);
    }
    
    // Resize and crop
    imagecopyresampled(
        $square, $source,
        0, 0, $cropX, $cropY,
        $size, $size, $minDimension, $minDimension
    );
    
    // Save image
    $result = false;
    switch ($mimeType) {
        case 'image/jpeg':
            $result = @imagejpeg($square, $imagePath, 90);
            break;
        case 'image/png':
            $result = @imagepng($square, $imagePath, 9);
            break;
        case 'image/gif':
            $result = @imagegif($square, $imagePath);
            break;
        case 'image/webp':
            $result = @imagewebp($square, $imagePath, 90);
            break;
    }
    
    // Clean up
    imagedestroy($source);
    imagedestroy($square);
    
    return $result;
}

/**
 * Upload image file with auto-resize and optional crop
 * 
 * @param string $fieldName Name of file input field
 * @param string $uploadDir Directory to upload (relative to root)
 * @param array $allowedTypes Allowed MIME types
 * @param int $maxSize Max file size in bytes (default 5MB)
 * @param bool $autoCrop Auto crop to square (default true)
 * @param int $cropSize Size for square crop (default 800px)
 * @param int $maxWidth Max width before resize (default 2000px)
 * @param int $maxHeight Max height before resize (default 2000px)
 * @param int $quality JPEG/WEBP quality (0-100, default 85)
 * @return array ['success' => bool, 'path' => string|null, 'error' => string|null]
 */
function uploadImage($fieldName, $uploadDir = 'uploads/images/products', $allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'], $maxSize = 5242880, $autoCrop = true, $cropSize = 800, $maxWidth = 2000, $maxHeight = 2000, $quality = 85) {
    if (!isset($_FILES[$fieldName]) || $_FILES[$fieldName]['error'] !== UPLOAD_ERR_OK) {
        return ['success' => false, 'path' => null, 'error' => 'Không có file được upload hoặc có lỗi xảy ra'];
    }
    
    $file = $_FILES[$fieldName];
    
    // Check file size (cho phép lớn hơn một chút vì sẽ resize)
    $actualMaxSize = $maxSize * 2; // Cho phép file gốc lớn gấp đôi vì sẽ resize
    if ($file['size'] > $actualMaxSize) {
        return ['success' => false, 'path' => null, 'error' => 'File quá lớn. Kích thước tối đa: ' . ($maxSize / 1024 / 1024) . 'MB (sau khi resize)'];
    }
    
    // Check MIME type
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);
    
    // Also check by extension for better compatibility
    $extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $allowedExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
    
    if (!in_array($mimeType, $allowedTypes) && !in_array($extension, $allowedExtensions)) {
        return ['success' => false, 'path' => null, 'error' => 'Định dạng file không được phép. Chỉ chấp nhận: JPG, JPEG, PNG, GIF, WEBP'];
    }
    
    // Normalize MIME type (jpeg = jpg)
    if ($mimeType === 'image/jpg' || ($extension === 'jpg' && $mimeType === 'application/octet-stream')) {
        $mimeType = 'image/jpeg';
    }
    
    // Create upload directory if not exists
    $rootPath = defined('ROOT_PATH') ? ROOT_PATH : dirname(dirname(__DIR__));
    
    // Normalize root path
    $rootPath = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $rootPath);
    $uploadDir = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $uploadDir);
    
    // Build full path properly
    $fullUploadDir = rtrim($rootPath, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR . ltrim($uploadDir, DIRECTORY_SEPARATOR);
    
    // Normalize final path
    $fullUploadDir = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $fullUploadDir);
    
    error_log("Upload directory: " . $fullUploadDir);
    
    if (!is_dir($fullUploadDir)) {
        error_log("Creating directory: " . $fullUploadDir);
        if (!@mkdir($fullUploadDir, 0755, true)) {
            $error = error_get_last();
            $errorMsg = 'Không thể tạo thư mục upload: ' . $fullUploadDir;
            if ($error) {
                $errorMsg .= ' - ' . $error['message'];
            }
            error_log($errorMsg);
            return ['success' => false, 'path' => null, 'error' => $errorMsg];
        }
        error_log("Directory created successfully");
    }
    
    // Check if directory is writable
    if (!is_writable($fullUploadDir)) {
        $errorMsg = 'Thư mục upload không có quyền ghi: ' . $fullUploadDir;
        error_log($errorMsg);
        return ['success' => false, 'path' => null, 'error' => $errorMsg];
    }
    
    // Generate unique filename
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename = uniqid('img_', true) . '.' . $extension;
    $targetPath = rtrim($fullUploadDir, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR . $filename;
    
    // Normalize target path
    $targetPath = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $targetPath);
    
    error_log("Target path: " . $targetPath);
    error_log("Temp file: " . $file['tmp_name']);
    error_log("Temp file exists: " . (file_exists($file['tmp_name']) ? 'yes' : 'no'));
    
    // Move uploaded file
    if (!@move_uploaded_file($file['tmp_name'], $targetPath)) {
        $error = error_get_last();
        $errorMsg = 'Không thể lưu file vào: ' . $targetPath;
        if ($error) {
            $errorMsg .= ' - ' . $error['message'];
        }
        error_log($errorMsg);
        error_log("PHP upload error code: " . $file['error']);
        return ['success' => false, 'path' => null, 'error' => $errorMsg];
    }
    
    // Verify file was actually saved
    if (!file_exists($targetPath)) {
        $errorMsg = 'File không tồn tại sau khi upload: ' . $targetPath;
        error_log($errorMsg);
        return ['success' => false, 'path' => null, 'error' => $errorMsg];
    }
    
    error_log("File uploaded successfully: " . $targetPath);
    
    // Auto resize if image is too large (before crop)
    if (function_exists('imagecreatetruecolor')) {
        try {
            // Resize first to reduce file size
            if (!resizeImage($targetPath, $maxWidth, $maxHeight, $quality)) {
                error_log("Warning: Failed to resize image: " . $targetPath);
                // Continue anyway, maybe image is already small enough
            }
        } catch (Exception $e) {
            error_log("Exception during resize: " . $e->getMessage());
            // Continue anyway
        } catch (Error $e) {
            error_log("Fatal error during resize: " . $e->getMessage());
            // Continue anyway
        }
        
        // Auto crop to square if enabled
        if ($autoCrop) {
            try {
                if (!cropImageToSquare($targetPath, $cropSize)) {
                    // If crop fails, still return success but log warning
                    error_log("Warning: Failed to crop image to square: " . $targetPath);
                }
            } catch (Exception $e) {
                error_log("Exception during crop: " . $e->getMessage());
                // Continue anyway
            } catch (Error $e) {
                error_log("Fatal error during crop: " . $e->getMessage());
                // Continue anyway
            }
        }
    } else {
        error_log("Warning: GD extension not available. Image will not be resized/cropped.");
    }
    
    // Check final file size after processing
    $finalSize = filesize($targetPath);
    if ($finalSize > $maxSize) {
        // Try to compress more aggressively
        if (function_exists('imagecreatetruecolor') && ($mimeType === 'image/jpeg' || $mimeType === 'image/webp')) {
            $lowerQuality = max(60, $quality - 20); // Reduce quality by 20, minimum 60
            resizeImage($targetPath, $maxWidth, $maxHeight, $lowerQuality);
            $finalSize = filesize($targetPath);
        }
        
        if ($finalSize > $maxSize) {
            error_log("Warning: Final image size (" . ($finalSize / 1024 / 1024) . "MB) still exceeds max size (" . ($maxSize / 1024 / 1024) . "MB)");
            // Still return success, but log warning
        }
    }
    
    // Return relative path from root
    return ['success' => true, 'path' => $uploadDir . '/' . $filename, 'error' => null];
}

/**
 * Delete image file
 * 
 * @param string $imagePath Relative path from root
 * @return bool
 */
function deleteImage($imagePath) {
    if (empty($imagePath)) {
        return true;
    }
    
    $rootPath = defined('ROOT_PATH') ? ROOT_PATH : dirname(dirname(__DIR__));
    $fullPath = $rootPath . '/' . $imagePath;
    
    // Normalize path separators
    $fullPath = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, $fullPath);
    
    if (file_exists($fullPath) && is_file($fullPath)) {
        $result = @unlink($fullPath);
        if (!$result) {
            $error = error_get_last();
            error_log("Failed to delete image: " . $fullPath . " - " . ($error ? $error['message'] : 'Unknown error'));
        }
        return $result;
    }
    
    return true;
}

/**
 * Format payment method name to Vietnamese
 * 
 * @param string $method Payment method code
 * @return string
 */
function formatPaymentMethod($method) {
    $methods = [
        PAYMENT_CASH => 'Tiền mặt',
        PAYMENT_BANK => 'Chuyển khoản',
        PAYMENT_CARD => 'Thẻ',
        PAYMENT_APP_ONLINE => 'App online'
    ];
    
    return $methods[$method] ?? $method;
}

/**
 * Hiển thị phương thức thanh toán theo trạng thái đơn
 */
function formatOrderPayment($method, $status) {
    if ($status === ORDER_STATUS_PENDING) {
        return 'Chưa thanh toán';
    }
    if ($status === ORDER_STATUS_CANCELLED) {
        return '—';
    }
    return formatPaymentMethod($method);
}

/**
 * Nhãn tiếng Việt cho trạng thái đơn hàng
 */
function formatOrderStatus($status) {
    $labels = [
        ORDER_STATUS_PENDING => 'Chờ xử lý',
        ORDER_STATUS_CONFIRMED => 'Đã xác nhận',
        ORDER_STATUS_PREPARING => 'Đang chuẩn bị',
        ORDER_STATUS_READY => 'Sẵn sàng',
        ORDER_STATUS_COMPLETED => 'Hoàn thành',
        ORDER_STATUS_CANCELLED => 'Đã hủy',
    ];
    return $labels[$status] ?? $status;
}

/**
 * CSS class cho badge trạng thái đơn hàng
 */
function getOrderStatusClass($status) {
    $map = [
        ORDER_STATUS_PENDING => 'order-status-pending',
        ORDER_STATUS_CONFIRMED => 'order-status-confirmed',
        ORDER_STATUS_PREPARING => 'order-status-preparing',
        ORDER_STATUS_READY => 'order-status-ready',
        ORDER_STATUS_COMPLETED => 'order-status-completed',
        ORDER_STATUS_CANCELLED => 'order-status-cancelled',
    ];
    return $map[$status] ?? 'order-status-default';
}

/**
 * Danh sách trạng thái dùng cho bộ lọc
 */
function getOrderStatusFilterOptions() {
    return [
        '' => 'Tất cả trạng thái',
        ORDER_STATUS_PENDING => 'Chờ xử lý',
        ORDER_STATUS_CONFIRMED => 'Đã xác nhận',
        ORDER_STATUS_PREPARING => 'Đang chuẩn bị',
        ORDER_STATUS_READY => 'Sẵn sàng',
        ORDER_STATUS_COMPLETED => 'Hoàn thành',
        ORDER_STATUS_CANCELLED => 'Đã hủy',
    ];
}

/**
 * Số thứ tự hóa đơn trong ngày (theo thời gian tạo)
 */
function getOrderDailySequence($orderId) {
    $row = db_fetch_one(
        "SELECT COUNT(*) AS stt
         FROM donhang
         WHERE DATE(ngay_tao) = (SELECT DATE(ngay_tao) FROM donhang WHERE id = ?)
           AND ngay_tao <= (SELECT ngay_tao FROM donhang WHERE id = ?)",
        'ii',
        [$orderId, $orderId]
    );
    return (int)($row['stt'] ?? 0);
}

/**
 * Tên khách hiển thị trên đơn hàng
 */
function getOrderCustomerDisplayName($order) {
    $name = trim($order['ten_khachhang'] ?? '');
    if ($name === '' || $name === 'Khách lẻ') {
        return 'Khách lẻ';
    }
    return $name;
}

/**
 * Get all payment methods as array for dropdown
 * 
 * @return array
 */
function getPaymentMethods() {
    return [
        '' => 'Tất cả phương thức',
        PAYMENT_CASH => 'Tiền mặt',
        PAYMENT_BANK => 'Chuyển khoản',
        PAYMENT_CARD => 'Thẻ',
        PAYMENT_APP_ONLINE => 'App online'
    ];
}

/**
 * Danh sách phương thức thanh toán cho form chọn (không có option lọc)
 */
function getPaymentMethodOptions() {
    return [
        PAYMENT_CASH => 'Tiền mặt',
        PAYMENT_BANK => 'Chuyển khoản',
        PAYMENT_CARD => 'Thẻ',
        PAYMENT_APP_ONLINE => 'App online',
    ];
}

/**
 * Đơn hàng có thể thanh toán để hoàn thành không
 */
function canCompleteOrder($status) {
    return in_array($status, [
        ORDER_STATUS_PENDING,
        ORDER_STATUS_CONFIRMED,
        ORDER_STATUS_PREPARING,
        ORDER_STATUS_READY,
    ], true);
}

/**
 * Get setting value by key
 * 
 * @param string $key Setting key
 * @param mixed $default Default value if not found
 * @return mixed
 */
function getSetting($key, $default = '') {
    try {
        global $conn;
        if ($conn === null) {
            error_log("getSetting: Database connection is null");
            return $default;
        }
        
        $result = db_fetch_one("SELECT `value` FROM settings WHERE `key` = ?", 's', [$key]);
        return $result ? $result['value'] : $default;
    } catch (Exception $e) {
        error_log("getSetting error: " . $e->getMessage());
        return $default;
    } catch (Error $e) {
        error_log("getSetting fatal error: " . $e->getMessage());
        return $default;
    }
}

/**
 * Set setting value
 * 
 * @param string $key Setting key
 * @param mixed $value Setting value
 * @return bool
 */
function setSetting($key, $value) {
    try {
        global $conn;
        if ($conn === null) {
            error_log("setSetting: Database connection is null");
            return false;
        }
        
        $valueStr = is_array($value) ? json_encode($value, JSON_UNESCAPED_UNICODE) : (string)$value;
        $result = db_query(
            "INSERT INTO settings (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = ?",
            'sss',
            [$key, $valueStr, $valueStr]
        );
        
        if ($result === false) {
            error_log("setSetting: db_query returned false for key: $key");
            return false;
        }
        
        return true;
    } catch (Exception $e) {
        error_log("setSetting error: " . $e->getMessage());
        return false;
    } catch (Error $e) {
        error_log("setSetting fatal error: " . $e->getMessage());
        return false;
    }
}

/**
 * Get all settings as associative array
 * 
 * @return array
 */
function getAllSettings() {
    $rows = db_fetch_all("SELECT `key`, `value` FROM settings");
    $settings = [];
    foreach ($rows as $row) {
        $settings[$row['key']] = $row['value'];
    }
    return $settings;
}

/**
 * Delete setting by key
 * 
 * @param string $key Setting key
 * @return bool
 */
function deleteSetting($key) {
    return db_query("DELETE FROM settings WHERE `key` = ?", 's', [$key]);
}

/**
 * Nhãn hiển thị hạng thành viên khách hàng
 */
function getCustomerRankLabel($rank) {
    $labels = [
        'thuong' => 'Thường',
        'bac' => 'Bạc',
        'vang' => 'Vàng',
        'vip' => 'VIP',
    ];
    return $labels[$rank] ?? 'Thường';
}

/**
 * CSS class cho badge hạng thành viên
 */
function getCustomerRankClass($rank) {
    $valid = ['thuong', 'bac', 'vang', 'vip'];
    return in_array($rank, $valid, true) ? 'rank-' . $rank : 'rank-thuong';
}

/**
 * Xác định hạng thành viên từ một khoản chi tiêu
 */
function getCustomerRankFromAmount($amount) {
    $amount = (float)$amount;
    if ($amount >= 5000000) {
        return 'vip';
    }
    if ($amount >= 2000000) {
        return 'vang';
    }
    if ($amount >= 500000) {
        return 'bac';
    }
    return 'thuong';
}

/**
 * Tính lại hạng thành viên theo tong_tien_da_chi
 */
function recalculateCustomerRank($customerId) {
    return db_query(
        "UPDATE khachhang SET loai_thanh_vien = CASE 
            WHEN tong_tien_da_chi >= 5000000 THEN 'vip'
            WHEN tong_tien_da_chi >= 2000000 THEN 'vang'
            WHEN tong_tien_da_chi >= 500000  THEN 'bac'
            ELSE 'thuong'
        END WHERE id = ?",
        'i',
        [$customerId]
    );
}

/**
 * Trừ chi tiêu khách khi hủy đơn đã hoàn thành
 */
function reverseCustomerSpendingOnCancel($customerId, $amount) {
    db_query(
        "UPDATE khachhang SET 
            tong_donhang = GREATEST(tong_donhang - 1, 0),
            tong_tien_da_chi = GREATEST(tong_tien_da_chi - ?, 0)
        WHERE id = ?",
        'di',
        [$amount, $customerId]
    );
    recalculateCustomerRank($customerId);
}
?>
