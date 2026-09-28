<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

// Xử lý xóa booking
if (isset($_GET['delete']) && $_GET['delete']) {
    $booking_id = $_GET['delete'];
    
    try {
        $stmt = $pdo->prepare("DELETE FROM dat_phong WHERE id = ?");
        $stmt->execute([$booking_id]);
        $success = 'Đã xóa booking thành công!';
    } catch (PDOException $e) {
        $error = 'Lỗi khi xóa: ' . $e->getMessage();
    }
}

// Lấy danh sách booking
$stmt = $pdo->query("SELECT dp.*, 
                            p.tieu_de as phong_tieu_de,
                            p.dia_chi as phong_dia_chi,
                            p.gia as phong_gia,
                            nd.ho_ten as nguoi_thue_ten,
                            nd.email as nguoi_thue_email,
                            nd.so_dien_thoai as nguoi_thue_sdt,
                            chu_nha.ho_ten as chu_nha_ten,
                            chu_nha.email as chu_nha_email
                     FROM dat_phong dp
                     JOIN phong p ON dp.phong_id = p.id
                     JOIN nguoi_dung nd ON dp.nguoi_dung_id = nd.id
                     LEFT JOIN nguoi_dung chu_nha ON p.nguoi_dung_id = chu_nha.id
                     ORDER BY dp.id DESC");
$bookings = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Booking - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h1>📋 Quản lý Booking</h1>
            <a href="dashboard.php" class="btn btn-secondary">← Về trang quản lý tổng</a>
        </div>
        
        <?php if (isset($error)): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <?php if (isset($success)): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
        <?php endif; ?>
        
        <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <p><strong>Tổng số booking:</strong> <?php echo count($bookings); ?></p>
        </div>
        
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Phòng</th>
                        <th>Người thuê</th>
                        <th>Chủ nhà</th>
                        <th>Giá</th>
                        <th>Trạng thái</th>
                        <th>Ngày đặt</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($bookings)): ?>
                        <tr>
                            <td colspan="8" class="text-center">Chưa có booking nào</td>
                        </tr>
                    <?php else: ?>
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
                                    <strong><?php echo htmlspecialchars($booking['nguoi_thue_ten'] ?? 'N/A'); ?></strong>
                                    <br>
                                    <small style="color: #666;"><?php echo htmlspecialchars($booking['nguoi_thue_email']); ?></small>
                                    <?php if ($booking['so_dien_thoai_lien_he']): ?>
                                        <br>
                                        <small style="color: #666;">📞 <?php echo htmlspecialchars($booking['so_dien_thoai_lien_he']); ?></small>
                                    <?php endif; ?>
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
                                    <a href="?delete=<?php echo $booking['id']; ?>" 
                                       class="btn btn-danger btn-sm" 
                                       onclick="return confirm('Bạn có chắc muốn xóa booking này?')">Xóa</a>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
    
    <?php include '../includes/footer.php'; ?>
</body>
</html>

