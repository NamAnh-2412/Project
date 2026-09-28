<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('nguyenlieu.php');
}

try {
    // Get ingredient data
    $row = db_fetch_one("SELECT * FROM nguyenvatlieu WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nguyên liệu.');
        redirect('nguyenlieu.php');
    }
    
    // Handle deletion
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
        $result = db_query("DELETE FROM nguyenvatlieu WHERE id = ?", 'i', [$id]);
        
        if ($result) {
            setFlashMessage('success', 'Xóa nguyên liệu thành công!');
            redirect('nguyenlieu.php');
        } else {
            setFlashMessage('error', 'Lỗi khi xóa nguyên liệu. Vui lòng thử lại.');
        }
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/xoa_nguyenlieu', [
        'row' => $row,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Xóa nguyên liệu',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/xoa_nguyenlieu: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi xóa nguyên liệu.');
    redirect('nguyenlieu.php');
}
?>

