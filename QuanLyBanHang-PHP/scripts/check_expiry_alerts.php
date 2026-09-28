<?php
/**
 * Script kiểm tra và gửi cảnh báo hạn sử dụng
 * 
 * Chạy script này bằng cron job hàng ngày:
 * 0 8 * * * php /path/to/scripts/check_expiry_alerts.php
 * 
 * Hoặc chạy thủ công:
 * php scripts/check_expiry_alerts.php
 */

require_once __DIR__ . '/../app/core/bootstrap.php';

try {
    global $conn;
    
    // Check if tables exist
    $result = $conn->query("SHOW TABLES LIKE 'kho_lot'");
    if (!$result || $result->num_rows == 0) {
        echo "Bảng kho_lot chưa tồn tại. Vui lòng chạy migration trước.\n";
        exit(1);
    }
    
    $result = $conn->query("SHOW TABLES LIKE 'expiry_alerts'");
    if (!$result || $result->num_rows == 0) {
        echo "Bảng expiry_alerts chưa tồn tại. Vui lòng chạy migration trước.\n";
        exit(1);
    }
    
    $today = date('Y-m-d');
    $seven_days_later = date('Y-m-d', strtotime('+7 days'));
    $one_day_later = date('Y-m-d', strtotime('+1 day'));
    
    $alerts_sent = 0;
    
    // 1. Cảnh báo 7 ngày trước
    $sql_7days = "SELECT kl.*, nl.ten_nguyenlieu, nl.ma_nguyenlieu
                   FROM kho_lot kl
                   INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id
                   WHERE kl.trang_thai != 'da_huy'
                   AND kl.han_su_dung >= ? 
                   AND kl.han_su_dung <= ?
                   AND NOT EXISTS (
                       SELECT 1 FROM expiry_alerts ea
                       WHERE ea.lot_id = kl.id 
                       AND ea.loai_canh_bao = '7_ngay'
                       AND ea.ngay_canh_bao = ?
                   )";
    
    $stmt_7days = $conn->prepare($sql_7days);
    $stmt_7days->bind_param('sss', $today, $seven_days_later, $today);
    $stmt_7days->execute();
    $result_7days = $stmt_7days->get_result();
    
    $stmt_insert_alert = $conn->prepare(
        "INSERT INTO expiry_alerts (lot_id, nguyenlieu_id, loai_canh_bao, ngay_canh_bao, da_xem)
         VALUES (?, ?, '7_ngay', ?, 0)"
    );
    
    while ($row = $result_7days->fetch_assoc()) {
        $stmt_insert_alert->bind_param('iis', $row['id'], $row['nguyenlieu_id'], $today);
        if ($stmt_insert_alert->execute()) {
            $alerts_sent++;
            echo "[7 NGÀY] Cảnh báo: {$row['ten_nguyenlieu']} ({$row['ma_nguyenlieu']}) - HSD: {$row['han_su_dung']}\n";
            
            // TODO: Gửi email/notification ở đây
            // sendEmailAlert($row);
        }
    }
    $stmt_insert_alert->close();
    $stmt_7days->close();
    
    // 2. Cảnh báo 1 ngày trước
    $sql_1day = "SELECT kl.*, nl.ten_nguyenlieu, nl.ma_nguyenlieu
                  FROM kho_lot kl
                  INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id
                  WHERE kl.trang_thai != 'da_huy'
                  AND kl.han_su_dung = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM expiry_alerts ea
                      WHERE ea.lot_id = kl.id 
                      AND ea.loai_canh_bao = '1_ngay'
                      AND ea.ngay_canh_bao = ?
                  )";
    
    $stmt_1day = $conn->prepare($sql_1day);
    $stmt_1day->bind_param('ss', $one_day_later, $today);
    $stmt_1day->execute();
    $result_1day = $stmt_1day->get_result();
    
    $stmt_insert_alert = $conn->prepare(
        "INSERT INTO expiry_alerts (lot_id, nguyenlieu_id, loai_canh_bao, ngay_canh_bao, da_xem)
         VALUES (?, ?, '1_ngay', ?, 0)"
    );
    
    while ($row = $result_1day->fetch_assoc()) {
        $stmt_insert_alert->bind_param('iis', $row['id'], $row['nguyenlieu_id'], $today);
        if ($stmt_insert_alert->execute()) {
            $alerts_sent++;
            echo "[1 NGÀY] Cảnh báo: {$row['ten_nguyenlieu']} ({$row['ma_nguyenlieu']}) - HSD: {$row['han_su_dung']}\n";
            
            // TODO: Gửi email/notification ở đây
            // sendEmailAlert($row);
        }
    }
    $stmt_insert_alert->close();
    $stmt_1day->close();
    
    // 3. Cảnh báo đã hết hạn
    $sql_expired = "SELECT kl.*, nl.ten_nguyenlieu, nl.ma_nguyenlieu
                    FROM kho_lot kl
                    INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id
                    WHERE kl.trang_thai != 'da_huy'
                    AND kl.han_su_dung < ?
                    AND NOT EXISTS (
                        SELECT 1 FROM expiry_alerts ea
                        WHERE ea.lot_id = kl.id 
                        AND ea.loai_canh_bao = 'het_han'
                        AND ea.ngay_canh_bao = ?
                    )";
    
    $stmt_expired = $conn->prepare($sql_expired);
    $stmt_expired->bind_param('ss', $today, $today);
    $stmt_expired->execute();
    $result_expired = $stmt_expired->get_result();
    
    $stmt_insert_alert = $conn->prepare(
        "INSERT INTO expiry_alerts (lot_id, nguyenlieu_id, loai_canh_bao, ngay_canh_bao, da_xem)
         VALUES (?, ?, 'het_han', ?, 0)"
    );
    
    while ($row = $result_expired->fetch_assoc()) {
        $stmt_insert_alert->bind_param('iis', $row['id'], $row['nguyenlieu_id'], $today);
        if ($stmt_insert_alert->execute()) {
            $alerts_sent++;
            echo "[HẾT HẠN] Cảnh báo: {$row['ten_nguyenlieu']} ({$row['ma_nguyenlieu']}) - HSD: {$row['han_su_dung']}\n";
            
            // TODO: Gửi email/notification ở đây
            // sendEmailAlert($row);
        }
    }
    $stmt_insert_alert->close();
    $stmt_expired->close();
    
    echo "\nHoàn thành! Đã gửi {$alerts_sent} cảnh báo.\n";
    
} catch (Exception $e) {
    echo "Lỗi: " . $e->getMessage() . "\n";
    error_log("Error in check_expiry_alerts: " . $e->getMessage());
    exit(1);
}
?>

