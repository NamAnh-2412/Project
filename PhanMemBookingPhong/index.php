<?php
require_once 'config/database.php';
require_once 'config/config.php';

// Lấy filter status
$trang_thai_filter = $_GET['trang_thai'] ?? 'all';
$search_query = $_GET['search'] ?? '';

// Lấy các filter mới
$dien_tich_min = $_GET['dien_tich_min'] ?? '';
$dien_tich_max = $_GET['dien_tich_max'] ?? '';
$gia_min = $_GET['gia_min'] ?? '';
$gia_max = $_GET['gia_max'] ?? '';
$dia_chi_filter = $_GET['dia_chi'] ?? '';

$pdo = getDBConnection();

// Xây dựng query
$where_conditions = [];
$params = [];

if ($trang_thai_filter !== 'all') {
    $where_conditions[] = "p.trang_thai = ?";
    $params[] = $trang_thai_filter;
}

if (!empty($search_query)) {
    $where_conditions[] = "(p.tieu_de LIKE ? OR p.dia_chi LIKE ? OR p.mo_ta LIKE ?)";
    $search_param = "%{$search_query}%";
    $params[] = $search_param;
    $params[] = $search_param;
    $params[] = $search_param;
}

// Filter theo diện tích
if (!empty($dien_tich_min) && is_numeric($dien_tich_min)) {
    $where_conditions[] = "p.dien_tich >= ?";
    $params[] = $dien_tich_min;
}

if (!empty($dien_tich_max) && is_numeric($dien_tich_max)) {
    $where_conditions[] = "p.dien_tich <= ?";
    $params[] = $dien_tich_max;
}

// Filter theo giá tiền
if (!empty($gia_min) && is_numeric($gia_min)) {
    $where_conditions[] = "p.gia >= ?";
    $params[] = $gia_min;
}

if (!empty($gia_max) && is_numeric($gia_max)) {
    $where_conditions[] = "p.gia <= ?";
    $params[] = $gia_max;
}

// Filter theo địa chỉ
if (!empty($dia_chi_filter)) {
    $where_conditions[] = "(p.dia_chi LIKE ? OR p.quan_huyen LIKE ?)";
    $dia_chi_param = "%{$dia_chi_filter}%";
    $params[] = $dia_chi_param;
    $params[] = $dia_chi_param;
}

$where_clause = !empty($where_conditions) ? "WHERE " . implode(" AND ", $where_conditions) : "";

// Lấy danh sách phòng với hình ảnh chính
$sql = "SELECT p.*, 
               nd.ho_ten as chu_nha_ten,
               " . getAnhChinhSubquery() . ",
               " . getAnhDauTienSubquery() . "
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        $where_clause
        ORDER BY p.id DESC, p.ngay_tao DESC";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$rooms = $stmt->fetchAll();

// Đếm số phòng theo trạng thái
$trang_thai_counts = [
    'all' => 0,
    'con_trong' => 0,
    'da_thue' => 0
];

$count_sql = "SELECT trang_thai, COUNT(*) as count FROM phong GROUP BY trang_thai";
$count_stmt = $pdo->query($count_sql);
while ($row = $count_stmt->fetch()) {
    $trang_thai_counts[$row['trang_thai']] = $row['count'];
}
$trang_thai_counts['all'] = array_sum($trang_thai_counts);

// Kiểm tra yêu thích cho user đã đăng nhập (trừ phòng của chính mình)
$favorite_rooms = [];
if (isLoggedIn() && isset($_SESSION['user_id'])) {
    $user_id = $_SESSION['user_id'];
    $fav_stmt = $pdo->prepare("SELECT yt.phong_id FROM yeu_thich yt 
                                INNER JOIN phong p ON yt.phong_id = p.id 
                                WHERE yt.nguoi_dung_id = ? AND p.nguoi_dung_id != ?");
    $fav_stmt->execute([$user_id, $user_id]);
    $favorite_rooms = array_column($fav_stmt->fetchAll(), 'phong_id');
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách phòng trọ - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
    <?php include 'includes/header.php'; ?>
    
    <div class="container">
        <h1>Danh sách phòng trọ</h1>
        
        <!-- Layout 2 cột: Sidebar filter + Danh sách phòng -->
        <div class="rooms-layout">
            <!-- Sidebar Filter (Bên trái) -->
            <aside class="filter-sidebar">
                <div class="filter-sidebar-content">
                    <div class="filter-header">
                        <h3>Chọn lọc theo:</h3>
                        <?php if (!empty($search_query) || !empty($dien_tich_min) || !empty($dien_tich_max) || !empty($gia_min) || !empty($gia_max) || !empty($dia_chi_filter) || $trang_thai_filter !== 'all'): ?>
                            <a href="index.php" class="btn-reset-filter">Xóa bộ lọc</a>
                        <?php endif; ?>
                    </div>
                    
                    <form method="GET" action="" class="filter-form">
                        <!-- Tìm kiếm -->
                        <div class="filter-block">
                            <label class="filter-block-title">🔍 Tìm kiếm</label>
                            <input type="text" name="search" placeholder="Tên phòng, mô tả..." value="<?php echo htmlspecialchars($search_query); ?>" class="filter-input">
                        </div>
                        
                        <!-- Trạng thái phòng -->
                        <div class="filter-block">
                            <label class="filter-block-title">Trạng thái phòng</label>
                            <div class="filter-options">
                                <?php
                                function buildFilterUrl($trang_thai, $params) {
                                    $url = "?trang_thai=" . $trang_thai;
                                    if (!empty($params['search'])) $url .= "&search=" . urlencode($params['search']);
                                    if (!empty($params['dien_tich_min'])) $url .= "&dien_tich_min=" . urlencode($params['dien_tich_min']);
                                    if (!empty($params['dien_tich_max'])) $url .= "&dien_tich_max=" . urlencode($params['dien_tich_max']);
                                    if (!empty($params['gia_min'])) $url .= "&gia_min=" . urlencode($params['gia_min']);
                                    if (!empty($params['gia_max'])) $url .= "&gia_max=" . urlencode($params['gia_max']);
                                    if (!empty($params['dia_chi'])) $url .= "&dia_chi=" . urlencode($params['dia_chi']);
                                    return $url;
                                }
                                $filter_params = [
                                    'search' => $search_query,
                                    'dien_tich_min' => $dien_tich_min,
                                    'dien_tich_max' => $dien_tich_max,
                                    'gia_min' => $gia_min,
                                    'gia_max' => $gia_max,
                                    'dia_chi' => $dia_chi_filter
                                ];
                                ?>
                                <label class="filter-option">
                                    <input type="radio" name="trang_thai" value="all" <?php echo $trang_thai_filter === 'all' ? 'checked' : ''; ?> onchange="this.form.submit()">
                                    <span>Tất cả (<?php echo $trang_thai_counts['all']; ?>)</span>
                                </label>
                                <label class="filter-option">
                                    <input type="radio" name="trang_thai" value="con_trong" <?php echo $trang_thai_filter === 'con_trong' ? 'checked' : ''; ?> onchange="this.form.submit()">
                                    <span>Còn trống (<?php echo $trang_thai_counts['con_trong']; ?>)</span>
                                </label>
                                <label class="filter-option">
                                    <input type="radio" name="trang_thai" value="da_thue" <?php echo $trang_thai_filter === 'da_thue' ? 'checked' : ''; ?> onchange="this.form.submit()">
                                    <span>Đã thuê (<?php echo $trang_thai_counts['da_thue']; ?>)</span>
                                </label>
                            </div>
                        </div>
                        
                        <!-- Diện tích -->
                        <div class="filter-block">
                            <label class="filter-block-title">📐 Diện tích (m²)</label>
                            <div class="filter-range">
                                <input type="number" name="dien_tich_min" placeholder="Từ" value="<?php echo htmlspecialchars($dien_tich_min); ?>" step="0.01" min="0" class="filter-input">
                                <span class="range-separator">-</span>
                                <input type="number" name="dien_tich_max" placeholder="Đến" value="<?php echo htmlspecialchars($dien_tich_max); ?>" step="0.01" min="0" class="filter-input">
                            </div>
                        </div>
                        
                        <!-- Giá tiền -->
                        <div class="filter-block">
                            <label class="filter-block-title">💰 Giá tiền (đồng/tháng)</label>
                            <div class="filter-range">
                                <input type="number" name="gia_min" placeholder="Từ" value="<?php echo htmlspecialchars($gia_min); ?>" step="1000" min="0" class="filter-input">
                                <span class="range-separator">-</span>
                                <input type="number" name="gia_max" placeholder="Đến" value="<?php echo htmlspecialchars($gia_max); ?>" step="1000" min="0" class="filter-input">
                            </div>
                        </div>
                        
                        <!-- Địa chỉ -->
                        <div class="filter-block">
                            <label class="filter-block-title">📍 Địa chỉ / Quận-Huyện</label>
                            <input type="text" name="dia_chi" placeholder="Ví dụ: Hoàng Mai, Hà Nội..." value="<?php echo htmlspecialchars($dia_chi_filter); ?>" class="filter-input">
                        </div>
                        
                        <button type="submit" class="btn-filter-submit">Áp dụng bộ lọc</button>
                    </form>
                </div>
            </aside>
            
            <!-- Danh sách phòng (Bên phải) -->
            <main class="rooms-content">
                <div class="rooms-grid">
            <?php if (empty($rooms)): ?>
                <p class="no-results">Không tìm thấy phòng nào.</p>
            <?php else: ?>
                <?php foreach ($rooms as $room): ?>
                    <?php
                    // Lấy hình ảnh chính hoặc hình ảnh đầu tiên
                    $main_image = $room['anh_chinh'] ?: ($room['anh_dau_tien'] ?: 'assets/images/no-image.jpg');
                    $is_favorite = in_array($room['id'], $favorite_rooms);
                    ?>
                    <div class="room-card">
                        <div class="room-image">
                            <img src="<?php echo htmlspecialchars($main_image); ?>" alt="<?php echo htmlspecialchars($room['tieu_de']); ?>" onerror="this.src='assets/images/no-image.jpg'">
                            <span class="room-status status-<?php echo $room['trang_thai']; ?>">
                                <?php echo getTrangThaiPhongText($room['trang_thai']); ?>
                            </span>
                            <?php 
                            // [ĐÃ SỬA CHUẨN]
                            // Chỉ hiển thị nút yêu thích khi:
                            // 1. Đã đăng nhập
                            // 2. Không phải phòng của mình
                            // 3. KHÔNG phải Chủ nhà (bất kể phòng ai)
                            // 4. KHÔNG phải Admin
                            
                            $is_own_room = isLoggedIn() && $room['nguoi_dung_id'] == $_SESSION['user_id'];
                            
                            // Thêm !isChuNha() và !isAdmin() vào điều kiện
                            if (isLoggedIn() && !$is_own_room && !isChuNha() && !isAdmin()): 
                            ?>
                                <button class="favorite-btn <?php echo $is_favorite ? 'active' : ''; ?>" 
                                        onclick="toggleFavorite(<?php echo $room['id']; ?>, this)" 
                                        title="<?php echo $is_favorite ? 'Bỏ yêu thích' : 'Yêu thích'; ?>">
                                    ❤️
                                </button>
                            <?php endif; ?>
                        </div>
                        <div class="room-info">
                            <h3><?php echo htmlspecialchars($room['tieu_de']); ?></h3>
                            <p class="room-price"><?php echo number_format($room['gia']); ?> đ/tháng</p>
                            <p class="room-address">📍 <?php echo htmlspecialchars($room['dia_chi']); ?></p>
                            <?php if ($room['quan_huyen']): ?>
                                <p class="room-district">🏘️ <?php echo htmlspecialchars($room['quan_huyen']); ?></p>
                            <?php endif; ?>
                            <?php if ($room['dien_tich']): ?>
                                <p class="room-area">📐 Diện tích: <?php echo $room['dien_tich']; ?> m²</p>
                            <?php endif; ?>
                            <a href="room_detail.php?id=<?php echo $room['id']; ?>" class="btn btn-primary">Xem chi tiết</a>
                        </div>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
                </div>
            </main>
        </div>
    </div>
    
    <?php include 'includes/footer.php'; ?>
    
    <script src="assets/js/favorites.js"></script>
</body>
</html>
