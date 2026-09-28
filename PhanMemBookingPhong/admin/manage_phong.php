<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

// Xử lý xóa phòng
if (isset($_GET['delete']) && $_GET['delete']) {
    $phong_id = $_GET['delete'];
    
    try {
        $pdo->beginTransaction();
        
        // Xóa ảnh phòng
        $stmt = $pdo->prepare("DELETE FROM anh_phong WHERE phong_id = ?");
        $stmt->execute([$phong_id]);
        
        // Xóa booking liên quan
        $stmt = $pdo->prepare("DELETE FROM dat_phong WHERE phong_id = ?");
        $stmt->execute([$phong_id]);
        
        // Xóa tiện ích
        $stmt = $pdo->prepare("DELETE FROM phong_tien_ich WHERE phong_id = ?");
        $stmt->execute([$phong_id]);
        
        // Xóa phòng
        $stmt = $pdo->prepare("DELETE FROM phong WHERE id = ?");
        $stmt->execute([$phong_id]);
        
        $pdo->commit();
        $success = 'Đã xóa phòng thành công!';
    } catch (PDOException $e) {
        $pdo->rollBack();
        $error = 'Lỗi khi xóa: ' . $e->getMessage();
    }
}

// Lấy danh sách phòng với thông tin chủ nhà
$stmt = $pdo->query("SELECT p.*, 
                            nd.ho_ten as chu_nha_ten,
                            nd.email as chu_nha_email,
                            " . getAnhChinhSubquery() . ",
                            " . getAnhDauTienSubquery() . "
                     FROM phong p
                     LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
                     ORDER BY p.id DESC");
$phong_list = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Phòng - Admin</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .room-table-img {
            width: 80px;
            height: 60px;
            object-fit: cover;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h1>🏘️ Quản lý Phòng</h1>
            <a href="dashboard.php" class="btn btn-secondary">← Về trang quản lý tổng</a>
        </div>
        
        <?php if (isset($error)): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <?php if (isset($success)): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
        <?php endif; ?>
        
        <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <p><strong>Tổng số phòng:</strong> <?php echo count($phong_list); ?></p>
        </div>
        
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Hình ảnh</th>
                        <th>Tiêu đề</th>
                        <th>Chủ nhà</th>
                        <th>Giá</th>
                        <th>Diện tích</th>
                        <th>Địa chỉ</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($phong_list)): ?>
                        <tr>
                            <td colspan="9" class="text-center">Chưa có phòng nào</td>
                        </tr>
                    <?php else: ?>
                        <?php foreach ($phong_list as $phong): ?>
                            <?php
                            $main_image = $phong['anh_chinh'] ?: ($phong['anh_dau_tien'] ?: 'assets/images/no-image.jpg');
                            ?>
                            <tr>
                                <td><?php echo $phong['id']; ?></td>
                                <td>
                                    <img src="../<?php echo htmlspecialchars($main_image); ?>" 
                                         alt="Phòng <?php echo $phong['id']; ?>" 
                                         class="room-table-img"
                                         onerror="this.src='../assets/images/no-image.jpg'">
                                </td>
                                <td>
                                    <strong><?php echo htmlspecialchars($phong['tieu_de']); ?></strong>
                                    <br>
                                    <small style="color: #666;"><?php echo htmlspecialchars(mb_substr($phong['mo_ta'] ?? '', 0, 50)) . '...'; ?></small>
                                </td>
                                <td>
                                    <strong><?php echo htmlspecialchars($phong['chu_nha_ten'] ?? 'N/A'); ?></strong>
                                    <br>
                                    <small style="color: #666;"><?php echo htmlspecialchars($phong['chu_nha_email'] ?? ''); ?></small>
                                </td>
                                <td><strong style="color: #e74c3c;"><?php echo number_format($phong['gia']); ?> đ</strong></td>
                                <td><?php echo $phong['dien_tich'] ? $phong['dien_tich'] . ' m²' : 'N/A'; ?></td>
                                <td>
                                    <small><?php echo htmlspecialchars(mb_substr($phong['dia_chi'], 0, 40)) . '...'; ?></small>
                                    <?php if ($phong['quan_huyen']): ?>
                                        <br><small style="color: #666;"><?php echo htmlspecialchars($phong['quan_huyen']); ?></small>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <?php if ($phong['trang_thai'] === 'con_trong'): ?>
                                        <span class="badge" style="background: #27ae60; color: white; padding: 5px 10px; border-radius: 4px;">Còn trống</span>
                                    <?php else: ?>
                                        <span class="badge" style="background: #e74c3c; color: white; padding: 5px 10px; border-radius: 4px;">Đã thuê</span>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <a href="../room_detail.php?id=<?php echo $phong['id']; ?>" target="_blank" class="btn btn-primary btn-sm">Xem</a>
                                    <a href="?delete=<?php echo $phong['id']; ?>" 
                                       class="btn btn-danger btn-sm" 
                                       onclick="return confirm('Bạn có chắc muốn xóa phòng này? Tất cả dữ liệu liên quan sẽ bị xóa!')">Xóa</a>
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

