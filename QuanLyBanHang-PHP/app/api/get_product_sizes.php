<?php
// Load bootstrap first
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication
requireApiAuth();

try {
    $product_id = get('product_id', 0, 'int');
    
    if (!$product_id) {
        jsonResponse(['success' => false, 'message' => 'ID sản phẩm không hợp lệ'], 400);
    }
    
    // Get sizes for this product (only active ones)
    $sizes = db_fetch_all(
        "SELECT id, ten_size, gia, dang_hoat_dong 
         FROM kichthuoc_sanpham 
         WHERE sanpham_id = ? AND dang_hoat_dong = 1 
         ORDER BY id",
        'i',
        [$product_id]
    );
    
    jsonResponse([
        'success' => true,
        'sizes' => $sizes,
        'has_sizes' => !empty($sizes)
    ]);
    
} catch (Exception $e) {
    error_log("Error in get_product_sizes API: " . $e->getMessage());
    jsonResponse(['success' => false, 'message' => 'Có lỗi xảy ra: ' . $e->getMessage()], 500);
}
?>

