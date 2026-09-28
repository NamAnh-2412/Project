<?php
// Disable output buffering and ensure clean output
if (ob_get_level()) {
    ob_end_clean();
}
ob_start();

require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

// Clear any output that might have been generated
ob_clean();

header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    ob_clean();
    jsonResponse(['success' => false, 'error' => 'Phải POST'], 405);
}

// Get optional data from POST body
$body = json_decode(file_get_contents('php://input'), true);
$nha_cung_cap_id = isset($body['nha_cung_cap_id']) && !empty($body['nha_cung_cap_id']) ? (int)$body['nha_cung_cap_id'] : null;
$ghi_chu = isset($body['ghi_chu']) && !empty($body['ghi_chu']) ? sanitize($body['ghi_chu']) : null;

if (empty($_SESSION['kho_cart'])) {
    ob_clean();
    jsonResponse(['success' => false, 'error' => 'Giỏ hàng rỗng'], 400);
}

$cart = $_SESSION['kho_cart'];

try {
    global $conn;
    
    // Get current user ID
    $nhanvien_id = getCurrentUserId();
    if (!$nhanvien_id) {
        throw new Exception('Không xác định được nhân viên');
    }
    
    // Start transaction
    $conn->begin_transaction();
    
    // 1. Tạo phiếu nhập kho (nhapkho)
    $ma_phieu_nhap = 'PN' . date('ymdHis');
    $ngay_nhap = date('Y-m-d');
    
    // Tính tổng tiền nhập (nếu có giá nhập trong cart)
    $tong_tien_nhap = 0.00;
    foreach ($cart as $item) {
        $don_gia = isset($item['don_gia']) ? (float)$item['don_gia'] : 0;
        $qty = (float)($item['qty'] ?? 0);
        $tong_tien_nhap += $don_gia * $qty;
    }
    
    // Insert vào bảng nhapkho
    // Handle NULL nha_cung_cap_id and ghi_chu properly
    // Build SQL and bind_param based on which fields are NULL
    $sql_fields = ['ma_phieu_nhap', 'ngay_nhap'];
    $sql_values = ['?', '?'];
    $bind_types = 'ss';
    $bind_params = [&$ma_phieu_nhap, &$ngay_nhap];
    
    if ($nha_cung_cap_id !== null) {
        $sql_fields[] = 'nha_cung_cap_id';
        $sql_values[] = '?';
        $bind_types .= 'i';
        $bind_params[] = &$nha_cung_cap_id;
    }
    
    $sql_fields[] = 'tong_tien_nhap';
    $sql_values[] = '?';
    $bind_types .= 'd';
    $bind_params[] = &$tong_tien_nhap;
    
    $sql_fields[] = 'nhanvien_nhap';
    $sql_values[] = '?';
    $bind_types .= 'i';
    $bind_params[] = &$nhanvien_id;
    
    $sql_fields[] = 'trang_thai';
    $sql_values[] = "'cho_duyet'";
    
    if ($ghi_chu !== null) {
        $sql_fields[] = 'ghi_chu';
        $sql_values[] = '?';
        $bind_types .= 's';
        $bind_params[] = &$ghi_chu;
    }
    
    $sql = "INSERT INTO nhapkho (" . implode(', ', $sql_fields) . ") VALUES (" . implode(', ', $sql_values) . ")";
    
    $stmt_nhapkho = $conn->prepare($sql);
    if (!$stmt_nhapkho) {
        throw new Exception('Prepare failed: ' . $conn->error);
    }
    
    // Use call_user_func_array to bind parameters dynamically
    call_user_func_array([$stmt_nhapkho, 'bind_param'], array_merge([$bind_types], $bind_params));
    
    if (!$stmt_nhapkho->execute()) {
        $error = $stmt_nhapkho->error;
        $stmt_nhapkho->close();
        throw new Exception('Không thể tạo phiếu nhập kho: ' . $error);
    }
    
    $nhapkho_id = $conn->insert_id;
    $stmt_nhapkho->close();
    
    // 2. Thêm chi tiết phiếu nhập kho (chitiet_nhapkho)
    $updated = [];
    
    foreach ($cart as $item) {
        $nguyenlieu_id = (int)($item['id'] ?? 0);
        $so_luong_nhap = (float)($item['qty'] ?? 0);
        $don_gia_nhap = isset($item['don_gia']) ? (float)$item['don_gia'] : 0.00;
        $thanh_tien = $don_gia_nhap * $so_luong_nhap;
        $han_su_dung = isset($item['han_su_dung']) && !empty($item['han_su_dung']) ? $item['han_su_dung'] : null;
        
        if ($nguyenlieu_id <= 0 || $so_luong_nhap <= 0) {
            throw new Exception('Dữ liệu không hợp lệ cho mặt hàng ID ' . $nguyenlieu_id . '. Số lượng: ' . $so_luong_nhap);
        }
        
        // Handle NULL han_su_dung by using separate SQL statements
        if ($han_su_dung === null) {
            $stmt_chitiet = $conn->prepare(
                "INSERT INTO chitiet_nhapkho (nhapkho_id, nguyenlieu_id, so_luong_nhap, don_gia_nhap, thanh_tien) 
                 VALUES (?, ?, ?, ?, ?)"
            );
            if (!$stmt_chitiet) {
                throw new Exception('Prepare chitiet failed: ' . $conn->error);
            }
            // bind_param: i=integer, d=double
            // Parameters: nhapkho_id(i), nguyenlieu_id(i), so_luong_nhap(d), don_gia_nhap(d), thanh_tien(d)
            $stmt_chitiet->bind_param('iiddd', 
                $nhapkho_id, 
                $nguyenlieu_id, 
                $so_luong_nhap, 
                $don_gia_nhap, 
                $thanh_tien
            );
        } else {
            $stmt_chitiet = $conn->prepare(
                "INSERT INTO chitiet_nhapkho (nhapkho_id, nguyenlieu_id, so_luong_nhap, don_gia_nhap, thanh_tien, han_su_dung) 
                 VALUES (?, ?, ?, ?, ?, ?)"
            );
            if (!$stmt_chitiet) {
                throw new Exception('Prepare chitiet failed: ' . $conn->error);
            }
            // bind_param: i=integer, d=double, s=string
            // Parameters: nhapkho_id(i), nguyenlieu_id(i), so_luong_nhap(d), don_gia_nhap(d), thanh_tien(d), han_su_dung(s)
            $stmt_chitiet->bind_param('iiddds', 
                $nhapkho_id, 
                $nguyenlieu_id, 
                $so_luong_nhap, 
                $don_gia_nhap, 
                $thanh_tien, 
                $han_su_dung
            );
        }
        
        if (!$stmt_chitiet->execute()) {
            $error = $stmt_chitiet->error;
            $stmt_chitiet->close();
            throw new Exception('Không thể thêm chi tiết phiếu nhập kho cho nguyên liệu ID ' . $nguyenlieu_id . ': ' . $error);
        }
        
        $stmt_chitiet->close();
        
        $updated[] = [
            'id' => $nguyenlieu_id, 
            'qty' => $so_luong_nhap,
            'don_gia' => $don_gia_nhap
        ];
    }
    
    // 3. Ghi lịch sử nhập kho (lich_su_nhap)
    try {
        $stmt_lichsu = $conn->prepare(
            "INSERT INTO lich_su_nhap (ngay, nhanvien_id, nguyenlieu_id, so_luong) 
             VALUES (NOW(), ?, ?, ?)"
        );
        
        if ($stmt_lichsu) {
            foreach ($cart as $item) {
                // Must use variables, not direct values for bind_param
                $nl_id = (int)$item['id'];
                $nl_qty = (int)$item['qty'];
                
                $stmt_lichsu->bind_param('iii', 
                    $nhanvien_id, 
                    $nl_id, 
                    $nl_qty
                );
                $stmt_lichsu->execute();
            }
            
            $stmt_lichsu->close();
        }
    } catch (Exception $e) {
        // History table might not exist, just log and continue
        error_log("Error logging inventory history: " . $e->getMessage());
    }
    
    // Commit transaction
    $conn->commit();
    
    // Clear cart
    $_SESSION['kho_cart'] = [];
    
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse([
        'success' => true, 
        'nhapkho_id' => $nhapkho_id,
        'ma_phieu_nhap' => $ma_phieu_nhap,
        'updated' => $updated,
        'message' => 'Đã tạo phiếu nhập kho thành công. Phiếu đang ở trạng thái chờ duyệt.'
    ]);
    
} catch (Exception $e) {
    global $conn;
    if (isset($conn)) {
        try {
            if (method_exists($conn, 'in_transaction') && $conn->in_transaction) {
                $conn->rollback();
            } elseif (method_exists($conn, 'get_server_info')) {
                // Try to rollback anyway
                @$conn->rollback();
            }
        } catch (Exception $rollbackEx) {
            error_log("Rollback error: " . $rollbackEx->getMessage());
        }
    }
    error_log("Error in kho/commit: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    error_log("Cart data: " . json_encode($cart));
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse(['success' => false, 'error' => $e->getMessage()], 500);
} catch (Error $e) {
    global $conn;
    if (isset($conn)) {
        try {
            if (method_exists($conn, 'in_transaction') && $conn->in_transaction) {
                $conn->rollback();
            } elseif (method_exists($conn, 'get_server_info')) {
                // Try to rollback anyway
                @$conn->rollback();
            }
        } catch (Exception $rollbackEx) {
            error_log("Rollback error: " . $rollbackEx->getMessage());
        }
    }
    error_log("Fatal error in kho/commit: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    // Clear output buffer before sending JSON
    ob_clean();
    jsonResponse(['success' => false, 'error' => 'Lỗi hệ thống: ' . $e->getMessage()], 500);
}
?>

