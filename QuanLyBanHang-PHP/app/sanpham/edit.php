<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    $id = get('id', 0, 'int');
    if (!$id) {
        setFlashMessage('error', 'ID sản phẩm không hợp lệ.');
        redirect('index.php');
    }
    
    // Get product data
    $product = db_fetch_one("SELECT * FROM sanpham WHERE id = ?", 'i', [$id]);
    if (!$product) {
        setFlashMessage('error', 'Không tìm thấy sản phẩm.');
        redirect('index.php');
    }
    
    $notice = '';
    
    // Handle form submission
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && post('action') === 'update_sp') {
        $ten = post('ten_sanpham');
        $dm = post('danhmuc_id', 0, 'int');
        $gia = post('gia_ban', 0, 'float');
        $active = post('dang_hoat_dong', 1, 'int');
        $mota = post('mota', '');
        
        // Handle image upload
        $anh_url = $product['anh_url'] ?? null; // Keep existing image
        if (isset($_FILES['anh_url']) && $_FILES['anh_url']['error'] === UPLOAD_ERR_OK) {
            // Delete old image if exists
            if ($anh_url) {
                deleteImage($anh_url);
            }
            
            $uploadResult = uploadImage('anh_url');
            if ($uploadResult['success']) {
                $anh_url = $uploadResult['path'];
            } else {
                $notice = $uploadResult['error'];
            }
        }
        
        // Validation
        if (empty($ten)) {
            $notice = 'Vui lòng nhập tên sản phẩm.';
        } elseif ($dm <= 0) {
            $notice = 'Vui lòng chọn danh mục.';
        } elseif ($gia < 0) {
            $notice = 'Giá bán không hợp lệ.';
        } else {
            try {
                if ($anh_url) {
                    $result = db_query(
                        "UPDATE sanpham SET ten_sanpham = ?, danhmuc_id = ?, gia_ban = ?, dang_hoat_dong = ?, mota = ?, anh_url = ? WHERE id = ?",
                        'sidsisi',
                        [$ten, $dm, $gia, $active, $mota, $anh_url, $id]
                    );
                } else {
                    $result = db_query(
                        "UPDATE sanpham SET ten_sanpham = ?, danhmuc_id = ?, gia_ban = ?, dang_hoat_dong = ?, mota = ? WHERE id = ?",
                        'sidsii',
                        [$ten, $dm, $gia, $active, $mota, $id]
                    );
                }
                
                if ($result) {
                    setFlashMessage('success', 'Cập nhật sản phẩm thành công!');
                    redirect('index.php');
                } else {
                    $notice = 'Không thể cập nhật sản phẩm. Vui lòng thử lại!';
                }
            } catch (Exception $e) {
                error_log("Error updating product: " . $e->getMessage());
                $notice = 'Có lỗi xảy ra. Vui lòng thử lại.';
            }
        }
        
        // Reload data if there was an error
        $product = db_fetch_one("SELECT * FROM sanpham WHERE id = ?", 'i', [$id]);
    }
    
    // Get categories (only menu categories, not choice groups)
    $danhmuc_raw = db_fetch_all(
        "SELECT * FROM danhmuc WHERE (mota IS NULL OR mota != ? OR mota = '') ORDER BY thu_tu, ten_danhmuc",
        's',
        ['nhóm lựa chọn']
    );
    $dm_arr = [];
    foreach ($danhmuc_raw as $dm) {
        $dm_arr[$dm['id']] = $dm;
    }
    
    // Get sizes for this product
    $sizes = db_fetch_all(
        "SELECT id, ten_size, gia, dang_hoat_dong FROM kichthuoc_sanpham WHERE sanpham_id = ? ORDER BY id",
        'i',
        [$id]
    );
    
    // Get linked choice groups for this product
    $linkedGroups = db_fetch_all(
        "SELECT n.id, n.ten_nhom, n.mota, snl.bat_buoc 
         FROM sanpham_nhom_lua_chon snl 
         INNER JOIN nhom_lua_chon n ON snl.nhom_lua_chon_id = n.id 
         WHERE snl.sanpham_id = ? AND n.dang_hoat_dong = 1 
         ORDER BY n.thu_tu, n.ten_nhom",
        'i',
        [$id]
    );
    
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('sanpham/edit', [
        'product' => $product,
        'dm_arr' => $dm_arr,
        'sizes' => $sizes,
        'linkedGroups' => $linkedGroups,
        'notice' => $notice,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Sửa sản phẩm',
        'page_css' => ['ui.css', 'products.css'],
        'body_class' => 'products-page',
        'show_header' => false
    ]);
} catch (Exception $e) {
    error_log("Error in sanpham/edit: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải trang sửa sản phẩm.');
    redirect('index.php');
}
?>

