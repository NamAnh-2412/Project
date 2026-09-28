<?php
require_once 'config/database.php';
require_once 'config/config.php';

requireLogin();
requireNguoiThue(); // Chỉ Người thuê mới được đặt phòng

$phong_id = $_GET['phong_id'] ?? 0;
$pdo = getDBConnection();
$user = getCurrentUser();

// Lấy thông tin phòng
$stmt = $pdo->prepare("SELECT * FROM phong WHERE id = ?");
$stmt->execute([$phong_id]);
$phong = $stmt->fetch();

if (!$phong || $phong['trang_thai'] !== 'con_trong') {
    header('Location: index.php');
    exit;
}

// Không cho phép đặt phòng của chính mình
if ($phong['nguoi_dung_id'] == $user['id']) {
    header('Location: room_detail.php?id=' . $phong_id . '&error=own_room');
    exit;
}

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $tin_nhan = trim($_POST['tin_nhan'] ?? '');
    $so_dien_thoai_lien_he = trim($_POST['so_dien_thoai_lien_he'] ?? '');
    
    try {
        $stmt = $pdo->prepare("INSERT INTO dat_phong (phong_id, nguoi_dung_id, tin_nhan, so_dien_thoai_lien_he, trang_thai) VALUES (?, ?, ?, ?, 'cho_xac_nhan')");
        $stmt->execute([$phong_id, $user['id'], $tin_nhan ?: null, $so_dien_thoai_lien_he ?: null]);
        
        $success = 'Đặt phòng thành công! Vui lòng chờ chủ nhà xác nhận.';
    } catch (PDOException $e) {
        $error = 'Có lỗi xảy ra: ' . $e->getMessage();
    }
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt phòng - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
    <?php include 'includes/header.php'; ?>
    
    <div class="container">
        <h1>Đặt phòng</h1>
        
        <?php if ($error): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <?php if ($success): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
            <a href="index.php" class="btn btn-primary">Quay về trang chủ</a>
        <?php else: ?>
            <div class="room-detail">
                <h2><?php echo htmlspecialchars($phong['tieu_de']); ?></h2>
                <p><strong>Địa chỉ:</strong> <?php echo htmlspecialchars($phong['dia_chi']); ?></p>
                <p><strong>Giá:</strong> <?php echo number_format($phong['gia']); ?> đ/tháng</p>
                
                <form method="POST" action="" class="booking-form">
                    <div class="form-group">
                        <label>Số điện thoại liên hệ *</label>
                        <input type="tel" name="so_dien_thoai_lien_he" required value="<?php echo htmlspecialchars($_POST['so_dien_thoai_lien_he'] ?? $user['so_dien_thoai'] ?? ''); ?>">
                    </div>
                    
                    <div class="form-group">
                        <label>Tin nhắn (nếu có)</label>
                        <textarea name="tin_nhan" rows="5" placeholder="Nhập thông tin bổ sung hoặc yêu cầu của bạn..."><?php echo htmlspecialchars($_POST['tin_nhan'] ?? ''); ?></textarea>
                    </div>
                    
                    <button type="submit" class="btn btn-success btn-large">Xác nhận đặt phòng</button>
                    <a href="room_detail.php?id=<?php echo $phong_id; ?>" class="btn btn-secondary">Quay lại</a>
                </form>
            </div>
        <?php endif; ?>
    </div>
    
    <?php include 'includes/footer.php'; ?>
</body>
</html>
