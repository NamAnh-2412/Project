<?php
require_once '../core/bootstrap.php';
requireLogin();

try {
    $id = (int)get('id', 0);
    if ($id <= 0) {
        setFlashMessage('error', 'ID phiếu không hợp lệ.');
        redirect('index.php?tab=kiemkho');
    }
    
    $phieu = db_fetch_one("SELECT * FROM kiemkho WHERE id = ?", 'i', [$id]);
    if (!$phieu) {
        setFlashMessage('error', 'Không tìm thấy phiếu.');
        redirect('index.php?tab=kiemkho');
    }
    
    if ($phieu['trang_thai'] !== 'cho_duyet') {
        setFlashMessage('error', 'Chỉ có thể hủy phiếu đang chờ duyệt.');
        redirect('view_kiemkho.php?id=' . $id);
    }
    
    global $conn;
    $stmt = $conn->prepare("UPDATE kiemkho SET trang_thai = 'da_huy' WHERE id = ?");
    $stmt->bind_param('i', $id);
    
    if ($stmt->execute()) {
        setFlashMessage('success', 'Đã hủy phiếu kiểm kho thành công.');
    } else {
        setFlashMessage('error', 'Không thể hủy phiếu.');
    }
    $stmt->close();
    
    redirect('view_kiemkho.php?id=' . $id);
} catch (Exception $e) {
    error_log("Error in cancel_kiemkho: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('index.php?tab=kiemkho');
}
?>
