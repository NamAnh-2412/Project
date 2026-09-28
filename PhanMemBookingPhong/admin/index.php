    <?php
require_once '../config/database.php';
require_once '../config/config.php';

requireLogin();

$pdo = getDBConnection();
$user = getCurrentUser();
$is_admin = isAdmin();
$is_chu_nha = isChuNha();

// Chỉ Admin hoặc Chủ nhà mới được xem thống kê
if (!$is_admin && !$is_chu_nha) {
    header('Location: ' . BASE_URL . 'index.php');
    exit;
}

// Thống kê
$stats = [];

if ($is_admin) {
    // Thống kê cho Admin - Tổng quan toàn hệ thống
    // Tổng số phòng trong hệ thống
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM phong");
    $result = $stmt->fetch();
    $stats['total_rooms'] = $result ? $result['total'] : 0;
    
    // Phòng còn trống
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'con_trong'");
    $result = $stmt->fetch();
    $stats['available_rooms'] = $result ? $result['total'] : 0;
    
    // Phòng đã thuê
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'da_thue'");
    $result = $stmt->fetch();
    $stats['rented_rooms'] = $result ? $result['total'] : 0;
    
    // Tổng số booking
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM dat_phong");
    $result = $stmt->fetch();
    $stats['total_bookings'] = $result ? $result['total'] : 0;
    
    // Booking đang chờ xác nhận
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'cho_xac_nhan'");
    $result = $stmt->fetch();
    $stats['pending_bookings'] = $result ? $result['total'] : 0;
    
    // Tổng số người thuê
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'nguoi_thue'");
    $result = $stmt->fetch();
    $stats['total_customers'] = $result ? $result['total'] : 0;
    
    // Tổng số chủ nhà
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'chu_nha'");
    $result = $stmt->fetch();
    $stats['total_chu_nha'] = $result ? $result['total'] : 0;
    
    // Tổng số admin
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'admin'");
    $result = $stmt->fetch();
    $stats['total_admin'] = $result ? $result['total'] : 0;
} else {
    // Thống kê cho Chủ nhà - Chỉ phòng của mình
    // Tổng số phòng của chủ nhà này
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = ?");
    $stmt->execute([$user['id']]);
    $result = $stmt->fetch();
    $stats['total_rooms'] = $result ? $result['total'] : 0;
    
    // Phòng còn trống
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = ? AND trang_thai = 'con_trong'");
    $stmt->execute([$user['id']]);
    $result = $stmt->fetch();
    $stats['available_rooms'] = $result ? $result['total'] : 0;
    
    // Phòng đã thuê
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = ? AND trang_thai = 'da_thue'");
    $stmt->execute([$user['id']]);
    $result = $stmt->fetch();
    $stats['rented_rooms'] = $result ? $result['total'] : 0;
    
    // Tổng số booking cho phòng của chủ nhà này
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM dat_phong dp 
                           INNER JOIN phong p ON dp.phong_id = p.id 
                           WHERE p.nguoi_dung_id = ?");
    $stmt->execute([$user['id']]);
    $result = $stmt->fetch();
    $stats['total_bookings'] = $result ? $result['total'] : 0;
    
    // Booking đang chờ xác nhận
    $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM dat_phong dp 
                           INNER JOIN phong p ON dp.phong_id = p.id 
                           WHERE p.nguoi_dung_id = ? AND dp.trang_thai = 'cho_xac_nhan'");
    $stmt->execute([$user['id']]);
    $result = $stmt->fetch();
    $stats['pending_bookings'] = $result ? $result['total'] : 0;
    
    // Tổng số người thuê
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'nguoi_thue'");
    $result = $stmt->fetch();
    $stats['total_customers'] = $result ? $result['total'] : 0;
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thống kê - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <h1><?php echo $is_admin ? '🛡️ Thống kê hệ thống ' : 'Thống kê'; ?></h1>
        <p>Xin chào, <strong><?php echo htmlspecialchars($user['ho_ten']); ?></strong>
        <?php if ($is_admin): ?>
            <span class="badge badge-admin" style="background: #dc3545; color: white; padding: 5px 10px; border-radius: 4px; margin-left: 10px;">Admin</span>
        <?php endif; ?>
        </p>
        
        <!-- Thống kê -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3><?php echo $is_admin ? 'Tổng số phòng (Hệ thống)' : 'Tổng số phòng'; ?></h3>
                <p class="stat-number"><?php echo $stats['total_rooms'] ?? 0; ?></p>
            </div>
            <div class="stat-card">
                <h3>Phòng còn trống</h3>
                <p class="stat-number"><?php echo $stats['available_rooms'] ?? 0; ?></p>
            </div>
            <div class="stat-card">
                <h3>Phòng đã thuê</h3>
                <p class="stat-number"><?php echo $stats['rented_rooms'] ?? 0; ?></p>
            </div>
            <?php if ($is_admin): ?>
                <div class="stat-card">
                    <h3><?php echo $is_admin ? 'Tổng số booking (Hệ thống)' : 'Tổng số booking'; ?></h3>
                    <p class="stat-number"><?php echo $stats['total_bookings'] ?? 0; ?></p>
                </div>
            <?php endif; ?>
            <div class="stat-card">
                <h3>Booking chờ xác nhận</h3>
                <p class="stat-number"><?php echo $stats['pending_bookings'] ?? 0; ?></p>
            </div>
            <?php if ($is_admin): ?>
                <div class="stat-card">
                    <h3>🏠 Tổng số chủ nhà</h3>
                    <p class="stat-number"><?php echo $stats['total_chu_nha'] ?? 0; ?></p>
                </div>
            <?php else: ?>
                <div class="stat-card">
                    <h3>Tổng số người thuê</h3>
                    <p class="stat-number"><?php echo $stats['total_customers'] ?? 0; ?></p>
                </div>
            <?php endif; ?>
        </div>
    </div>
    
    <?php include '../includes/footer.php'; ?>
</body>
</html>
