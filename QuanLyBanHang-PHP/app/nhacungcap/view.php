<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

$id = get('id', 0, 'int');
if (!$id) {
    redirect('index.php');
}

try {
    // Get supplier info
    $supplier = db_fetch_one("SELECT * FROM nhacungcap WHERE id = ?", 'i', [$id]);
    if (!$supplier) {
        setFlashMessage('error', 'Không tìm thấy nhà cung cấp.');
        redirect('index.php');
    }
    
    // Get materials supplied by this supplier
    $materials = db_fetch_all(
        "SELECT id, ma_nguyenlieu, ten_nguyenlieu, don_vi_tinh, gia_nhap, trang_thai 
         FROM nguyenvatlieu 
         WHERE nha_cung_cap_id = ? 
         ORDER BY ten_nguyenlieu",
        'i',
        [$id]
    );
    
    // Get import history (phiếu nhập)
    $importHistory = db_fetch_all(
        "SELECT nk.id, nk.ma_phieu_nhap, nk.ngay_nhap, nk.tong_tien_nhap, nk.trang_thai,
                nv.ten_nhanvien
         FROM nhapkho nk
         LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id
         WHERE nk.nha_cung_cap_id = ?
         ORDER BY nk.ngay_nhap DESC
         LIMIT 50",
        'i',
        [$id]
    );
    
    // Statistics
    $stats = [
        'total_materials' => count($materials),
        'total_imports' => db_fetch_one(
            "SELECT COUNT(*) as count FROM nhapkho WHERE nha_cung_cap_id = ?",
            'i',
            [$id]
        )['count'] ?? 0,
        'total_value' => db_fetch_one(
            "SELECT SUM(tong_tien_nhap) as total FROM nhapkho WHERE nha_cung_cap_id = ? AND trang_thai = 'da_duyet'",
            'i',
            [$id]
        )['total'] ?? 0
    ];
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    renderView('nhacungcap/view', [
        'supplier' => $supplier,
        'materials' => $materials,
        'importHistory' => $importHistory,
        'stats' => $stats,
        'ten_nv' => $ten_nv,
        'first_char' => $first_char,
        'page_title' => 'Chi tiết nhà cung cấp',
        'page_css' => ['customers.css']
    ]);
} catch (Exception $e) {
    error_log("Error in nhacungcap/view: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải chi tiết nhà cung cấp.');
    redirect('index.php');
}
?>

