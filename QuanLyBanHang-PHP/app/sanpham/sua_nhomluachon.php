<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('nhomluachon.php');
}

$notice = '';

try {
    // Get choice group data
    $row = db_fetch_one("SELECT * FROM nhom_lua_chon WHERE id = ?", 'i', [$id]);
    
    if (!$row) {
        setFlashMessage('error', 'Không tìm thấy nhóm lựa chọn.');
        redirect('nhomluachon.php');
    }
    
    // Handle add choice
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'add_choice') {
        $ten_lua_chon = post('ten_lua_chon');
        $gia_tang = post('gia_tang_them', 0, 'float');
        $thu_tu_choice = post('thu_tu_choice', 0, 'int');
        
        if (empty($ten_lua_chon)) {
            $notice = 'Vui lòng nhập tên lựa chọn.';
        } else {
            try {
                global $conn;
                $stmt = $conn->prepare(
                    "INSERT INTO lua_chon (nhom_lua_chon_id, ten_lua_chon, gia_tang_them, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, ?, 1)"
                );
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                $stmt->bind_param('isdi', $id, $ten_lua_chon, $gia_tang, $thu_tu_choice);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Thêm lựa chọn thành công!');
                    redirect('sua_nhomluachon.php?id=' . $id);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi thêm lựa chọn. Vui lòng thử lại.';
                }
            } catch (Exception $e) {
                error_log("Error adding choice: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
    }
    
    // Handle update choice
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'update_choice') {
        $choice_id = post('choice_id', 0, 'int');
        $ten_lua_chon = post('ten_lua_chon');
        $gia_tang = post('gia_tang_them', 0, 'float');
        $thu_tu_choice = post('thu_tu_choice', 0, 'int');
        
        if (empty($ten_lua_chon)) {
            $notice = 'Vui lòng nhập tên lựa chọn.';
        } elseif ($choice_id <= 0) {
            $notice = 'ID lựa chọn không hợp lệ.';
        } else {
            try {
                global $conn;
                $stmt = $conn->prepare(
                    "UPDATE lua_chon SET ten_lua_chon = ?, gia_tang_them = ?, thu_tu = ? WHERE id = ? AND nhom_lua_chon_id = ?"
                );
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                $stmt->bind_param('sdiii', $ten_lua_chon, $gia_tang, $thu_tu_choice, $choice_id, $id);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Cập nhật lựa chọn thành công!');
                    redirect('sua_nhomluachon.php?id=' . $id);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi cập nhật lựa chọn. Vui lòng thử lại.';
                }
            } catch (Exception $e) {
                error_log("Error updating choice: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
    }
    
    // Handle delete choice
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'delete_choice') {
        $choice_id = post('choice_id', 0, 'int');
        
        if ($choice_id <= 0) {
            $notice = 'ID lựa chọn không hợp lệ.';
        } else {
            try {
                global $conn;
                // Soft delete - set dang_hoat_dong = 0
                $stmt = $conn->prepare(
                    "UPDATE lua_chon SET dang_hoat_dong = 0 WHERE id = ? AND nhom_lua_chon_id = ?"
                );
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                $stmt->bind_param('ii', $choice_id, $id);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Xóa lựa chọn thành công!');
                    redirect('sua_nhomluachon.php?id=' . $id);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi xóa lựa chọn. Vui lòng thử lại.';
                }
            } catch (Exception $e) {
                error_log("Error deleting choice: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
    }
    
    // Handle form submission - Update group info
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'update_group') {
        $ten = post('ten_nhom');
        $mota = post('mota');
        $thu_tu = post('thu_tu', 0, 'int');
        
        // Validation
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên nhóm lựa chọn.';
        } else {
            try {
                global $conn;
                
                // Update choice group
                $stmt = $conn->prepare(
                    "UPDATE nhom_lua_chon SET ten_nhom = ?, mota = ?, thu_tu = ? WHERE id = ?"
                );
                
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                
                $stmt->bind_param('ssii', $ten, $mota, $thu_tu, $id);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Cập nhật nhóm lựa chọn thành công!');
                    redirect('sua_nhomluachon.php?id=' . $id);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Lỗi khi cập nhật nhóm lựa chọn. Vui lòng thử lại.';
                }
            } catch (Exception $e) {
                error_log("Error updating choice group: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
        
        // Reload data if there was an error
        $row = db_fetch_one("SELECT * FROM nhom_lua_chon WHERE id = ?", 'i', [$id]);
    }
    
    // Load choices for this group
    $choices = db_fetch_all(
        "SELECT * FROM lua_chon WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1 ORDER BY thu_tu, ten_lua_chon",
        'i',
        [$id]
    );
    if (!$choices) {
        $choices = [];
    }
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/sua_nhomluachon', [
        'row' => $row,
        'choices' => $choices,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Sửa nhóm lựa chọn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/sua_nhomluachon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra.');
    redirect('nhomluachon.php');
}
?>

