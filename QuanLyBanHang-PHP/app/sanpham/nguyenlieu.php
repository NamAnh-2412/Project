<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $notice = '';
    
    // Get filter parameters (cần lấy trước khi xử lý POST để dùng trong redirect)
    $kw = get('kw');
    $filter = get('loai');
    
    // Handle add ingredient
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'add_nguyenlieu') {
        $ten = post('ten_nguyenlieu');
        $nhom = post('danhmuc_nl');
        $donvi = post('don_vi_tinh');
        $trangthai = post('trang_thai', 'con_hang');
        
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên nguyên liệu';
            // Giữ form mở khi có lỗi
            $showAddForm = true;
        } else {
            try {
                global $conn;
                $ma = 'NL' . date('ymdHis');
                
                // Dùng prepared statement trực tiếp để INSERT
                $stmt = $conn->prepare(
                    "INSERT INTO nguyenvatlieu (ma_nguyenlieu, ten_nguyenlieu, danhmuc_nl, don_vi_tinh, trang_thai) VALUES (?, ?, ?, ?, ?)"
                );
                
                if (!$stmt) {
                    throw new Exception("SQL Prepare Error: " . $conn->error);
                }
                
                $stmt->bind_param('sssss', $ma, $ten, $nhom, $donvi, $trangthai);
                
                if ($stmt->execute()) {
                    $stmt->close();
                    setFlashMessage('success', 'Thêm nguyên liệu thành công!');
                    // Redirect về trang không có ?add để đóng form
                    // Giữ lại filter và search nếu có
                    $redirectUrl = 'nguyenlieu.php';
                    $params = [];
                    if (!empty($kw)) {
                        $params[] = 'kw=' . urlencode($kw);
                    }
                    if (!empty($filter)) {
                        $params[] = 'loai=' . urlencode($filter);
                    }
                    if (!empty($params)) {
                        $redirectUrl .= '?' . implode('&', $params);
                    }
                    redirect($redirectUrl);
                } else {
                    $error = $stmt->error;
                    $stmt->close();
                    error_log("SQL Execute Error: " . $error);
                    $notice = 'Không thể thêm nguyên liệu. Vui lòng thử lại!';
                    $showAddForm = true;
                }
            } catch (Exception $e) {
                error_log("Error adding ingredient: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
                $showAddForm = true;
            }
        }
    }
    
    // Hiển thị form thêm nếu có ?add trong URL hoặc nếu đang có lỗi từ POST
    if (!isset($showAddForm)) {
        $showAddForm = isset($_GET['add']);
    }
    
    // Build query with prepared statements
    $where = "1=1";
    $params = [];
    $types = '';
    
    if (!empty($kw)) {
        $where .= " AND ten_nguyenlieu LIKE ?";
        $params[] = '%' . $kw . '%';
        $types .= 's';
    }
    
    if (!empty($filter)) {
        $where .= " AND danhmuc_nl = ?";
        $params[] = $filter;
        $types .= 's';
    }
    
    // Get ingredients
    $sql = "SELECT * FROM nguyenvatlieu WHERE $where ORDER BY danhmuc_nl, ten_nguyenlieu";
    $ds = db_fetch_all($sql, $types, $params);
    
    $danhmuc = ['tra' => 'Trà', 'mứt' => 'Mứt', 'thạch' => 'Thạch', 'sữa' => 'Sữa', 'đường' => 'Đường', 'topping' => 'Topping', 'ly_cốc' => 'Ly/cốc', 'khác' => 'Khác'];
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/nguyenlieu', [
        'ds' => $ds,
        'danhmuc' => $danhmuc,
        'kw' => $kw,
        'filter' => $filter,
        'notice' => $notice,
        'showAddForm' => $showAddForm,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Nguyên liệu',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/nguyenlieu: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang nguyên liệu.');
    redirect('../index.php');
}
?>
