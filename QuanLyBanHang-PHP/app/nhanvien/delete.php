<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

try {
    // Get employee data
    $row = db_fetch_one("SELECT * FROM nhanvien WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nhân viên.');
        redirect('index.php');
    }
    
    // Handle deletion
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
        // Prevent deleting self
        if ($id == getCurrentUserId()) {
            setFlashMessage('error', 'Bạn không thể xóa chính mình.');
            redirect('index.php');
        }
        
        $result = db_query("DELETE FROM nhanvien WHERE id = ?", 'i', [$id]);
        
        if ($result) {
            setFlashMessage('success', 'Xóa nhân viên thành công!');
            redirect('index.php');
        } else {
            setFlashMessage('error', 'Lỗi khi xóa nhân viên. Vui lòng thử lại.');
        }
    }
} catch (Exception $e) {
    error_log("Error in nhanvien/delete: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi xóa nhân viên.');
    redirect('index.php');
}

$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhanvien/delete', [
    'row' => $row,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Xóa nhân viên',
    'page_css' => ['ui.css', 'employees.css']
]);
?>
