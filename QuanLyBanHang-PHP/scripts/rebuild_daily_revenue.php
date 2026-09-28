<?php
// Script to (re)build the `daily_revenue` table from `donhang`.
// Usage: php scripts/rebuild_daily_revenue.php
// It uses the project's existing db.php which should expose $conn (mysqli).

require_once __DIR__ . '/../db.php';

if (!isset($conn) && !isset($GLOBALS['conn'])) {
    echo "Could not find database connection in db.php (expected \$conn).\n";
    exit(1);
}
// Use $conn (mysqli)

// 1) Aggregate by date from completed orders
$sql = "SELECT DATE(ngay_tao) AS day_date, COALESCE(SUM(thanh_tien),0) AS total_amount, COUNT(*) AS order_count FROM donhang WHERE trang_thai='hoan_thanh' GROUP BY DATE(ngay_tao)";
$res = $conn->query($sql);
if (!$res) {
    echo "Query failed: " . $conn->error . "\n";
    exit(1);
}

// 2) Replace into daily_revenue
$conn->begin_transaction();
$truncate_ok = $conn->query("TRUNCATE TABLE daily_revenue");
if ($truncate_ok === false) {
    echo "Failed to truncate daily_revenue: " . $conn->error . "\n";
    $conn->rollback();
    exit(1);
}

$ins = $conn->prepare("REPLACE INTO daily_revenue (day_date, total_amount, order_count) VALUES (?, ?, ?)");
if (!$ins) { echo "Prepare failed: " . $conn->error . "\n"; $conn->rollback(); exit(1); }

while ($row = $res->fetch_assoc()){
    $day = $row['day_date'];
    $total = $row['total_amount'];
    $cnt = $row['order_count'];
    $ins->bind_param('sdi', $day, $total, $cnt);
    if (!$ins->execute()){
        echo "Insert failed for $day: " . $ins->error . "\n";
        $conn->rollback();
        exit(1);
    }
}
$ins->close();
$conn->commit();

echo "daily_revenue rebuilt successfully (" . date('Y-m-d H:i:s') . ")\n";

?>
