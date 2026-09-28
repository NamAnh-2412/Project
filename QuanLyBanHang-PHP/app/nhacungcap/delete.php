<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

// Get supplier data
$row = db_fetch_one("SELECT * FROM nhacungcap WHERE id = ?", 'i', [$id]);
if (!$row) {
    setFlashMessage('error', 'Không tìm thấy nhà cung cấp.');
    redirect('index.php');
}

// Check if supplier is used in nhapkho
$usedInImports = db_fetch_one(
    "SELECT COUNT(*) as count FROM nhapkho WHERE nha_cung_cap_id = ?",
    'i',
    [$id]
);

$hasImports = ($usedInImports && isset($usedInImports['count']) && $usedInImports['count'] > 0);

// Handle deletion
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm'])) {
    try {
        if ($hasImports) {
            // Cannot delete, only change status
            $result = db_query(
                "UPDATE nhacungcap SET trang_thai = 'ngung_hop_tac' WHERE id = ?",
                'i',
                [$id]
            );
            
            if ($result) {
                setFlashMessage('success', 'Nhà cung cấp đã được đổi trạng thái thành "Ngừng hợp tác" (không thể xóa vì đã có trong phiếu nhập kho).');
                redirect('index.php');
            } else {
                setFlashMessage('error', 'Lỗi khi cập nhật nhà cung cấp. Vui lòng thử lại.');
            }
        } else {
            // Can delete
            $result = db_query("DELETE FROM nhacungcap WHERE id = ?", 'i', [$id]);
            
            if ($result) {
                setFlashMessage('success', 'Xóa nhà cung cấp thành công!');
                redirect('index.php');
            } else {
                setFlashMessage('error', 'Lỗi khi xóa nhà cung cấp. Vui lòng thử lại.');
            }
        }
    } catch (Exception $e) {
        error_log("Error deleting supplier: " . $e->getMessage());
        setFlashMessage('error', 'Có lỗi xảy ra khi xóa nhà cung cấp.');
    }
}

$currentUser = getCurrentUser();
$ten_nv = $currentUser['username'] ?? 'Admin';
$first_char = strtoupper(substr($ten_nv, 0, 1));

renderView('nhacungcap/delete', [
    'row' => $row,
    'hasImports' => $hasImports,
    'usedInImports' => $usedInImports,
    'ten_nv' => $ten_nv,
    'first_char' => $first_char,
    'page_title' => 'Xóa nhà cung cấp',
    'page_css' => ['ui.css', 'customers.css']
]);
?>

