<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonResponse(['success' => false, 'error' => 'Phải POST'], 405);
}

// Get data from POST body
$body = json_decode(file_get_contents('php://input'), true);
$lot_id = isset($body['lot_id']) ? (int)$body['lot_id'] : 0;
$ly_do = isset($body['ly_do']) ? sanitize($body['ly_do']) : 'Hết hạn sử dụng';

if ($lot_id <= 0) {
    jsonResponse(['success' => false, 'error' => 'ID lô không hợp lệ'], 400);
}

try {
    global $conn;
    
    // Get current user ID
    $nhanvien_id = getCurrentUserId();
    if (!$nhanvien_id) {
        throw new Exception('Không xác định được nhân viên');
    }
    
    // Start transaction
    $conn->begin_transaction();
    
    try {
        // 1. Get lot information
        $lot = db_fetch_one(
            "SELECT kl.*, nl.ten_nguyenlieu 
             FROM kho_lot kl
             INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id
             WHERE kl.id = ? AND kl.trang_thai != 'da_huy'",
            'i',
            [$lot_id]
        );
        
        if (!$lot) {
            throw new Exception('Không tìm thấy lô nguyên liệu hoặc đã bị hủy');
        }
        
        $so_luong = (float)$lot['so_luong_con_lai'];
        $nguyenlieu_id = (int)$lot['nguyenlieu_id'];
        
        if ($so_luong <= 0) {
            throw new Exception('Số lượng còn lại đã bằng 0');
        }
        
        // 2. Update nguyenvatlieu: Trừ số lượng tồn kho
        $stmt_update = $conn->prepare(
            "UPDATE nguyenvatlieu SET so_luong_ton = so_luong_ton - ? WHERE id = ?"
        );
        $stmt_update->bind_param('di', $so_luong, $nguyenlieu_id);
        
        if (!$stmt_update->execute()) {
            throw new Exception('Không thể cập nhật số lượng tồn kho: ' . $stmt_update->error);
        }
        $stmt_update->close();
        
        // 3. Update kho_lot: Đánh dấu đã hủy
        $stmt_lot = $conn->prepare(
            "UPDATE kho_lot 
             SET trang_thai = 'da_huy',
                 ngay_huy = NOW(),
                 nhanvien_huy = ?,
                 ly_do_huy = ?,
                 so_luong_con_lai = 0
             WHERE id = ?"
        );
        $stmt_lot->bind_param('isi', $nhanvien_id, $ly_do, $lot_id);
        
        if (!$stmt_lot->execute()) {
            throw new Exception('Không thể cập nhật trạng thái lô: ' . $stmt_lot->error);
        }
        $stmt_lot->close();
        
        // 4. Insert into lich_su_huy (if table exists)
        $result = $conn->query("SHOW TABLES LIKE 'lich_su_huy'");
        if ($result && $result->num_rows > 0) {
            $stmt_history = $conn->prepare(
                "INSERT INTO lich_su_huy (lot_id, nguyenlieu_id, so_luong, nhanvien_id, ly_do, ngay_huy)
                 VALUES (?, ?, ?, ?, ?, NOW())"
            );
            $stmt_history->bind_param('iidis', $lot_id, $nguyenlieu_id, $so_luong, $nhanvien_id, $ly_do);
            
            if (!$stmt_history->execute()) {
                // Log error but don't fail the transaction
                error_log("Failed to insert into lich_su_huy: " . $stmt_history->error);
            }
            $stmt_history->close();
        }
        
        // Commit transaction
        $conn->commit();
        
        jsonResponse([
            'success' => true,
            'message' => 'Đã hủy nguyên liệu thành công',
            'data' => [
                'lot_id' => $lot_id,
                'nguyenlieu_id' => $nguyenlieu_id,
                'so_luong' => $so_luong
            ]
        ]);
        
    } catch (Exception $e) {
        $conn->rollback();
        throw $e;
    }
    
} catch (Exception $e) {
    error_log("Error in kho/expiry_cancel: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => $e->getMessage()], 500);
}
?>

