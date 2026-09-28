<?php
// Disable output buffering and ensure clean output
if (ob_get_level()) {
    ob_end_clean();
}
ob_start();

require_once '../core/bootstrap.php';
requireLogin();

ob_clean();
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonResponse(['success' => false, 'error' => 'Phải POST'], 405);
}

$body = json_decode(file_get_contents('php://input'), true);
if (!$body || empty($body['items'])) {
    jsonResponse(['success' => false, 'error' => 'Dữ liệu không hợp lệ hoặc không có mặt hàng nào.'], 400);
}

$ghi_chu = isset($body['ghi_chu']) ? sanitize($body['ghi_chu']) : '';
$items = $body['items'];

try {
    global $conn;
    $nhanvien_id = getCurrentUserId();
    
    $conn->begin_transaction();
    
    // 1. Tạo phiếu kiểm kho
    $ma_phieu_kiem = 'PK' . date('ymdHis');
    $ngay_kiem = date('Y-m-d');
    
    $stmt = $conn->prepare("INSERT INTO kiemkho (ma_phieu_kiem, ngay_kiem, nhanvien_kiem, ghi_chu, trang_thai) VALUES (?, ?, ?, ?, 'cho_duyet')");
    $stmt->bind_param('ssis', $ma_phieu_kiem, $ngay_kiem, $nhanvien_id, $ghi_chu);
    
    if (!$stmt->execute()) {
        throw new Exception('Không thể tạo phiếu kiểm kho: ' . $stmt->error);
    }
    
    $kiemkho_id = $conn->insert_id;
    $stmt->close();
    
    // 2. Thêm chi tiết
    $stmt_chitiet = $conn->prepare("INSERT INTO chitiet_kiemkho (kiemkho_id, nguyenlieu_id, ton_he_thong, ton_thuc_te, chenh_lech, ly_do) VALUES (?, ?, ?, ?, ?, ?)");
    
    // Lấy tồn hệ thống mới nhất để đảm bảo chính xác
    $stmt_ton = $conn->prepare("SELECT so_luong_ton FROM nguyenvatlieu WHERE id = ?");
    
    foreach ($items as $item) {
        $nguyenlieu_id = (int)$item['id'];
        $ton_thuc_te = (float)$item['act_qty'];
        $ly_do = isset($item['reason']) ? sanitize($item['reason']) : '';
        
        $stmt_ton->bind_param('i', $nguyenlieu_id);
        $stmt_ton->execute();
        $res = $stmt_ton->get_result();
        $row = $res->fetch_assoc();
        $ton_he_thong = (float)($row['so_luong_ton'] ?? 0);
        
        $chenh_lech = $ton_thuc_te - $ton_he_thong;
        
        $stmt_chitiet->bind_param('iiddds', $kiemkho_id, $nguyenlieu_id, $ton_he_thong, $ton_thuc_te, $chenh_lech, $ly_do);
        if (!$stmt_chitiet->execute()) {
            throw new Exception('Lỗi lưu chi tiết mặt hàng ID ' . $nguyenlieu_id);
        }
    }
    
    $stmt_ton->close();
    $stmt_chitiet->close();
    
    $conn->commit();
    
    // Xóa giỏ hàng
    unset($_SESSION['kiemkho_cart']);
    
    jsonResponse(['success' => true]);
    
} catch (Exception $e) {
    if (isset($conn) && $conn->in_transaction) {
        $conn->rollback();
    }
    error_log("Error in commit_kiemkho: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => $e->getMessage()], 500);
}
?>
