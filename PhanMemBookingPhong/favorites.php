<?php
require_once 'config/database.php';
require_once 'config/config.php';

requireLogin();
requireNguoiThue(); // Chỉ Người thuê mới xem được phòng yêu thích

$user = getCurrentUser();
$pdo = getDBConnection();

// Lấy danh sách phòng yêu thích
$stmt = $pdo->prepare("SELECT p.*, 
                              " . getAnhChinhSubquery() . ",
                              " . getAnhDauTienSubquery() . ",
                              yt.ngay_tao as ngay_yeu_thich
                       FROM yeu_thich yt
                       INNER JOIN phong p ON yt.phong_id = p.id
                       WHERE yt.nguoi_dung_id = ?
                       ORDER BY yt.ngay_tao DESC, p.id DESC");
$stmt->execute([$user['id']]);
$favorite_rooms = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phòng yêu thích - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
    <?php include 'includes/header.php'; ?>
    
    <div class="container">
        <h1>Phòng yêu thích của tôi</h1>
        
        <?php if (empty($favorite_rooms)): ?>
            <p class="no-results">Bạn chưa có phòng yêu thích nào.</p>
            <a href="index.php" class="btn btn-primary">Tìm phòng ngay</a>
        <?php else: ?>
            <div class="rooms-grid">
                <?php foreach ($favorite_rooms as $room): ?>
                    <?php
                    $main_image = $room['anh_chinh'] ?: ($room['anh_dau_tien'] ?: 'assets/images/no-image.jpg');
                    ?>
                    <div class="room-card">
                        <div class="room-image">
                            <img src="<?php echo htmlspecialchars($main_image); ?>" alt="<?php echo htmlspecialchars($room['tieu_de']); ?>" onerror="this.src='assets/images/no-image.jpg'">
                            <span class="room-status status-<?php echo $room['trang_thai']; ?>">
                                <?php echo getTrangThaiPhongText($room['trang_thai']); ?>
                            </span>
                            <button class="favorite-btn active" 
                                    onclick="toggleFavorite(<?php echo $room['id']; ?>, this, {removeCardOnUnfavorite: true})" 
                                    title="Bỏ yêu thích">
                                ❤️
                            </button>
                        </div>
                        <div class="room-info">
                            <h3><?php echo htmlspecialchars($room['tieu_de']); ?></h3>
                            <p class="room-price"><?php echo number_format($room['gia']); ?> đ/tháng</p>
                            <p class="room-address">📍 <?php echo htmlspecialchars($room['dia_chi']); ?></p>
                            <?php if ($room['dien_tich']): ?>
                                <p class="room-area">📐 Diện tích: <?php echo $room['dien_tich']; ?> m²</p>
                            <?php endif; ?>
                            <p class="favorite-date"><small>Đã yêu thích: <?php echo date('d/m/Y', strtotime($room['ngay_yeu_thich'])); ?></small></p>
                            <a href="room_detail.php?id=<?php echo $room['id']; ?>" class="btn btn-primary">Xem chi tiết</a>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>
    
    <?php include 'includes/footer.php'; ?>
    
    <script src="assets/js/favorites.js"></script>
</body>
</html>
