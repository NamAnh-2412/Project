<?php
/**
 * View Helper - Render view files
 * Now uses layout system by default
 * 
 * @param string $viewName Name of the view file (without .php extension)
 * @param array $data Data to pass to the view
 * @param string|null $layout Layout name (null = auto-detect, 'none' = no layout)
 */
function renderView($viewName, $data = [], $layout = null) {
    // Auto-detect layout: auth views use 'auth', others use 'main'
    if ($layout === null) {
        if (strpos($viewName, 'auth/') === 0) {
            $layout = 'auth';
        } else {
            $layout = 'main';
        }
    }
    
    // If layout is 'none', render without layout (backward compatibility)
    if ($layout === 'none') {
        extract($data);
        $viewFile = __DIR__ . '/../views/' . $viewName . '.php';
        if (!file_exists($viewFile)) {
            die("View file not found: {$viewName}.php");
        }
        include $viewFile;
        return;
    }
    
    // Use layout system
    renderViewWithLayout($viewName, $data, $layout);
}

/**
 * Get base URL for assets (CSS, JS, images)
 * Tính toán đường dẫn từ document root dựa trên SCRIPT_NAME
 * 
 * @return string Base URL path (relative từ document root, có dấu / ở cuối)
 */
function getBaseUrl() {
    static $baseUrl = null;
    
    if ($baseUrl === null) {
        $scriptName = $_SERVER['SCRIPT_NAME'] ?? '/index.php';
        
        // Xác định project root (thư mục chứa index.php, assets/, js/)
        if (strpos($scriptName, '/app/') !== false) {
            // File đang chạy trong app/, project root là trước /app/
            // Ví dụ: /QuanLyBanHang/app/khachhang/index.php -> /QuanLyBanHang/
            $projectRoot = substr($scriptName, 0, strpos($scriptName, '/app/'));
        } else {
            // File đang chạy ở root (như index.php)
            // Ví dụ: /QuanLyBanHang/index.php -> /QuanLyBanHang/
            $projectRoot = dirname($scriptName);
            if ($projectRoot === '.' || $projectRoot === '/') {
                $projectRoot = '';
            }
        }
        
        // Đảm bảo có dấu / ở đầu (nếu không phải root)
        if ($projectRoot !== '' && substr($projectRoot, 0, 1) !== '/') {
            $projectRoot = '/' . $projectRoot;
        }
        // Đảm bảo có dấu / ở cuối
        if ($projectRoot !== '' && substr($projectRoot, -1) !== '/') {
            $projectRoot = $projectRoot . '/';
        }
        
        $baseUrl = $projectRoot;
    }
    
    return $baseUrl;
}

/**
 * Render view with auth layout (no sidebar)
 * 
 * @param string $viewName Name of the view file (without .php extension)
 * @param array $data Data to pass to the view
 */
function renderAuthView($viewName, $data = []) {
    renderViewWithLayout($viewName, $data, 'auth');
}

/**
 * Render view with layout
 * 
 * @param string $viewName Name of the view file (without .php extension)
 * @param array $data Data to pass to the view
 * @param string $layout Layout name (default: 'main')
 */
function renderViewWithLayout($viewName, $data = [], $layout = 'main') {
    // Auto-add user info if not provided and layout is 'main'
    if ($layout === 'main' && !isset($data['ten_nv'])) {
        $userInfo = getCurrentUserInfo();
        $data = array_merge($userInfo, $data);
    }
    
    // Extract data array to variables for the view
    extract($data);
    
    // Get the view file path
    $viewFile = __DIR__ . '/../views/' . $viewName . '.php';
    
    // Check if view file exists
    if (!file_exists($viewFile)) {
        die("View file not found: {$viewName}.php");
    }
    
    // Start output buffering for view content
    ob_start();
    include $viewFile;
    $content = ob_get_clean();
    
    // Get layout file path
    $layoutFile = __DIR__ . '/../views/layouts/' . $layout . '.php';
    
    // Check if layout file exists
    if (!file_exists($layoutFile)) {
        die("Layout file not found: {$layout}.php");
    }
    
    // Include layout with content
    include $layoutFile;
}

