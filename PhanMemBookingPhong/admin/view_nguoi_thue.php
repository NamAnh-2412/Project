<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

$nguoi_thue_id = $_GET['id'] ?? 0;

// Lấy thông tin người thuê
$stmt = $pdo->prepare("SELECT * FROM nguoi_dung WHERE id = ? AND vai_tro = 'nguoi_thue'");
$stmt->execute([$nguoi_thue_id]);
$nguoi_thue = $stmt->fetch();

if (!$nguoi_thue) {
    header('Location: manage_nguoi_thue.php');
    exit;
}

// Lấy danh sách booking của người thuê này
$stmt = $pdo->prepare("SELECT dp.*, 
                              p.tieu_de as phong_tieu_de,
                              p.dia_chi as phong_dia_chi,
                              p.gia as phong_gia,
                              chu_nha.ho_ten as chu_nha_ten,
                              chu_nha.email as chu_nha_email
                       FROM dat_phong dp
                       JOIN phong p ON dp.phong_id = p.id
                       LEFT JOIN nguoi_dung chu_nha ON p.nguoi_dung_id = chu_nha.id
                       WHERE dp.nguoi_dung_id = ?
                       ORDER BY dp.id DESC");
$stmt->execute([$nguoi_thue_id]);
$bookings = $stmt->fetchAll();

// Thống kê
$stats = [
    'total_bookings' => count($bookings),
    'cho_xac_nhan' => 0,
    'da_xac_nhan' => 0,
    'da_huy' => 0
];

foreach ($bookings as $booking) {
    $stats[$booking['trang_thai']]++;
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Người thuê - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="margin-bottom: 20px;">
            <a href="manage_nguoi_thue.php" class="btn btn-secondary">← Quay lại</a>
        </div>
        
        <h1>👥 Chi tiết Người thuê</h1>
        
        <!-- Thông tin người thuê -->
        <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 30px;">
            <h2>Thông tin cá nhân</h2>
            <table class="table" style="margin-top: 15px;">
                <tr>
                    <th width="200">ID:</th>
                    <td><?php echo $nguoi_thue['id']; ?></td>
                </tr>
                <tr>
                    <th>Họ tên:</th>
                    <td><strong><?php echo htmlspecialchars($nguoi_thue['ho_ten'] ?? 'N/A'); ?></strong></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><?php echo htmlspecialchars($nguoi_thue['email']); ?></td>
                </tr>
                <tr>
                    <th>Số điện thoại:</th>
                    <td><?php echo htmlspecialchars($nguoi_thue['so_dien_thoai'] ?? 'N/A'); ?></td>
                </tr>
                <tr>
                    <th>Ngày đăng ký:</th>
                    <td><?php echo date('d/m/Y H:i', strtotime($nguoi_thue['ngay_tao'])); ?></td>
                </tr>
            </table>
        </div>
        
        <!-- Thống kê -->
        <div class="stats-grid" style="margin-bottom: 30px;">
            <div class="stat-card blue">
                <h3>Tổng số booking</h3>
                <p class="stat-number"><?php echo $stats['total_bookings']; ?></p>
            </div>
            <div class="stat-card orange">
                <h3>Chờ xác nhận</h3>
                <p class="stat-number"><?php echo $stats['cho_xac_nhan']; ?></p>
            </div>
            <div class="stat-card green">
                <h3>Đã xác nhận</h3>
                <p class="stat-number"><?php echo $stats['da_xac_nhan']; ?></p>
            </div>
            <div class="stat-card red">
                <h3>Đã hủy</h3>
                <p class="stat-number"><?php echo $stats['da_huy']; ?></p>
            </div>
        </div>
        
        <!-- Danh sách booking -->
        <h2>Danh sách booking (<?php echo count($bookings); ?>)</h2>
        <?php if (empty($bookings)): ?>
            <p class="no-results">Người thuê này chưa có booking nào.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Phòng</th>
                            <th>Chủ nhà</th>
                            <th>Giá</th>
                            <th>Trạng thái</th>
                            <th>Ngày đặt</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($bookings as $booking): ?>
                            <?php
                            $trang_thai_info = getTrangThaiBookingInfo($booking['trang_thai']);
                            ?>
                            <tr>
                                <td><?php echo $booking['id']; ?></td>
                                <td>
                                    <strong><?php echo htmlspecialchars($booking['phong_tieu_de']); ?></strong>
                                    <br>
                                    <small style="color: #666;"><?php echo htmlspecialchars(mb_substr($booking['phong_dia_chi'], 0, 40)) . '...'; ?></small>
                                </td>
                                <td>
                                    <strong><?php echo htmlspecialchars($booking['chu_nha_ten'] ?? 'N/A'); ?></strong>
                                    <br>
                                    <small style="color: #666;"><?php echo htmlspecialchars($booking['chu_nha_email'] ?? ''); ?></small>
                                </td>
                                <td><strong style="color: #e74c3c;"><?php echo number_format($booking['phong_gia']); ?> đ</strong></td>
                                <td>
                                    <span class="badge" style="background: <?php echo $trang_thai_info['color']; ?>; color: white; padding: 5px 10px; border-radius: 4px;">
                                        <?php echo $trang_thai_info['text']; ?>
                                    </span>
                                </td>
                                <td><?php echo date('d/m/Y H:i', strtotime($booking['ngay_tao'])); ?></td>
                                <td>
                                    <a href="../room_detail.php?id=<?php echo $booking['phong_id']; ?>" target="_blank" class="btn btn-primary btn-sm">Xem phòng</a>
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

