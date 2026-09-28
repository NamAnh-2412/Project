<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $notice = '';
    $productId = get('id', 0, 'int');
    
    if ($productId <= 0) {
        setFlashMessage('error', 'ID sản phẩm không hợp lệ');
        redirect('index.php');
    }
    
    // Lấy thông tin sản phẩm
    $product = db_fetch_one(
        "SELECT * FROM sanpham WHERE id = ? LIMIT 1",
        'i',
        [$productId]
    );
    
    if (!$product) {
        setFlashMessage('error', 'Không tìm thấy sản phẩm');
        redirect('index.php');
    }
    
    // Xử lý cập nhật liên kết
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'update_links') {
        $selectedGroups = post('nhom_lua_chon', [], 'array');
        $requiredGroups = post('bat_buoc', [], 'array');
        
        try {
            global $conn;
            
            // Xóa tất cả liên kết cũ (dùng prepared statement)
            $deleteStmt = $conn->prepare("DELETE FROM sanpham_nhom_lua_chon WHERE sanpham_id = ?");
            $deleteStmt->bind_param('i', $productId);
            $deleteStmt->execute();
            $deleteStmt->close();
            
            // Thêm liên kết mới
            $stmt = $conn->prepare("INSERT INTO sanpham_nhom_lua_chon (sanpham_id, nhom_lua_chon_id, bat_buoc) VALUES (?, ?, ?)");
            
            foreach ($selectedGroups as $groupId) {
                $groupId = (int)$groupId;
                if ($groupId > 0) {
                    $batBuoc = in_array($groupId, $requiredGroups) ? 1 : 0;
                    $stmt->bind_param('iii', $productId, $groupId, $batBuoc);
                    $stmt->execute();
                }
            }
            
            $stmt->close();
            setFlashMessage('success', 'Cập nhật liên kết nhóm lựa chọn thành công!');
            redirect("lienket_luachon.php?id=$productId");
        } catch (Exception $e) {
            error_log("Error updating product choice links: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra khi cập nhật. Vui lòng thử lại.';
        }
    }
    
    // Lấy tất cả nhóm lựa chọn
    $allGroups = db_fetch_all(
        "SELECT * FROM nhom_lua_chon WHERE dang_hoat_dong = 1 ORDER BY thu_tu, ten_nhom",
        '',
        []
    );
    
    // Lấy các nhóm đã liên kết với sản phẩm
    $linkedGroups = db_fetch_all(
        "SELECT nhom_lua_chon_id, bat_buoc FROM sanpham_nhom_lua_chon WHERE sanpham_id = ?",
        'i',
        [$productId]
    );
    
    $linkedGroupIds = [];
    $requiredGroupIds = [];
    foreach ($linkedGroups as $link) {
        $linkedGroupIds[] = $link['nhom_lua_chon_id'];
        if ($link['bat_buoc']) {
            $requiredGroupIds[] = $link['nhom_lua_chon_id'];
        }
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/lienket_luachon', [
        'product' => $product,
        'allGroups' => $allGroups,
        'linkedGroupIds' => $linkedGroupIds,
        'requiredGroupIds' => $requiredGroupIds,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Liên kết nhóm lựa chọn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/lienket_luachon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php');
}
?>

