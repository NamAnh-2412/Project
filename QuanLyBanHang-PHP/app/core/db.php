<?php
/**
 * Database Connection
 * Quản lý kết nối database với error handling tốt hơn
 */

// Load constants nếu chưa được load
if (!defined('DB_HOST')) {
    require_once __DIR__ . '/constants.php';
}

class Database {
    private static $instance = null;
    private $conn;
    
    private function __construct() {
        try {
            $this->conn = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
            
            if ($this->conn->connect_error) {
                $errorMsg = 'Kết nối database thất bại: ' . $this->conn->connect_error;
                error_log($errorMsg);
                throw new Exception($errorMsg);
            }
            
            $this->conn->set_charset(DB_CHARSET);
        } catch (Exception $e) {
            error_log("Database connection error: " . $e->getMessage());
            // Don't die() here, let the caller handle it
            throw $e;
        }
    }
    
    public static function getInstance() {
        if (self::$instance === null) {
            try {
                self::$instance = new self();
            } catch (Exception $e) {
                error_log("Failed to create Database instance: " . $e->getMessage());
                // Return null or throw, depending on your error handling strategy
                // For now, we'll let it throw so callers can catch it
                throw $e;
            }
        }
        return self::$instance;
    }
    
    public function getConnection() {
        return $this->conn;
    }
    
    // Prevent cloning
    private function __clone() {}
    
    // Prevent unserialization
    public function __wakeup() {
        throw new Exception("Cannot unserialize singleton");
    }
}

// Tạo connection global để tương thích với code cũ
// Wrap in try-catch để tránh fatal error nếu database không kết nối được
try {
    $dbInstance = Database::getInstance();
    $conn = $dbInstance->getConnection();
    
    // Test connection
    if ($conn && $conn->ping()) {
        // Connection is good
    } else {
        error_log("Database connection ping failed");
        $conn = null;
    }
} catch (Exception $e) {
    error_log("Failed to initialize global database connection: " . $e->getMessage());
    error_log("Exception type: " . get_class($e));
    error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
    // Set $conn to null, functions will need to handle this
    $conn = null;
} catch (Error $e) {
    error_log("Fatal error initializing database connection: " . $e->getMessage());
    error_log("File: " . $e->getFile() . " Line: " . $e->getLine());
    $conn = null;
}

/**
 * Helper function để thực thi prepared statement an toàn
 * 
 * @param string $sql SQL query với placeholders
 * @param string $types Types string (s=string, i=int, d=double, b=blob)
 * @param array $params Parameters array
 * @return mysqli_result|bool Result object hoặc false nếu lỗi
 */
function db_query($sql, $types = '', $params = []) {
    global $conn;
    
    if ($conn === null) {
        error_log("Database connection is null. Cannot execute query: " . $sql);
        return false;
    }
    
    if (empty($params)) {
        // Query không có parameters
        return $conn->query($sql);
    }
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        error_log("SQL Prepare Error: " . $conn->error);
        return false;
    }
    
    if (!empty($types) && !empty($params)) {
        $stmt->bind_param($types, ...$params);
    }
    
    if (!$stmt->execute()) {
        error_log("SQL Execute Error: " . $stmt->error);
        $stmt->close();
        return false;
    }
    
    // Kiểm tra xem có phải SELECT query không
    $isSelect = stripos(trim($sql), 'SELECT') === 0;
    
    if ($isSelect) {
        $result = $stmt->get_result();
        $stmt->close();
        return $result;
    } else {
        // Với UPDATE, INSERT, DELETE - trả về true nếu thành công
        // affected_rows có thể là 0 (không có row nào thay đổi) nhưng vẫn là thành công
        $affected = $stmt->affected_rows;
        $stmt->close();
        // Trả về true nếu không có lỗi (affected_rows >= 0)
        return true;
    }
}

/**
 * Helper function để lấy một row từ database
 */
function db_fetch_one($sql, $types = '', $params = []) {
    $result = db_query($sql, $types, $params);
    if ($result && $result->num_rows > 0) {
        return $result->fetch_assoc();
    }
    return null;
}

/**
 * Helper function để lấy tất cả rows từ database
 */
function db_fetch_all($sql, $types = '', $params = []) {
    $result = db_query($sql, $types, $params);
    if (!$result) {
        return [];
    }
    
    $rows = [];
    while ($row = $result->fetch_assoc()) {
        $rows[] = $row;
    }
    return $rows;
}
?>

