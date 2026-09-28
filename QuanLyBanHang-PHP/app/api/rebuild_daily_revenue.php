<?php
// Load bootstrap first (initApi() is defined in helpers.php after bootstrap)
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication
requireApiAuth();

// Require POST method
requireApiMethod('POST');

try {
    global $conn;
    
    // 1) Aggregate by date from completed orders
    $sql = "SELECT DATE(ngay_tao) AS day_date, COALESCE(SUM(thanh_tien),0) AS total_amount, COUNT(*) AS order_count 
            FROM donhang 
            WHERE trang_thai = ? 
            GROUP BY DATE(ngay_tao)";
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception('Prepare failed: ' . $conn->error);
    }
    
    $status = ORDER_STATUS_COMPLETED;
    $stmt->bind_param('s', $status);
    
    if (!$stmt->execute()) {
        throw new Exception('Query failed: ' . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $data = [];
    while ($row = $result->fetch_assoc()) {
        $data[] = $row;
    }
    $stmt->close();
    
    // 2) Replace into daily_revenue
    $conn->begin_transaction();
    
    $truncate_ok = $conn->query("TRUNCATE TABLE daily_revenue");
    if ($truncate_ok === false) {
        throw new Exception('Failed to truncate daily_revenue: ' . $conn->error);
    }
    
    $ins = $conn->prepare("REPLACE INTO daily_revenue (day_date, total_amount, order_count) VALUES (?, ?, ?)");
    if (!$ins) {
        throw new Exception('Prepare failed: ' . $conn->error);
    }
    
    $inserted = 0;
    foreach ($data as $row) {
        $day = $row['day_date'];
        $total = (float)$row['total_amount'];
        $cnt = (int)$row['order_count'];
        
        $ins->bind_param('sdi', $day, $total, $cnt);
        if (!$ins->execute()) {
            $conn->rollback();
            throw new Exception('Insert failed for ' . $day . ': ' . $ins->error);
        }
        $inserted++;
    }
    $ins->close();
    
    $conn->commit();
    
    jsonResponse([
        'success' => true,
        'message' => 'Rebuild thành công!',
        'inserted' => $inserted,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    if (isset($conn) && $conn->in_transaction) {
        $conn->rollback();
    }
    error_log("Error rebuilding daily_revenue: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => $e->getMessage()], 500);
}
?>

