<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $id = (int)get('id', 0);
    
    if ($id <= 0) {
        setFlashMessage('error', 'ID phiếu nhập kho không hợp lệ.');
        redirect('index.php');
    }
    
    // Get phiếu nhập kho
    $phieu = db_fetch_one(
        "SELECT * FROM nhapkho WHERE id = ?",
        'i',
        [$id]
    );
    
    if (!$phieu) {
        setFlashMessage('error', 'Không tìm thấy phiếu nhập kho.');
        redirect('index.php');
    }
    
    // Check if already approved
    if ($phieu['trang_thai'] === 'da_duyet') {
        setFlashMessage('error', 'Không thể hủy phiếu nhập kho đã được duyệt.');
        redirect('view.php?id=' . $id);
    }
    
    if ($phieu['trang_thai'] === 'da_huy') {
        setFlashMessage('info', 'Phiếu nhập kho này đã bị hủy rồi.');
        redirect('view.php?id=' . $id);
    }
    
    // Update trạng thái thành 'da_huy'
    $result = db_query(
        "UPDATE nhapkho SET trang_thai = 'da_huy' WHERE id = ?",
        'i',
        [$id]
    );
    
    if ($result) {
        setFlashMessage('success', 'Đã hủy phiếu nhập kho thành công.');
        redirect('view.php?id=' . $id);
    } else {
        throw new Exception('Không thể hủy phiếu nhập kho.');
    }
    
} catch (Exception $e) {
    error_log("Error in kho/cancel: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra: ' . $e->getMessage());
    redirect('index.php');
}
?>

