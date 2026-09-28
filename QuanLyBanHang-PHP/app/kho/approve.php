<?php
require_once '../core/bootstrap.php';

// Require authentication and manager role
requireLogin();
requireManager();

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
    
    // Check if already approved or cancelled
    if ($phieu['trang_thai'] === 'da_duyet') {
        setFlashMessage('info', 'Phiếu nhập kho này đã được duyệt rồi.');
        redirect('view.php?id=' . $id);
    }
    
    if ($phieu['trang_thai'] === 'da_huy') {
        setFlashMessage('error', 'Không thể duyệt phiếu nhập kho đã bị hủy.');
        redirect('view.php?id=' . $id);
    }
    
    global $conn;
    
    // Check if kho_lot table exists
    $table_exists = false;
    $result = $conn->query("SHOW TABLES LIKE 'kho_lot'");
    if ($result && $result->num_rows > 0) {
        $table_exists = true;
    }
    
    // Start transaction
    $conn->begin_transaction();
    
    try {
        // 1. Get chi tiết phiếu nhập kho
        $chitiet = db_fetch_all(
            "SELECT * FROM chitiet_nhapkho WHERE nhapkho_id = ?",
            'i',
            [$id]
        );
        
        if (empty($chitiet)) {
            throw new Exception('Phiếu nhập kho không có chi tiết.');
        }
        
        // 2. Create lots in kho_lot (if table exists)
        if ($table_exists) {
            $stmt_lot = $conn->prepare(
                "INSERT INTO kho_lot (
                    chitiet_nhapkho_id, nguyenlieu_id, nhapkho_id,
                    so_luong_nhap, so_luong_con_lai, don_gia_nhap,
                    han_su_dung, ngay_nhap, trang_thai
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'con_hang')"
            );
            
            foreach ($chitiet as $item) {
                $chitiet_id = (int)$item['id'];
                $nguyenlieu_id = (int)$item['nguyenlieu_id'];
                $so_luong = (float)$item['so_luong_nhap'];
                $don_gia = (float)$item['don_gia_nhap'];
                $han_su_dung = !empty($item['han_su_dung']) ? $item['han_su_dung'] : date('Y-m-d', strtotime('+30 days')); // Default 30 days if not set
                $ngay_nhap = $phieu['ngay_nhap'];
                
                $stmt_lot->bind_param('iiidddss', 
                    $chitiet_id, 
                    $nguyenlieu_id, 
                    $id, // nhapkho_id
                    $so_luong, 
                    $so_luong, // so_luong_con_lai = so_luong_nhap initially
                    $don_gia,
                    $han_su_dung,
                    $ngay_nhap
                );
                
                if (!$stmt_lot->execute()) {
                    throw new Exception('Không thể tạo lô cho nguyên liệu ID ' . $nguyenlieu_id . ': ' . $stmt_lot->error);
                }
            }
            
            $stmt_lot->close();
        }
        
        // 3. Update số lượng tồn trong nguyenvatlieu
        $stmt_update = $conn->prepare(
            "UPDATE nguyenvatlieu SET so_luong_ton = so_luong_ton + ? WHERE id = ?"
        );
        
        foreach ($chitiet as $item) {
            $so_luong = (float)$item['so_luong_nhap'];
            $nguyenlieu_id = (int)$item['nguyenlieu_id'];
            
            $stmt_update->bind_param('di', $so_luong, $nguyenlieu_id);
            if (!$stmt_update->execute()) {
                throw new Exception('Không thể cập nhật số lượng tồn cho nguyên liệu ID ' . $nguyenlieu_id);
            }
        }
        
        $stmt_update->close();
        
        // 4. Update trạng thái phiếu nhập kho
        $stmt_phieu = $conn->prepare(
            "UPDATE nhapkho SET trang_thai = 'da_duyet' WHERE id = ?"
        );
        $stmt_phieu->bind_param('i', $id);
        
        if (!$stmt_phieu->execute()) {
            throw new Exception('Không thể cập nhật trạng thái phiếu nhập kho.');
        }
        
        $stmt_phieu->close();
        
        // Commit transaction
        $conn->commit();
        
        $message = 'Đã duyệt phiếu nhập kho thành công. Số lượng tồn kho đã được cập nhật.';
        if ($table_exists) {
            $message .= ' Các lô nguyên liệu đã được tạo.';
        }
        
        setFlashMessage('success', $message);
        redirect('view.php?id=' . $id);
        
    } catch (Exception $e) {
        $conn->rollback();
        throw $e;
    }
    
} catch (Exception $e) {
    error_log("Error in kho/approve: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra: ' . $e->getMessage());
    redirect('index.php');
}
?>

