<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $notice = '';
    
    // Get filter parameter (cần lấy trước khi xử lý POST để dùng trong redirect)
    $kw = get('kw');
    
    // Handle add menu category
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'add_thucdon') {
        $ten = post('ten_danhmuc');
        $mota = post('mota');
        $thu_tu = post('thu_tu', 0, 'int');
        
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên thực đơn';
            // Giữ form mở khi có lỗi
            $showAddForm = true;
        } else {
            try {
                global $conn;
                $cha = null;
                
                // Dùng prepared statement trực tiếp để INSERT
                $stmt = $conn->prepare(
                    "INSERT INTO danhmuc (ten_danhmuc, mota, danhmuc_cha, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, ?, 1)"
                );
                
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                
                $stmt->bind_param('ssii', $ten, $mota, $cha, $thu_tu);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Thêm thực đơn thành công!');
                    // Redirect về trang không có ?add để đóng form
                    // Giữ lại search nếu có
                    $redirectUrl = 'thucdon.php';
                    if (!empty($kw)) {
                        $redirectUrl .= '?kw=' . urlencode($kw);
                    }
                    redirect($redirectUrl);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Không thể thêm thực đơn. Vui lòng thử lại!';
                    $showAddForm = true;
                }
            } catch (Exception $e) {
                error_log("Error adding menu: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
                $showAddForm = true;
            }
        }
    }
    
    // Build query with prepared statements
    // Trang thực đơn chỉ hiển thị các danh mục KHÔNG phải nhóm lựa chọn
    $where = "(mota IS NULL OR mota != ? OR mota = '')";
    $params = ['nhóm lựa chọn'];
    $types = 's';
    
    if (!empty($kw)) {
        $where .= " AND ten_danhmuc LIKE ?";
        $params[] = '%' . $kw . '%';
        $types .= 's';
    }
    
    // Get categories (only menu categories, not choice groups)
    $sql = "SELECT * FROM danhmuc WHERE $where ORDER BY thu_tu";
    $ds = db_fetch_all($sql, $types, $params);
    
    // Đếm số sản phẩm cho mỗi danh mục
    foreach ($ds as &$item) {
        $productCount = db_fetch_one(
            "SELECT COUNT(*) as cnt FROM sanpham WHERE danhmuc_id = ?",
            'i',
            [$item['id']]
        );
        $item['so_sanpham'] = $productCount ? (int)$productCount['cnt'] : 0;
    }
    unset($item); // Unset reference
    
    // Hiển thị form thêm nếu có ?add trong URL hoặc nếu đang có lỗi từ POST
    if (!isset($showAddForm)) {
        $showAddForm = isset($_GET['add']);
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/thucdon', [
        'ds' => $ds,
        'kw' => $kw,
        'notice' => $notice,
        'showAddForm' => $showAddForm,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Thực đơn',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/thucdon: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang thực đơn.');
    redirect('../index.php');
}
?>
