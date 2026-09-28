<?php
/**
 * Bootstrap File - Khởi tạo hệ thống
 * File này nên được include đầu tiên trong mỗi request
 */

if (ob_get_level() === 0) {
    ob_start();
}

// Đảm bảo session được khởi động
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Định nghĩa đường dẫn gốc
if (!defined('ROOT_PATH')) {
    define('ROOT_PATH', dirname(dirname(__DIR__)));
}
if (!defined('APP_PATH')) {
    define('APP_PATH', __DIR__ . '/..');
}
if (!defined('VIEWS_PATH')) {
    define('VIEWS_PATH', APP_PATH . '/views');
}
if (!defined('CORE_PATH')) {
    define('CORE_PATH', __DIR__);
}

// Load core files theo thứ tự
require_once CORE_PATH . '/constants.php';
require_once CORE_PATH . '/config.php';
require_once CORE_PATH . '/db.php';
require_once CORE_PATH . '/view.php';
require_once CORE_PATH . '/auth.php';
require_once CORE_PATH . '/helpers.php';
require_once CORE_PATH . '/OrderService.php';

