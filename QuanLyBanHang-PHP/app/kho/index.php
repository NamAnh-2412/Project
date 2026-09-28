<?php
require_once '../core/bootstrap.php';

// Require authentication
requireLogin();

try {
    // Get tab parameter (phieu or expiry)
    $tab = get('tab', 'phieu'); // phieu: Phiếu nhập kho, expiry: Hạn sử dụng
    
    // Get current user info
    $currentUser = getCurrentUser();
    $ten_nv = $currentUser['username'] ?? 'Admin';
    $first_char = strtoupper(substr($ten_nv, 0, 1));
    
    // Check if kho_lot table exists
    global $conn;
    $table_exists = false;
    $result = $conn->query("SHOW TABLES LIKE 'kho_lot'");
    if ($result && $result->num_rows > 0) {
        $table_exists = true;
    }
    
    if ($tab === 'expiry') {
        // Tab: Hạn sử dụng
        if (!$table_exists) {
            // Nếu chưa có bảng kho_lot, hiển thị thông báo
            renderView('kho/index', [
                'tab' => 'expiry',
                'list' => [],
                'expiry_list' => [],
                'stats' => [
                    'cho_duyet' => 0,
                    'da_duyet' => 0,
                    'da_huy' => 0,
                    'tong_tien' => 0
                ],
                'expiry_stats' => [
                    'sap_het_han' => 0,
                    'het_han' => 0,
                    'da_huy' => 0
                ],
                'status' => 'all',
                'from' => '',
                'to' => '',
                'expiry_filter' => 'all',
                'ten_nv' => $ten_nv,
                'first_char' => $first_char,
                'page_title' => 'Quản lý nhập kho',
                'page_css' => ['inventory.css'],
                'table_exists' => false
            ]);
            exit;
        }
        
        // Get filter parameters for expiry
        $expiry_filter = get('expiry_filter', 'all'); // all, sap_het_han, het_han, da_huy
        $from = get('from');
        $to = get('to');
        
        $today = date('Y-m-d');
        $seven_days_later = date('Y-m-d', strtotime('+7 days'));
        
        // Build query for expiry
        $where = "kl.trang_thai != 'da_huy'"; // Chỉ lấy các lô chưa hủy
        $params = [];
        $types = '';
        
        if ($expiry_filter === 'sap_het_han') {
            // Sắp hết hạn: còn 7 ngày
            $where .= " AND kl.han_su_dung >= ? AND kl.han_su_dung <= ?";
            $params[] = $today;
            $params[] = $seven_days_later;
            $types .= 'ss';
        } elseif ($expiry_filter === 'het_han') {
            // Đã hết hạn
            $where .= " AND kl.han_su_dung < ?";
            $params[] = $today;
            $types .= 's';
        } elseif ($expiry_filter === 'da_huy') {
            // Đã hủy
            $where = "kl.trang_thai = 'da_huy'";
        }
        
        if (!empty($from)) {
            $where .= " AND kl.han_su_dung >= ?";
            $params[] = $from;
            $types .= 's';
        }
        
        if (!empty($to)) {
            $where .= " AND kl.han_su_dung <= ?";
            $params[] = $to;
            $types .= 's';
        }
        
        // Get expiry list
        $sql = "SELECT 
                    kl.*,
                    nl.ten_nguyenlieu,
                    nl.don_vi_tinh,
                    nl.ma_nguyenlieu,
                    DATEDIFF(kl.han_su_dung, CURDATE()) as so_ngay_con_lai,
                    nk.ma_phieu_nhap,
                    nv.ten_nhanvien as nhanvien_nhap
                FROM kho_lot kl
                INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id
                LEFT JOIN nhapkho nk ON kl.nhapkho_id = nk.id
                LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id
                WHERE $where
                ORDER BY kl.han_su_dung ASC, nl.ten_nguyenlieu ASC
                LIMIT 500";
        
        $expiry_list = db_fetch_all($sql, $types, $params);
        
        // Get expiry statistics
        $expiry_stats = [
            'sap_het_han' => db_fetch_one(
                "SELECT COUNT(*) as count FROM kho_lot kl 
                 WHERE kl.trang_thai != 'da_huy' 
                 AND kl.han_su_dung >= CURDATE() 
                 AND kl.han_su_dung <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)"
            )['count'] ?? 0,
            'het_han' => db_fetch_one(
                "SELECT COUNT(*) as count FROM kho_lot kl 
                 WHERE kl.trang_thai != 'da_huy' 
                 AND kl.han_su_dung < CURDATE()"
            )['count'] ?? 0,
            'da_huy' => db_fetch_one(
                "SELECT COUNT(*) as count FROM kho_lot kl 
                 WHERE kl.trang_thai = 'da_huy'"
            )['count'] ?? 0
        ];
        
        // Get phiếu nhập kho stats (for tab switch)
        $stats = [
            'cho_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'cho_duyet'")['count'] ?? 0,
            'da_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'da_duyet'")['count'] ?? 0,
            'da_huy' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'da_huy'")['count'] ?? 0,
            'tong_tien' => db_fetch_one("SELECT SUM(tong_tien_nhap) as total FROM nhapkho WHERE trang_thai = 'da_duyet'")['total'] ?? 0
        ];
        
        renderView('kho/index', [
            'tab' => 'expiry',
            'list' => [],
            'expiry_list' => $expiry_list,
            'stats' => $stats,
            'expiry_stats' => $expiry_stats,
            'status' => 'all',
            'from' => $from,
            'to' => $to,
            'expiry_filter' => $expiry_filter,
            'ten_nv' => $ten_nv,
            'first_char' => $first_char,
            'page_title' => 'Quản lý nhập kho',
            'page_css' => ['inventory.css'],
            'table_exists' => true
        ]);
        
    } elseif ($tab === 'tonkho') {
        // Tab: Tồn kho
        $kw = get('kw');
        $filter = get('loai');
        
        $where = "IFNULL(so_luong_ton, 0) > 0";
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
        
        $sql = "SELECT * FROM nguyenvatlieu WHERE $where ORDER BY danhmuc_nl, ten_nguyenlieu";
        $tonkho_list = db_fetch_all($sql, $types, $params);
        $danhmuc = ['tra' => 'Trà', 'mứt' => 'Mứt', 'thạch' => 'Thạch', 'sữa' => 'Sữa', 'đường' => 'Đường', 'topping' => 'Topping', 'ly_cốc' => 'Ly/cốc', 'khác' => 'Khác'];
        
        // Đếm số lượng nguyên liệu sắp hết (dưới 10 và dưới 5)
        $low_stock_10 = 0;
        $low_stock_5 = 0;
        foreach ($tonkho_list as $item) {
            if (isset($item['so_luong_ton'])) {
                $sl = (float)$item['so_luong_ton'];
                if ($sl < 5) {
                    $low_stock_5++;
                } elseif ($sl < 10) {
                    $low_stock_10++;
                }
            }
        }
        
        // Lấy stats của hạn sử dụng (để hiển thị bubble đếm số ở tab)
        $expiry_stats = [
            'sap_het_han' => 0,
            'het_han' => 0,
            'da_huy' => 0
        ];
        if ($table_exists) {
            $expiry_stats = [
                'sap_het_han' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai != 'da_huy' 
                     AND kl.han_su_dung >= CURDATE() 
                     AND kl.han_su_dung <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)"
                )['count'] ?? 0,
                'het_han' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai != 'da_huy' 
                     AND kl.han_su_dung < CURDATE()"
                )['count'] ?? 0,
                'da_huy' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai = 'da_huy'"
                )['count'] ?? 0
            ];
        }

        renderView('kho/index', [
            'tab' => 'tonkho',
            'list' => [],
            'expiry_list' => [],
            'tonkho_list' => $tonkho_list,
            'danhmuc' => $danhmuc,
            'kw' => $kw,
            'filter' => $filter,
            'low_stock_5' => $low_stock_5,
            'low_stock_10' => $low_stock_10,
            'stats' => ['cho_duyet' => 0, 'da_duyet' => 0, 'da_huy' => 0, 'tong_tien' => 0],
            'expiry_stats' => $expiry_stats,
            'status' => 'all',
            'from' => '',
            'to' => '',
            'expiry_filter' => 'all',
            'ten_nv' => $ten_nv,
            'first_char' => $first_char,
            'page_title' => 'Quản lý Tồn kho',
            'page_css' => ['inventory.css'],
            'table_exists' => $table_exists
        ]);
        
    } elseif ($tab === 'kiemkho') {
        // Tab: Phiếu kiểm kho
        $status = get('status', 'all');
        $from = get('from');
        $to = get('to');
        
        $where = "1=1";
        $params = [];
        $types = '';
        
        if ($status !== 'all') {
            $where .= " AND trang_thai = ?";
            $params[] = $status;
            $types .= 's';
        }
        
        if (!empty($from)) {
            $where .= " AND DATE(ngay_kiem) >= ?";
            $params[] = $from;
            $types .= 's';
        }
        
        if (!empty($to)) {
            $where .= " AND DATE(ngay_kiem) <= ?";
            $params[] = $to;
            $types .= 's';
        }
        
        $sql = "SELECT kk.*, nv.ten_nhanvien 
                FROM kiemkho kk 
                LEFT JOIN nhanvien nv ON kk.nhanvien_kiem = nv.id 
                WHERE $where 
                ORDER BY kk.ngay_tao DESC LIMIT 200";
        $kiemkho_list = db_fetch_all($sql, $types, $params);
        
        $stats = [
            'cho_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM kiemkho WHERE trang_thai = 'cho_duyet'")['count'] ?? 0,
            'da_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM kiemkho WHERE trang_thai = 'da_duyet'")['count'] ?? 0,
            'da_huy' => db_fetch_one("SELECT COUNT(*) as count FROM kiemkho WHERE trang_thai = 'da_huy'")['count'] ?? 0
        ];
        
        renderView('kho/index', [
            'tab' => 'kiemkho',
            'list' => [],
            'expiry_list' => [],
            'tonkho_list' => [],
            'kiemkho_list' => $kiemkho_list,
            'stats' => $stats,
            'expiry_stats' => ['sap_het_han' => 0, 'het_han' => 0, 'da_huy' => 0],
            'status' => $status,
            'from' => $from,
            'to' => $to,
            'expiry_filter' => 'all',
            'ten_nv' => $ten_nv,
            'first_char' => $first_char,
            'page_title' => 'Phiếu Kiểm Kho',
            'page_css' => ['inventory.css'],
            'table_exists' => $table_exists
        ]);
        
    } else {
        // Tab: Phiếu nhập kho (default)
        // Get filter parameters
        $status = get('status', 'all'); // all, cho_duyet, da_duyet, da_huy
        $from = get('from');
        $to = get('to');
        
        // Build query for phiếu nhập kho
        $where = "1=1";
        $params = [];
        $types = '';
        
        if ($status !== 'all') {
            $where .= " AND trang_thai = ?";
            $params[] = $status;
            $types .= 's';
        }
        
        if (!empty($from)) {
            $where .= " AND DATE(ngay_nhap) >= ?";
            $params[] = $from;
            $types .= 's';
        }
        
        if (!empty($to)) {
            $where .= " AND DATE(ngay_nhap) <= ?";
            $params[] = $to;
            $types .= 's';
        }
        
        // Get list of phiếu nhập kho with related info
        $sql = "SELECT 
                    nk.*,
                    nv.ten_nhanvien,
                    ncc.ten_nhacungcap,
                    (SELECT COUNT(*) FROM chitiet_nhapkho WHERE nhapkho_id = nk.id) as so_mat_hang
                FROM nhapkho nk
                LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id
                LEFT JOIN nhacungcap ncc ON nk.nha_cung_cap_id = ncc.id
                WHERE $where
                ORDER BY nk.ngay_tao DESC
                LIMIT 200";
        
        $list = db_fetch_all($sql, $types, $params);
        
        // Get statistics
        $stats = [
            'cho_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'cho_duyet'")['count'] ?? 0,
            'da_duyet' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'da_duyet'")['count'] ?? 0,
            'da_huy' => db_fetch_one("SELECT COUNT(*) as count FROM nhapkho WHERE trang_thai = 'da_huy'")['count'] ?? 0,
            'tong_tien' => db_fetch_one("SELECT SUM(tong_tien_nhap) as total FROM nhapkho WHERE trang_thai = 'da_duyet'")['total'] ?? 0
        ];
        
        // Get expiry stats (for tab switch)
        $expiry_stats = [
            'sap_het_han' => 0,
            'het_han' => 0,
            'da_huy' => 0
        ];
        if ($table_exists) {
            $expiry_stats = [
                'sap_het_han' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai != 'da_huy' 
                     AND kl.han_su_dung >= CURDATE() 
                     AND kl.han_su_dung <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)"
                )['count'] ?? 0,
                'het_han' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai != 'da_huy' 
                     AND kl.han_su_dung < CURDATE()"
                )['count'] ?? 0,
                'da_huy' => db_fetch_one(
                    "SELECT COUNT(*) as count FROM kho_lot kl 
                     WHERE kl.trang_thai = 'da_huy'"
                )['count'] ?? 0
            ];
        }
        
        renderView('kho/index', [
            'tab' => 'phieu',
            'list' => $list,
            'expiry_list' => [],
            'stats' => $stats,
            'expiry_stats' => $expiry_stats,
            'status' => $status,
            'from' => $from,
            'to' => $to,
            'expiry_filter' => 'all',
            'ten_nv' => $ten_nv,
            'first_char' => $first_char,
            'page_title' => 'Quản lý nhập kho',
            'page_css' => ['inventory.css'],
            'table_exists' => $table_exists
        ]);
    }
    
} catch (Exception $e) {
    error_log("Error in kho/index: " . $e->getMessage());
    setFlashMessage('error', 'Có lỗi xảy ra khi tải danh sách nhập kho.');
    redirect('../index.php');
}
?>
