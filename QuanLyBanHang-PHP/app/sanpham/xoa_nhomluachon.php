<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('nhomluachon.php');
}

try {
    // Get choice group data
    $row = db_fetch_one("SELECT * FROM nhom_lua_chon WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nhóm lựa chọn.');
        redirect('nhomluachon.php');
    }
    
    // Check if choice group has choices
    $choiceCount = db_fetch_one(
        "SELECT COUNT(*) as cnt FROM lua_chon WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1",
        'i',
        [$id]
    );
    $hasChoices = $choiceCount && $choiceCount['cnt'] > 0;
    $choiceCountNum = $hasChoices ? (int)$choiceCount['cnt'] : 0;
    
    // Check if there are products linked to this choice group
    $productCount = db_fetch_one(
        "SELECT COUNT(DISTINCT snl.sanpham_id) as cnt 
         FROM sanpham_nhom_lua_chon snl 
         INNER JOIN sanpham s ON snl.sanpham_id = s.id 
         WHERE snl.nhom_lua_chon_id = ? AND s.dang_hoat_dong = 1",
        'i',
        [$id]
    );
    $hasProducts = $productCount && $productCount['cnt'] > 0;
    $productCountNum = $hasProducts ? (int)$productCount['cnt'] : 0;
    
    $notice = '';
    
    // Handle deletion
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
        try {
            // Re-check counts before deletion
            $choiceCount = db_fetch_one(
                "SELECT COUNT(*) as cnt FROM lua_chon WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1",
                'i',
                [$id]
            );
            $hasChoices = $choiceCount && $choiceCount['cnt'] > 0;
            
            $productCount = db_fetch_one(
                "SELECT COUNT(DISTINCT snl.sanpham_id) as cnt 
                 FROM sanpham_nhom_lua_chon snl 
                 INNER JOIN sanpham s ON snl.sanpham_id = s.id 
                 WHERE snl.nhom_lua_chon_id = ? AND s.dang_hoat_dong = 1",
                'i',
                [$id]
            );
            $hasProducts = $productCount && $productCount['cnt'] > 0;
            
            if ($hasProducts) {
                $notice = 'Không thể xóa nhóm lựa chọn này vì còn ' . $productCount['cnt'] . ' sản phẩm đang sử dụng.';
            } elseif ($hasChoices) {
                $notice = 'Không thể xóa nhóm lựa chọn này vì còn ' . $choiceCount['cnt'] . ' lựa chọn. Vui lòng xóa các lựa chọn trước.';
            } else {
                global $conn;
                
                // Start transaction
                $conn->begin_transaction();
                
                try {
                    // Delete from linking table first
                    $stmt1 = $conn->prepare("DELETE FROM sanpham_nhom_lua_chon WHERE nhom_lua_chon_id = ?");
                    $stmt1->bind_param('i', $id);
                    $stmt1->execute();
                    $stmt1->close();
                    
                    // Delete the choice group
                    $stmt2 = $conn->prepare("DELETE FROM nhom_lua_chon WHERE id = ?");
                    $stmt2->bind_param('i', $id);
                    
                    if ($stmt2->execute()) {
                        $stmt2->close();
                        $conn->commit();
                        setFlashMessage('success', 'Xóa nhóm lựa chọn thành công!');
                        redirect('nhomluachon.php');
                    } else {
                        $error = $stmt2->error;
                        $stmt2->close();
                        $conn->rollback();
                        error_log("SQL Execute Error: " . $error);
                        $notice = 'Lỗi khi xóa nhóm lựa chọn. Vui lòng thử lại.';
                    }
                } catch (Exception $e) {
                    $conn->rollback();
                    throw $e;
                }
            }
        } catch (Exception $e) {
            error_log("Error deleting choice group: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra khi xóa nhóm lựa chọn.';
        }
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/xoa_nhomluachon', [
        'row' => $row,
        'hasChoices' => $hasChoices,
        'hasProducts' => $hasProducts,
        'choiceCountNum' => $choiceCountNum,
        'productCountNum' => $productCountNum,
        'notice' => $notice ?? '',
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Xóa nhóm lựa chọn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/xoa_nhomluachon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi xóa nhóm lựa chọn.');
    redirect('nhomluachon.php');
}
?>

