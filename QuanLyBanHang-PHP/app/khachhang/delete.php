<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

// Get customer data
$row = db_fetch_one("SELECT * FROM khachhang WHERE id = ?", 'i', [$id]);
if (!$row) {
    setFlashMessage('error', 'Không tìm thấy khách hàng.');
    redirect('index.php');
}

// Handle deletion
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
    try {
        $result = db_query("DELETE FROM khachhang WHERE id = ?", 'i', [$id]);
        
        if ($result) {
            setFlashMessage('success', 'Xóa khách hàng thành công!');
            redirect('index.php');
        } else {
            setFlashMessage('error', 'Lỗi khi xóa khách hàng. Vui lòng thử lại.');
        }
    } catch (Exception $e) {
        error_log("Error deleting customer: " . $e->getMessage());
        setFlashMessage('error', 'Có lỗi xảy ra khi xóa khách hàng.');
    }
}

$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('khachhang/delete', [
    'row' => $row,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Xóa khách hàng',
    'page_css' => ['ui.css', 'customers.css']
]);
?>
