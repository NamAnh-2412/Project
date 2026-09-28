<?php
// Bảo vệ file - không cho truy cập trực tiếp
if (basename($_SERVER['PHP_SELF']) === 'database.php') {
    header('HTTP/1.0 403 Forbidden');
    die('Access Denied - This file cannot be accessed directly.');
}

// Cấu hình database
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'bookingphong');
define('DB_CHARSET', 'utf8mb4');

// Tạo kết nối database
function getDBConnection() {
    try {
        $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ];
        $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
        return $pdo;
    } catch (PDOException $e) {
        die("Kết nối database thất bại: " . $e->getMessage());
    }
}
?>

