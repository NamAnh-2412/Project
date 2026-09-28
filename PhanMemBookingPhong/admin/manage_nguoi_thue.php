<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

// Xử lý xóa người thuê
if (isset($_GET['delete']) && $_GET['delete']) {
    $user_id = $_GET['delete'];
    
    // Kiểm tra xem người thuê này có booking không
    $check_stmt = $pdo->prepare("SELECT COUNT(*) as count FROM dat_phong WHERE nguoi_dung_id = ?");
    $check_stmt->execute([$user_id]);
    $has_bookings = $check_stmt->fetch()['count'] > 0;
    
    if ($has_bookings) {
        $error = 'Không thể xóa người thuê này vì đang có đơn đặt phòng. Vui lòng xóa booking trước.';
    } else {
        try {
            $stmt = $pdo->prepare("DELETE FROM nguoi_dung WHERE id = ? AND vai_tro = 'nguoi_thue'");
            $stmt->execute([$user_id]);
            $success = 'Đã xóa người thuê thành công!';
        } catch (PDOException $e) {
            $error = 'Lỗi khi xóa: ' . $e->getMessage();
        }
    }
}

// Lấy danh sách người thuê
$stmt = $pdo->query("SELECT nd.*, 
                            COUNT(dp.id) as so_booking,
                            COUNT(CASE WHEN dp.trang_thai = 'cho_xac_nhan' THEN 1 END) as booking_cho_xac_nhan,
                            COUNT(CASE WHEN dp.trang_thai = 'da_xac_nhan' THEN 1 END) as booking_da_xac_nhan
                     FROM nguoi_dung nd
                     LEFT JOIN dat_phong dp ON nd.id = dp.nguoi_dung_id
                     WHERE nd.vai_tro = 'nguoi_thue'
                     GROUP BY nd.id
                     ORDER BY nd.id DESC");
$nguoi_thue_list = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Người thuê - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h1>👥 Quản lý Người thuê</h1>
            <a href="dashboard.php" class="btn btn-secondary">← Về trang quản lý tổng</a>
        </div>
        
        <?php if (isset($error)): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <?php if (isset($success)): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
        <?php endif; ?>
        
        <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <p><strong>Tổng số người thuê:</strong> <?php echo count($nguoi_thue_list); ?></p>
        </div>
        
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Số điện thoại</th>
                        <th>Số booking</th>
                        <th>Chờ xác nhận</th>
                        <th>Đã xác nhận</th>
                        <th>Ngày đăng ký</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($nguoi_thue_list)): ?>
                        <tr>
                            <td colspan="9" class="text-center">Chưa có người thuê nào</td>
                        </tr>
                    <?php else: ?>
                        <?php foreach ($nguoi_thue_list as $nguoi_thue): ?>
                            <tr>
                                <td><?php echo $nguoi_thue['id']; ?></td>
                                <td><strong><?php echo htmlspecialchars($nguoi_thue['ho_ten'] ?? 'N/A'); ?></strong></td>
                                <td><?php echo htmlspecialchars($nguoi_thue['email']); ?></td>
                                <td><?php echo htmlspecialchars($nguoi_thue['so_dien_thoai'] ?? 'N/A'); ?></td>
                                <td><span class="badge" style="background: #3498db; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $nguoi_thue['so_booking']; ?></span></td>
                                <td><span class="badge" style="background: #f39c12; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $nguoi_thue['booking_cho_xac_nhan']; ?></span></td>
                                <td><span class="badge" style="background: #27ae60; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $nguoi_thue['booking_da_xac_nhan']; ?></span></td>
                                <td><?php echo date('d/m/Y H:i', strtotime($nguoi_thue['ngay_tao'])); ?></td>
                                <td>
                                    <a href="view_nguoi_thue.php?id=<?php echo $nguoi_thue['id']; ?>" class="btn btn-primary btn-sm">Xem chi tiết</a>
                                    <?php if ($nguoi_thue['so_booking'] == 0): ?>
                                        <a href="?delete=<?php echo $nguoi_thue['id']; ?>" 
                                           class="btn btn-danger btn-sm" 
                                           onclick="return confirm('Bạn có chắc muốn xóa người thuê này?')">Xóa</a>
                                    <?php else: ?>
                                        <span style="color: #999; font-size: 12px;">Có booking</span>
                                    <?php endif; ?>
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

