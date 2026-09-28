<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

$chu_nha_id = $_GET['id'] ?? 0;

// Lấy thông tin chủ nhà
$stmt = $pdo->prepare("SELECT * FROM nguoi_dung WHERE id = ? AND vai_tro = 'chu_nha'");
$stmt->execute([$chu_nha_id]);
$chu_nha = $stmt->fetch();

if (!$chu_nha) {
    header('Location: manage_chu_nha.php');
    exit;
}

// Lấy danh sách phòng của chủ nhà này
$stmt = $pdo->prepare("SELECT p.*, 
                              " . getAnhChinhSubquery() . ",
                              " . getAnhDauTienSubquery() . "
                       FROM phong p
                       WHERE p.nguoi_dung_id = ?
                       ORDER BY p.id DESC");
$stmt->execute([$chu_nha_id]);
$phong_list = $stmt->fetchAll();

// Thống kê
$stats = [
    'total_rooms' => count($phong_list),
    'available_rooms' => 0,
    'rented_rooms' => 0,
    'total_bookings' => 0
];

foreach ($phong_list as $phong) {
    if ($phong['trang_thai'] === 'con_trong') {
        $stats['available_rooms']++;
    } else {
        $stats['rented_rooms']++;
    }
}

$stmt = $pdo->prepare("SELECT COUNT(*) as total FROM dat_phong dp 
                       INNER JOIN phong p ON dp.phong_id = p.id 
                       WHERE p.nguoi_dung_id = ?");
$stmt->execute([$chu_nha_id]);
$result = $stmt->fetch();
$stats['total_bookings'] = $result ? $result['total'] : 0;
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Chủ nhà - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="margin-bottom: 20px;">
            <a href="manage_chu_nha.php" class="btn btn-secondary">← Quay lại</a>
        </div>
        
        <h1>🏠 Chi tiết Chủ nhà</h1>
        
        <!-- Thông tin chủ nhà -->
        <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 30px;">
            <h2>Thông tin cá nhân</h2>
            <table class="table" style="margin-top: 15px;">
                <tr>
                    <th width="200">ID:</th>
                    <td><?php echo $chu_nha['id']; ?></td>
                </tr>
                <tr>
                    <th>Họ tên:</th>
                    <td><strong><?php echo htmlspecialchars($chu_nha['ho_ten'] ?? 'N/A'); ?></strong></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><?php echo htmlspecialchars($chu_nha['email']); ?></td>
                </tr>
                <tr>
                    <th>Số điện thoại:</th>
                    <td><?php echo htmlspecialchars($chu_nha['so_dien_thoai'] ?? 'N/A'); ?></td>
                </tr>
                <tr>
                    <th>Ngày đăng ký:</th>
                    <td><?php echo date('d/m/Y H:i', strtotime($chu_nha['ngay_tao'])); ?></td>
                </tr>
            </table>
        </div>
        
        <!-- Thống kê -->
        <div class="stats-grid" style="margin-bottom: 30px;">
            <div class="stat-card blue">
                <h3>Tổng số phòng</h3>
                <p class="stat-number"><?php echo $stats['total_rooms']; ?></p>
            </div>
            <div class="stat-card green">
                <h3>Phòng còn trống</h3>
                <p class="stat-number"><?php echo $stats['available_rooms']; ?></p>
            </div>
            <div class="stat-card orange">
                <h3>Phòng đã thuê</h3>
                <p class="stat-number"><?php echo $stats['rented_rooms']; ?></p>
            </div>
            <div class="stat-card purple">
                <h3>Tổng số booking</h3>
                <p class="stat-number"><?php echo $stats['total_bookings']; ?></p>
            </div>
        </div>
        
        <!-- Danh sách phòng -->
        <h2>Danh sách phòng (<?php echo count($phong_list); ?>)</h2>
        <?php if (empty($phong_list)): ?>
            <p class="no-results">Chủ nhà này chưa có phòng nào.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tiêu đề</th>
                            <th>Giá</th>
                            <th>Diện tích</th>
                            <th>Địa chỉ</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($phong_list as $phong): ?>
                            <tr>
                                <td><?php echo $phong['id']; ?></td>
                                <td><strong><?php echo htmlspecialchars($phong['tieu_de']); ?></strong></td>
                                <td><strong style="color: #e74c3c;"><?php echo number_format($phong['gia']); ?> đ</strong></td>
                                <td><?php echo $phong['dien_tich'] ? $phong['dien_tich'] . ' m²' : 'N/A'; ?></td>
                                <td><small><?php echo htmlspecialchars(mb_substr($phong['dia_chi'], 0, 50)) . '...'; ?></small></td>
                                <td>
                                    <?php if ($phong['trang_thai'] === 'con_trong'): ?>
                                        <span class="badge" style="background: #27ae60; color: white; padding: 5px 10px; border-radius: 4px;">Còn trống</span>
                                    <?php else: ?>
                                        <span class="badge" style="background: #e74c3c; color: white; padding: 5px 10px; border-radius: 4px;">Đã thuê</span>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <a href="../room_detail.php?id=<?php echo $phong['id']; ?>" target="_blank" class="btn btn-primary btn-sm">Xem</a>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </div>
    
    <?php include '../includes/footer.php'; ?>
</body>
</html>

