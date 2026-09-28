<?php
// Load bootstrap first
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication
requireApiAuth();

try {
    $productId = get('id', 0, 'int');
    
    if ($productId <= 0) {
        jsonResponse(['success' => false, 'error' => 'Invalid product ID'], 400);
    }
    
    // Lấy các nhóm lựa chọn của sản phẩm
    $groups = db_fetch_all(
        "SELECT 
            nlc.id,
            nlc.ten_nhom,
            nlc.mota,
            snlc.bat_buoc
        FROM sanpham_nhom_lua_chon snlc
        INNER JOIN nhom_lua_chon nlc ON snlc.nhom_lua_chon_id = nlc.id
        WHERE snlc.sanpham_id = ? AND nlc.dang_hoat_dong = 1
        ORDER BY nlc.thu_tu, nlc.ten_nhom",
        'i',
        [$productId]
    );
    
    // Lấy các lựa chọn con cho mỗi nhóm
    foreach ($groups as &$group) {
        $choices = db_fetch_all(
            "SELECT id, ten_lua_chon, gia_tang_them, thu_tu
            FROM lua_chon
            WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1
            ORDER BY thu_tu, ten_lua_chon",
            'i',
            [$group['id']]
        );
        $group['lua_chon'] = $choices ? $choices : [];
    }
    unset($group);
    
    jsonResponse([
        'success' => true,
        'choices' => $groups
    ]);
    
} catch (Exception $e) {
    error_log("Error in get_product_choices: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => 'Internal server error'], 500);
}
?>

