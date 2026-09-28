<?php
// Disable output buffering and ensure clean output
if (ob_get_level()) {
    ob_end_clean();
}
ob_start();

require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

// Clear any output that might have been generated
ob_clean();

header('Content-Type: application/json; charset=utf-8');

try {
    // Get JSON input
    $body = json_decode(file_get_contents('php://input'), true);
    
    if (!$body) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Dữ liệu không hợp lệ'], 400);
    }
    
    $id = (int)($body['id'] ?? 0);
    $qty = (float)($body['qty'] ?? 0);
    $don_gia = isset($body['don_gia']) ? (float)$body['don_gia'] : 0.00;
    $han_su_dung = isset($body['han_su_dung']) && !empty($body['han_su_dung']) ? $body['han_su_dung'] : null;
    
    if ($id <= 0 || $qty <= 0) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Dữ liệu không hợp lệ'], 400);
    }
    
    // Get material info
    $row = db_fetch_one(
        "SELECT id, ten_nguyenlieu, don_vi_tinh, so_luong_ton, gia_nhap FROM nguyenvatlieu WHERE id = ? LIMIT 1",
        'i',
        [$id]
    );
    
    if (!$row) {
        ob_clean();
        jsonResponse(['success' => false, 'error' => 'Không tìm thấy mặt hàng'], 404);
    }
    
    // Use provided price or default to gia_nhap from database
    if ($don_gia <= 0 && !empty($row['gia_nhap'])) {
        $don_gia = (float)$row['gia_nhap'];
    }
    
    // Prepare cart item
    $item = [
        'id' => (int)$row['id'],
        'name' => $row['ten_nguyenlieu'],
        'don_vi' => $row['don_vi_tinh'],
        'qty' => $qty,
        'don_gia' => $don_gia,
        'han_su_dung' => $han_su_dung,
        'current' => (float)$row['so_luong_ton']
    ];
    
    // Initialize cart if not exists
    if (!isset($_SESSION['kho_cart'])) {
        $_SESSION['kho_cart'] = [];
    }
    
    // Check if item exists in cart with same price and expiry date, sum qty if found
    // Otherwise, add as new item
    $found = false;
    foreach ($_SESSION['kho_cart'] as &$c) {
        if ($c['id'] == $id && 
            (float)$c['don_gia'] === (float)$don_gia && 
            ($c['han_su_dung'] ?? null) === $han_su_dung) {
            $c['qty'] += $qty;
            $found = true;
            break;
        }
    }
    
    if (!$found) {
        $_SESSION['kho_cart'][] = $item;
    }
    
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse(['success' => true, 'cart' => $_SESSION['kho_cart']]);
    
} catch (Exception $e) {
    error_log("Error in kho/add_cart: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse(['success' => false, 'error' => 'Lỗi hệ thống: ' . $e->getMessage()], 500);
} catch (Error $e) {
    error_log("Fatal error in kho/add_cart: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse(['success' => false, 'error' => 'Lỗi hệ thống: ' . $e->getMessage()], 500);
}
?>

