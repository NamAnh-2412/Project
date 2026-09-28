<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $notice = '';
    
    // Handle add product
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'add_sp') {
        $ten = post('ten_sanpham');
        $dm = post('danhmuc_id', 0, 'int');
        $gia = post('gia_ban', 0, 'float');
        $active = post('dang_hoat_dong', 1, 'int');
        $has_sizes = post('has_sizes', 0, 'int');
        
        // Parse sizes từ JSON
        $sizes_json = post('sizes', '[]', 'raw');
        $sizes = json_decode($sizes_json, true);
        if (!is_array($sizes)) {
            $sizes = [];
        }
        
        // Handle image upload
        $anh_url = null;
        if (isset($_FILES['anh_url']) && $_FILES['anh_url']['error'] === UPLOAD_ERR_OK) {
            $uploadResult = uploadImage('anh_url');
            if ($uploadResult['success']) {
                $anh_url = $uploadResult['path'];
            } else {
                $notice = $uploadResult['error'];
            }
        }
        
        if (empty($ten) || $dm <= 0) {
            $notice = 'Vui lòng nhập tên sản phẩm và chọn danh mục';
        } elseif ($gia < 0) {
            $notice = 'Giá bán không hợp lệ';
        } elseif ($has_sizes && empty($sizes)) {
            $notice = 'Vui lòng thêm ít nhất một size';
        } else {
            try {
                global $conn;
                $conn->begin_transaction();
                
                // 1. Insert sản phẩm
                $ma = 'SP' . date('ymdHis');
                if ($anh_url) {
                    $stmt = $conn->prepare(
                        "INSERT INTO sanpham (ma_sanpham, ten_sanpham, danhmuc_id, gia_ban, dang_hoat_dong, anh_url) VALUES (?, ?, ?, ?, ?, ?)"
                    );
                    if (!$stmt) {
                        throw new Exception("SQL Prepare Error: " . $conn->error);
                    }
                    $stmt->bind_param('ssidss', $ma, $ten, $dm, $gia, $active, $anh_url);
                } else {
                    $stmt = $conn->prepare(
                        "INSERT INTO sanpham (ma_sanpham, ten_sanpham, danhmuc_id, gia_ban, dang_hoat_dong) VALUES (?, ?, ?, ?, ?)"
                    );
                    if (!$stmt) {
                        throw new Exception("SQL Prepare Error: " . $conn->error);
                    }
                    $stmt->bind_param('ssidi', $ma, $ten, $dm, $gia, $active);
                }
                $stmt->execute();
                $productId = $conn->insert_id;
                $stmt->close();
                
                // 2. Insert sizes nếu có
                if ($has_sizes && !empty($sizes)) {
                    // Validate: không được trùng tên size trong cùng sản phẩm
                    $size_names = [];
                    foreach ($sizes as $size) {
                        $ten_size = trim($size['ten_size'] ?? '');
                        if (empty($ten_size)) {
                            continue;
                        }
                        if (in_array($ten_size, $size_names)) {
                            throw new Exception("Tên size '{$ten_size}' bị trùng. Vui lòng kiểm tra lại.");
                        }
                        $size_names[] = $ten_size;
                    }
                    
                    $stmt_size = $conn->prepare(
                        "INSERT INTO kichthuoc_sanpham (sanpham_id, ten_size, gia, dang_hoat_dong) VALUES (?, ?, ?, 1)"
                    );
                    if (!$stmt_size) {
                        throw new Exception("SQL Prepare Error (sizes): " . $conn->error);
                    }
                    
                    foreach ($sizes as $size) {
                        $ten_size = trim($size['ten_size'] ?? '');
                        $gia_size = (float)($size['gia'] ?? 0);
                        
                        if (!empty($ten_size) && $gia_size >= 0) {
                            $stmt_size->bind_param('isd', $productId, $ten_size, $gia_size);
                            if (!$stmt_size->execute()) {
                                throw new Exception("Error inserting size: " . $stmt_size->error);
                            }
                        }
                    }
                    $stmt_size->close();
                }
                
                $conn->commit();
                setFlashMessage('success', 'Thêm sản phẩm thành công!');
                redirect('index.php');
                
            } catch (Exception $e) {
                if (isset($conn) && $conn->in_transaction) {
                    $conn->rollback();
                }
                error_log("Error adding product: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra: ' . $e->getMessage();
            }
        }
    }
    
    // Get filter parameters
    $kw = get('kw');
    $cat = get('cat', '', 'int');
    
    // Build query with prepared statements
    $where = "1=1";
    $params = [];
    $types = '';
    
    if (!empty($kw)) {
        $where .= " AND ten_sanpham LIKE ?";
        $params[] = '%' . $kw . '%';
        $types .= 's';
    }
    
    if (!empty($cat)) {
        $where .= " AND danhmuc_id = ?";
        $params[] = $cat;
        $types .= 'i';
    }
    
    // Get products
    $sql = "SELECT * FROM sanpham WHERE $where ORDER BY ngay_tao DESC";
    $ds = db_fetch_all($sql, $types, $params);
    
    // Get categories (only menu categories, not choice groups)
    $danhmuc_raw = db_fetch_all(
        "SELECT * FROM danhmuc WHERE (mota IS NULL OR mota != ? OR mota = '')",
        's',
        ['nhóm lựa chọn']
    );
    $dm_arr = [];
    foreach ($danhmuc_raw as $dm) {
        $dm_arr[$dm['id']] = $dm['ten_danhmuc'];
    }
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/index', [
        'ds' => $ds,
        'dm_arr' => $dm_arr,
        'kw' => $kw,
        'cat' => $cat,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Sản phẩm',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang sản phẩm.');
    redirect('../index.php');
}
?>
