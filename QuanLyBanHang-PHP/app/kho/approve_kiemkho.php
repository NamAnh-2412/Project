<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

try {
    $id = (int)get('id', 0);
    if ($id <= 0) {
        setFlashMessage('error', 'ID phiếu không hợp lệ.');
        redirect('index.php?tab=kiemkho');
    }
    
    $phieu = db_fetch_one("SELECT * FROM kiemkho WHERE id = ?", 'i', [$id]);
    if (!$phieu) {
        setFlashMessage('error', 'Không tìm thấy phiếu kiểm kho.');
        redirect('index.php?tab=kiemkho');
    }
    
    if ($phieu['trang_thai'] !== 'cho_duyet') {
        setFlashMessage('error', 'Phiếu này không ở trạng thái chờ duyệt.');
        redirect('view_kiemkho.php?id=' . $id);
    }
    
    global $conn;
    $conn->begin_transaction();
    
    try {
        // Lấy chi tiết
        $chitiet = db_fetch_all("SELECT * FROM chitiet_kiemkho WHERE kiemkho_id = ?", 'i', [$id]);
        
        $stmt_update = $conn->prepare("UPDATE nguyenvatlieu SET so_luong_ton = ? WHERE id = ?");
        
        foreach ($chitiet as $item) {
            $ton_thuc_te = (float)$item['ton_thuc_te'];
            $nguyenlieu_id = (int)$item['nguyenlieu_id'];
            
            $stmt_update->bind_param('di', $ton_thuc_te, $nguyenlieu_id);
            if (!$stmt_update->execute()) {
                throw new Exception('Không thể cập nhật số lượng tồn cho mặt hàng ID ' . $nguyenlieu_id);
            }
        }
        $stmt_update->close();
        
        // Cập nhật trạng thái phiếu
        $stmt_phieu = $conn->prepare("UPDATE kiemkho SET trang_thai = 'da_duyet' WHERE id = ?");
        $stmt_phieu->bind_param('i', $id);
        if (!$stmt_phieu->execute()) {
            throw new Exception('Không thể cập nhật trạng thái phiếu.');
        }
        $stmt_phieu->close();
        
        $conn->commit();
        setFlashMessage('success', 'Đã duyệt phiếu kiểm kho thành công. Số lượng thực tế đã được cập nhật vào kho.');
        redirect('view_kiemkho.php?id=' . $id);
        
    } catch (Exception $e) {
        $conn->rollback();
        throw $e;
    }
} catch (Exception $e) {
    error_log("Error in approve_kiemkho: " . $e->getMessage());
    setFlashMessage('error', 'Lỗi: ' . $e->getMessage());
    redirect('index.php?tab=kiemkho');
}
?>
