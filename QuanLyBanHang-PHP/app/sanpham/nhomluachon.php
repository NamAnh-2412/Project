<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $notice = '';
    
    // Clear flash messages cũ nếu trang load thành công (tránh hiển thị lỗi cũ)
    // Chỉ clear nếu không có lỗi thực sự
    $hasError = false;
    
    // Get filter parameter (cần lấy trước khi xử lý POST để dùng trong redirect)
    $kw = get('kw');
    
    // Handle add choice group
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'add_nhom') {
        $ten = post('ten_nhom');
        $mota = post('mota');
        $thu_tu = post('thu_tu', 0, 'int');
        
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên nhóm lựa chọn';
            // Giữ form mở khi có lỗi
            $showAddForm = true;
        } else {
            try {
                global $conn;
                
                // Dùng bảng nhom_lua_chon riêng biệt
                $stmt = $conn->prepare(
                    "INSERT INTO nhom_lua_chon (ten_nhom, mota, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, 1)"
                );
                
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                
                $stmt->bind_param('ssi', $ten, $mota, $thu_tu);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Thêm nhóm lựa chọn thành công!');
                    // Redirect về trang không có ?add để đóng form
                    // Giữ lại search nếu có
                    $redirectUrl = 'nhomluachon.php';
                    if (!empty($kw)) {
                        $redirectUrl .= '?kw=' . urlencode($kw);
                    }
                    redirect($redirectUrl);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Không thể thêm nhóm lựa chọn. Vui lòng thử lại!';
                    $showAddForm = true;
                }
            } catch (Exception $e) {
                error_log("Error adding choice group: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
                $showAddForm = true;
            }
        }
    }
    
    // Build query with prepared statements - Sử dụng bảng nhom_lua_chon riêng biệt
    $where = "1=1";
    $params = [];
    $types = '';
    
    if (!empty($kw)) {
        $where .= " AND ten_nhom LIKE ?";
        $params[] = '%' . $kw . '%';
        $types .= 's';
    }
    
    // Get choice groups from nhom_lua_chon table
    // Đếm số sản phẩm liên kết qua bảng trung gian sanpham_nhom_lua_chon
    try {
        $sql = "SELECT n.*, 
                       (SELECT COUNT(*) FROM lua_chon l WHERE l.nhom_lua_chon_id = n.id AND l.dang_hoat_dong = 1) as so_lua_chon,
                       (SELECT COUNT(DISTINCT snl.sanpham_id) 
                        FROM sanpham_nhom_lua_chon snl 
                        INNER JOIN sanpham s ON snl.sanpham_id = s.id 
                        WHERE snl.nhom_lua_chon_id = n.id AND s.dang_hoat_dong = 1) as so_sanpham
                FROM nhom_lua_chon n 
                WHERE $where AND n.dang_hoat_dong = 1 
                ORDER BY n.thu_tu, n.ten_nhom";
        $ds = db_fetch_all($sql, $types, $params);
        
        if ($ds === false) {
            throw new Exception("Không thể truy vấn dữ liệu nhóm lựa chọn. Vui lòng kiểm tra database.");
        }
    } catch (Exception $e) {
        error_log("Error querying nhom_lua_chon: " . $e->getMessage());
        throw $e;
    }
    
    // Lấy danh sách lựa chọn con cho mỗi nhóm
    foreach ($ds as &$item) {
        try {
            $lua_chon = db_fetch_all(
                "SELECT * FROM lua_chon WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1 ORDER BY thu_tu, ten_lua_chon",
                'i',
                [$item['id']]
            );
            $item['danh_sach_lua_chon'] = $lua_chon ? $lua_chon : [];
        } catch (Exception $e) {
            error_log("Error loading lua_chon for group {$item['id']}: " . $e->getMessage());
            $item['danh_sach_lua_chon'] = [];
        }
        // Đảm bảo so_sanpham có giá trị mặc định
        $item['so_sanpham'] = isset($item['so_sanpham']) ? (int)$item['so_sanpham'] : 0;
    }
    unset($item); // Unset reference
    
    // Hiển thị form thêm nếu có ?add trong URL hoặc nếu đang có lỗi từ POST
    if (!isset($showAddForm)) {
        $showAddForm = isset($_GET['add']);
    }
    
    // Nếu không có lỗi, chỉ clear flash messages lỗi cũ (giữ lại success messages)
    if (empty($notice) && isset($_SESSION['flash_messages'])) {
        $_SESSION['flash_messages'] = array_filter($_SESSION['flash_messages'], function($msg) {
            return $msg['type'] !== 'error';
        });
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/nhomluachon', [
        'ds' => $ds,
        'kw' => $kw,
        'notice' => $notice,
        'showAddForm' => $showAddForm,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Nhóm lựa chọn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/nhomluachon: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    
    // Kiểm tra xem có phải lỗi do bảng chưa tồn tại không
    $errorMsg = $e->getMessage();
    if (strpos($errorMsg, "doesn't exist") !== false || strpos($errorMsg, "Table") !== false) {
        $notice = 'Bảng database chưa được tạo. Vui lòng chạy file SQL migration trước.';
    } else {
        $notice = 'Có lỗi xảy ra: ' . htmlspecialchars($errorMsg);
    }
    
    // Không redirect, hiển thị lỗi trên trang
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/nhomluachon', [
        'ds' => [],
        'kw' => $kw ?? '',
        'notice' => $notice,
        'showAddForm' => false,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Nhóm lựa chọn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
    exit;
}
?>
