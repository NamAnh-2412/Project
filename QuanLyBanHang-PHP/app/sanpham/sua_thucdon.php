<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('thucdon.php');
}

$notice = '';

try {
    // Get category data
    $row = db_fetch_one("SELECT * FROM danhmuc WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy thực đơn.');
        redirect('thucdon.php');
    }
    
    // Handle form submission
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $ten = post('ten_danhmuc');
        $mota = post('mota');
        $thu_tu = post('thu_tu', 0, 'int');
        
        // Validation
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên thực đơn.';
        } else {
            try {
                global $conn;
                
                // Update category
                $stmt = $conn->prepare(
                    "UPDATE danhmuc SET ten_danhmuc = ?, mota = ?, thu_tu = ? WHERE id = ?"
                );
                
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                
                $stmt->bind_param('ssii', $ten, $mota, $thu_tu, $id);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Cập nhật thực đơn thành công!');
                    redirect('thucdon.php');
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi cập nhật thực đơn. Vui lòng thử lại.';
                }
            } catch (Exception $e) {
                error_log("Error updating menu: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
        
        // Reload data if there was an error
        $row = db_fetch_one("SELECT * FROM danhmuc WHERE id = ?", 'i', [$id]);
    }
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/sua_thucdon', [
        'row' => $row,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Sửa thực đơn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/sua_thucdon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('thucdon.php');
}
?>

