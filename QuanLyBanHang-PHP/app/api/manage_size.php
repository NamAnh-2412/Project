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
    $action = post('action', '', 'raw');
    $product_id = post('product_id', 0, 'int');
    
    if (!$product_id) {
        jsonResponse(['success' => false, 'message' => 'ID sản phẩm không hợp lệ'], 400);
    }
    
    // Verify product exists
    $product = db_fetch_one("SELECT id FROM sanpham WHERE id = ?", 'i', [$product_id]);
    if (!$product) {
        jsonResponse(['success' => false, 'message' => 'Không tìm thấy sản phẩm'], 404);
    }
    
    switch ($action) {
        case 'add':
            $ten_size = trim(post('ten_size', '', 'string'));
            $gia = post('gia', 0, 'float');
            
            if (empty($ten_size)) {
                jsonResponse(['success' => false, 'message' => 'Tên size không được để trống'], 400);
            }
            
            if ($gia < 0) {
                jsonResponse(['success' => false, 'message' => 'Giá không được âm'], 400);
            }
            
            // Check duplicate
            $existing = db_fetch_one(
                "SELECT id FROM kichthuoc_sanpham WHERE sanpham_id = ? AND ten_size = ? AND dang_hoat_dong = 1",
                'is',
                [$product_id, $ten_size]
            );
            
            if ($existing) {
                jsonResponse(['success' => false, 'message' => 'Size này đã tồn tại'], 400);
            }
            
            // Insert
            $result = db_query(
                "INSERT INTO kichthuoc_sanpham (sanpham_id, ten_size, gia, dang_hoat_dong) VALUES (?, ?, ?, 1)",
                'isd',
                [$product_id, $ten_size, $gia]
            );
            
            if ($result) {
                $size_id = db_fetch_one("SELECT LAST_INSERT_ID() as id")['id'] ?? null;
                jsonResponse([
                    'success' => true,
                    'message' => 'Thêm size thành công',
                    'size' => [
                        'id' => $size_id,
                        'ten_size' => $ten_size,
                        'gia' => $gia
                    ]
                ]);
            } else {
                jsonResponse(['success' => false, 'message' => 'Không thể thêm size'], 500);
            }
            break;
            
        case 'update':
            $size_id = post('size_id', 0, 'int');
            $ten_size = trim(post('ten_size', '', 'string'));
            $gia = post('gia', 0, 'float');
            
            if (!$size_id) {
                jsonResponse(['success' => false, 'message' => 'ID size không hợp lệ'], 400);
            }
            
            if (empty($ten_size)) {
                jsonResponse(['success' => false, 'message' => 'Tên size không được để trống'], 400);
            }
            
            if ($gia < 0) {
                jsonResponse(['success' => false, 'message' => 'Giá không được âm'], 400);
            }
            
            // Verify size belongs to product
            $size = db_fetch_one(
                "SELECT id FROM kichthuoc_sanpham WHERE id = ? AND sanpham_id = ?",
                'ii',
                [$size_id, $product_id]
            );
            
            if (!$size) {
                jsonResponse(['success' => false, 'message' => 'Không tìm thấy size'], 404);
            }
            
            // Check duplicate (excluding current size)
            $existing = db_fetch_one(
                "SELECT id FROM kichthuoc_sanpham WHERE sanpham_id = ? AND ten_size = ? AND id != ? AND dang_hoat_dong = 1",
                'isi',
                [$product_id, $ten_size, $size_id]
            );
            
            if ($existing) {
                jsonResponse(['success' => false, 'message' => 'Size này đã tồn tại'], 400);
            }
            
            // Update
            $result = db_query(
                "UPDATE kichthuoc_sanpham SET ten_size = ?, gia = ? WHERE id = ?",
                'sdi',
                [$ten_size, $gia, $size_id]
            );
            
            if ($result) {
                jsonResponse([
                    'success' => true,
                    'message' => 'Cập nhật size thành công',
                    'size' => [
                        'id' => $size_id,
                        'ten_size' => $ten_size,
                        'gia' => $gia
                    ]
                ]);
            } else {
                jsonResponse(['success' => false, 'message' => 'Không thể cập nhật size'], 500);
            }
            break;
            
        case 'delete':
            $size_id = post('size_id', 0, 'int');
            
            if (!$size_id) {
                jsonResponse(['success' => false, 'message' => 'ID size không hợp lệ'], 400);
            }
            
            // Verify size belongs to product
            $size = db_fetch_one(
                "SELECT id FROM kichthuoc_sanpham WHERE id = ? AND sanpham_id = ?",
                'ii',
                [$size_id, $product_id]
            );
            
            if (!$size) {
                jsonResponse(['success' => false, 'message' => 'Không tìm thấy size'], 404);
            }
            
            // Check if size is used in orders (soft delete if used)
            $used = db_fetch_one(
                "SELECT COUNT(*) as count FROM chitiet_donhang WHERE size LIKE (SELECT ten_size FROM kichthuoc_sanpham WHERE id = ?)",
                'i',
                [$size_id]
            );
            
            if ($used && $used['count'] > 0) {
                // Soft delete
                $result = db_query(
                    "UPDATE kichthuoc_sanpham SET dang_hoat_dong = 0 WHERE id = ?",
                    'i',
                    [$size_id]
                );
                $message = 'Size đã được ẩn (không thể xóa vì đã có trong đơn hàng)';
            } else {
                // Hard delete
                $result = db_query(
                    "DELETE FROM kichthuoc_sanpham WHERE id = ?",
                    'i',
                    [$size_id]
                );
                $message = 'Xóa size thành công';
            }
            
            if ($result) {
                jsonResponse(['success' => true, 'message' => $message]);
            } else {
                jsonResponse(['success' => false, 'message' => 'Không thể xóa size'], 500);
            }
            break;
            
        case 'list':
            $sizes = db_fetch_all(
                "SELECT id, ten_size, gia, dang_hoat_dong FROM kichthuoc_sanpham WHERE sanpham_id = ? ORDER BY id",
                'i',
                [$product_id]
            );
            
            jsonResponse(['success' => true, 'sizes' => $sizes]);
            break;
            
        default:
            jsonResponse(['success' => false, 'message' => 'Action không hợp lệ'], 400);
    }
    
} catch (Exception $e) {
    error_log("Error in manage_size API: " . $e->getMessage());
    jsonResponse(['success' => false, 'message' => 'Có lỗi xảy ra: ' . $e->getMessage()], 500);
}
?>

