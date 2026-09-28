<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

// Xử lý xóa chủ nhà
if (isset($_GET['delete']) && $_GET['delete']) {
    $user_id = $_GET['delete'];
    
    // Kiểm tra xem chủ nhà này có phòng không
    $check_stmt = $pdo->prepare("SELECT COUNT(*) as count FROM phong WHERE nguoi_dung_id = ?");
    $check_stmt->execute([$user_id]);
    $has_rooms = $check_stmt->fetch()['count'] > 0;
    
    if ($has_rooms) {
        $error = 'Không thể xóa chủ nhà này vì đang có phòng trọ. Vui lòng xóa phòng trước.';
    } else {
        try {
            $stmt = $pdo->prepare("DELETE FROM nguoi_dung WHERE id = ? AND vai_tro = 'chu_nha'");
            $stmt->execute([$user_id]);
            $success = 'Đã xóa chủ nhà thành công!';
        } catch (PDOException $e) {
            $error = 'Lỗi khi xóa: ' . $e->getMessage();
        }
    }
}

// Lấy danh sách chủ nhà
$stmt = $pdo->query("SELECT nd.*, 
                            COUNT(p.id) as so_phong,
                            COUNT(CASE WHEN p.trang_thai = 'con_trong' THEN 1 END) as phong_con_trong,
                            COUNT(CASE WHEN p.trang_thai = 'da_thue' THEN 1 END) as phong_da_thue
                     FROM nguoi_dung nd
                     LEFT JOIN phong p ON nd.id = p.nguoi_dung_id
                     WHERE nd.vai_tro = 'chu_nha'
                     GROUP BY nd.id
                     ORDER BY nd.id DESC");
$chu_nha_list = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Chủ nhà - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h1>🏠 Quản lý Chủ nhà</h1>
            <a href="dashboard.php" class="btn btn-secondary">← Về trang quản lý tổng</a>
        </div>
        
        <?php if (isset($error)): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <?php if (isset($success)): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
        <?php endif; ?>
        
        <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <p><strong>Tổng số chủ nhà:</strong> <?php echo count($chu_nha_list); ?></p>
        </div>
        
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Số điện thoại</th>
                        <th>Số phòng</th>
                        <th>Phòng còn trống</th>
                        <th>Phòng đã thuê</th>
                        <th>Ngày đăng ký</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($chu_nha_list)): ?>
                        <tr>
                            <td colspan="9" class="text-center">Chưa có chủ nhà nào</td>
                        </tr>
                    <?php else: ?>
                        <?php foreach ($chu_nha_list as $chu_nha): ?>
                            <tr>
                                <td><?php echo $chu_nha['id']; ?></td>
                                <td><strong><?php echo htmlspecialchars($chu_nha['ho_ten'] ?? 'N/A'); ?></strong></td>
                                <td><?php echo htmlspecialchars($chu_nha['email']); ?></td>
                                <td><?php echo htmlspecialchars($chu_nha['so_dien_thoai'] ?? 'N/A'); ?></td>
                                <td><span class="badge" style="background: #3498db; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $chu_nha['so_phong']; ?></span></td>
                                <td><span class="badge" style="background: #27ae60; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $chu_nha['phong_con_trong']; ?></span></td>
                                <td><span class="badge" style="background: #e74c3c; color: white; padding: 5px 10px; border-radius: 4px;"><?php echo $chu_nha['phong_da_thue']; ?></span></td>
                                <td><?php echo date('d/m/Y H:i', strtotime($chu_nha['ngay_tao'])); ?></td>
                                <td>
                                    <a href="view_chu_nha.php?id=<?php echo $chu_nha['id']; ?>" class="btn btn-primary btn-sm">Xem chi tiết</a>
                                    <?php if ($chu_nha['so_phong'] == 0): ?>
                                        <a href="?delete=<?php echo $chu_nha['id']; ?>" 
                                           class="btn btn-danger btn-sm" 
                                           onclick="return confirm('Bạn có chắc muốn xóa chủ nhà này?')">Xóa</a>
                                    <?php else: ?>
                                        <span style="color: #999; font-size: 12px;">Có phòng</span>
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

