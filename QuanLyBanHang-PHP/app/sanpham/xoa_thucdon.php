<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('thucdon.php');
}

try {
    // Get category data
    $row = db_fetch_one("SELECT * FROM danhmuc WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy thực đơn.');
        redirect('thucdon.php');
    }
    
    // Check if category has products
    $productCount = db_fetch_one(
        "SELECT COUNT(*) as cnt FROM sanpham WHERE danhmuc_id = ?",
        'i',
        [$id]
    );
    $hasProducts = $productCount && $productCount['cnt'] > 0;
    $productCountNum = $hasProducts ? (int)$productCount['cnt'] : 0;
    
    // Check if there are child categories
    $childCount = db_fetch_one(
        "SELECT COUNT(*) as cnt FROM danhmuc WHERE danhmuc_cha = ?",
        'i',
        [$id]
    );
    $hasChildren = $childCount && $childCount['cnt'] > 0;
    $childCountNum = $hasChildren ? (int)$childCount['cnt'] : 0;
    
    $notice = '';
    
    // Handle deletion
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
        try {
            // Re-check counts before deletion
            $productCount = db_fetch_one(
                "SELECT COUNT(*) as cnt FROM sanpham WHERE danhmuc_id = ?",
                'i',
                [$id]
            );
            $hasProducts = $productCount && $productCount['cnt'] > 0;
            
            $childCount = db_fetch_one(
                "SELECT COUNT(*) as cnt FROM danhmuc WHERE danhmuc_cha = ?",
                'i',
                [$id]
            );
            $hasChildren = $childCount && $childCount['cnt'] > 0;
            
            if ($hasProducts) {
                $notice = 'Không thể xóa thực đơn này vì còn ' . $productCount['cnt'] . ' sản phẩm đang sử dụng.';
            } elseif ($hasChildren) {
                $notice = 'Không thể xóa thực đơn này vì còn ' . $childCount['cnt'] . ' danh mục con.';
            } else {
                global $conn;
                $stmt = $conn->prepare("DELETE FROM danhmuc WHERE id = ?");
                $stmt->bind_param('i', $id);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Xóa thực đơn thành công!');
                    redirect('thucdon.php');
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi xóa thực đơn. Vui lòng thử lại.';
                }
            }
        } catch (Exception $e) {
            error_log("Error deleting menu: " . $e->getMessage());
            $notice = 'Có lỗi xảy ra khi xóa thực đơn.';
        }
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/xoa_thucdon', [
        'row' => $row,
        'hasProducts' => $hasProducts,
        'hasChildren' => $hasChildren,
        'productCountNum' => $productCountNum,
        'childCountNum' => $childCountNum,
        'notice' => $notice ?? '',
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Xóa thực đơn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/xoa_thucdon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi xóa thực đơn.');
    redirect('thucdon.php');
}
?>

