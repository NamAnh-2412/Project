<?php
// Load bootstrap first
require_once __DIR__ . '/../core/bootstrap.php';

// Initialize API environment
initApi();

// Require authentication
requireApiAuth();

try {
    $q = get('q', '', 'string');
    
    if (empty($q)) {
        jsonResponse([
            'success' => true,
            'customers' => []
        ]);
    }
    
    $searchTerm = '%' . $q . '%';
    
    $customers = db_fetch_all(
        "SELECT 
            id,
            ten_khachhang,
            so_dien_thoai,
            email,
            dia_chi,
            ngay_sinh,
            ghi_chu,
            loai_thanh_vien,
            tong_tien_da_chi,
            tong_donhang
        FROM khachhang
        WHERE ten_khachhang LIKE ? OR so_dien_thoai LIKE ?
        ORDER BY tong_tien_da_chi DESC, ten_khachhang ASC
        LIMIT 15",
        'ss',
        [$searchTerm, $searchTerm]
    );
    
    jsonResponse([
        'success' => true,
        'customers' => $customers ? $customers : []
    ]);
    
} catch (Exception $e) {
    error_log("Error in search_customer: " . $e->getMessage());
    jsonResponse(['success' => false, 'error' => 'Internal server error'], 500);
}
?>
