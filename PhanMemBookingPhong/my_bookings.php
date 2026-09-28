<?php
require_once 'config/database.php';
require_once 'config/config.php';

requireLogin();
requireNguoiThue(); // Chỉ Người thuê mới xem được đặt phòng của mình

$user = getCurrentUser();
$pdo = getDBConnection();

// Xử lý hủy booking
if (isset($_GET['cancel']) && $_GET['cancel']) {
    $booking_id = $_GET['cancel'];
    
    // Kiểm tra booking thuộc về user này
    $check_stmt = $pdo->prepare("SELECT * FROM dat_phong WHERE id = ? AND nguoi_dung_id = ?");
    $check_stmt->execute([$booking_id, $user['id']]);
    $booking = $check_stmt->fetch();
    
    if ($booking && ($booking['trang_thai'] === 'cho_xac_nhan' || $booking['trang_thai'] === 'da_xac_nhan')) {
        try {
            $pdo->beginTransaction();
            
            // Cập nhật trạng thái booking thành đã hủy
            $stmt = $pdo->prepare("UPDATE dat_phong SET trang_thai = 'da_huy' WHERE id = ?");
            $stmt->execute([$booking_id]);
            
            // Nếu booking đã được xác nhận, cập nhật lại trạng thái phòng thành còn trống
            if ($booking['trang_thai'] === 'da_xac_nhan') {
                $stmt = $pdo->prepare("UPDATE phong SET trang_thai = 'con_trong' WHERE id = ?");
                $stmt->execute([$booking['phong_id']]);
            }
            
            $pdo->commit();
            header('Location: my_bookings.php?success=cancelled');
            exit;
        } catch (PDOException $e) {
            $pdo->rollBack();
            header('Location: my_bookings.php?error=cancel_failed');
            exit;
        }
    } else {
        header('Location: my_bookings.php?error=invalid_booking');
        exit;
    }
}

// Lấy danh sách booking của user kèm thông tin chủ nhà (chỉ khi đã xác nhận)
$stmt = $pdo->prepare("SELECT dp.*, 
                              p.tieu_de as phong_tieu_de, 
                              p.dia_chi as phong_dia_chi, 
                              p.gia,
                              p.nguoi_dung_id as chu_nha_id,
                              nd.ho_ten as chu_nha_ten,
                              nd.so_dien_thoai as chu_nha_sdt,
                              nd.email as chu_nha_email,
                              " . getAnhChinhSubquery() . ",
                              " . getAnhDauTienSubquery() . "
                       FROM dat_phong dp 
                       JOIN phong p ON dp.phong_id = p.id 
                       LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
                       WHERE dp.nguoi_dung_id = ? 
                       ORDER BY dp.id DESC, dp.ngay_tao DESC");
$stmt->execute([$user['id']]);
$bookings = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt phòng của tôi - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="assets/css/bookings.css">
</head>
<body>
    <?php include 'includes/header.php'; ?>
    
    <div class="container">
        <h1>Đặt phòng của tôi</h1>
        
        <?php if (isset($_GET['success']) && $_GET['success'] === 'cancelled'): ?>
            <div class="alert alert-success">Đã hủy đặt phòng thành công!</div>
        <?php endif; ?>
        
        <?php if (isset($_GET['error'])): ?>
            <?php
            $error_msg = [
                'cancel_failed' => 'Có lỗi xảy ra khi hủy đặt phòng. Vui lòng thử lại.',
                'invalid_booking' => 'Đơn đặt phòng không hợp lệ hoặc không thể hủy.'
            ];
            ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error_msg[$_GET['error']] ?? 'Có lỗi xảy ra.'); ?></div>
        <?php endif; ?>
        
        <?php if (empty($bookings)): ?>
            <p class="no-results">Bạn chưa có đơn đặt phòng nào.</p>
            <a href="index.php" class="btn btn-primary">Tìm phòng ngay</a>
        <?php else: ?>
            <div class="bookings-list">
                <?php foreach ($bookings as $booking): ?>
                    <?php
                    $main_image = $booking['anh_chinh'] ?: ($booking['anh_dau_tien'] ?: 'assets/images/no-image.jpg');
                    $trang_thai_info = getTrangThaiBookingInfo($booking['trang_thai']);
                    ?>
                    <div class="booking-card">
                        <div class="booking-image">
                            <img src="<?php echo htmlspecialchars($main_image); ?>" alt="<?php echo htmlspecialchars($booking['phong_tieu_de']); ?>" onerror="this.src='assets/images/no-image.jpg'">
                        </div>
                        <div class="booking-info">
                            <h3><?php echo htmlspecialchars($booking['phong_tieu_de']); ?></h3>
                            <p><strong>Địa chỉ:</strong> <?php echo htmlspecialchars($booking['phong_dia_chi']); ?></p>
                            <p><strong>Giá:</strong> <?php echo number_format($booking['gia']); ?> đ/tháng</p>
                            <p><strong>Trạng thái:</strong> 
                                <span style="background-color: <?php echo $trang_thai_info['color']; ?>; color: white; padding: 5px 10px; border-radius: 4px;">
                                    <?php echo $trang_thai_info['text']; ?>
                                </span>
                            </p>
                            <?php if ($booking['so_dien_thoai_lien_he']): ?>
                                <p><strong>Số điện thoại liên hệ:</strong> <?php echo htmlspecialchars($booking['so_dien_thoai_lien_he']); ?></p>
                            <?php endif; ?>
                            <?php if ($booking['tin_nhan']): ?>
                                <p><strong>Tin nhắn:</strong> <?php echo htmlspecialchars($booking['tin_nhan']); ?></p>
                            <?php endif; ?>
                            
                            <!-- Thông tin chủ nhà - Chỉ hiển thị khi đã được xác nhận -->
                            <?php if ($booking['trang_thai'] === 'da_xac_nhan' && $booking['chu_nha_ten']): ?>
                                <div style="background: #e8f5e9; padding: 15px; border-radius: 6px; margin-top: 15px; border-left: 4px solid #27ae60;">
                                    <h4 style="margin: 0 0 10px 0; color: #27ae60;">✅ Thông tin chủ nhà</h4>
                                    <p style="margin: 5px 0;"><strong>👤 Tên:</strong> <?php echo htmlspecialchars($booking['chu_nha_ten']); ?></p>
                                    <?php if ($booking['chu_nha_sdt']): ?>
                                        <p style="margin: 5px 0;"><strong>📞 Điện thoại:</strong> <?php echo htmlspecialchars($booking['chu_nha_sdt']); ?></p>
                                    <?php endif; ?>
                                    <?php if ($booking['chu_nha_email']): ?>
                                        <p style="margin: 5px 0;"><strong>📧 Email:</strong> <?php echo htmlspecialchars($booking['chu_nha_email']); ?></p>
                                    <?php endif; ?>
                                </div>
                            <?php elseif ($booking['trang_thai'] === 'cho_xac_nhan'): ?>
                                <div style="background: #fff3cd; padding: 15px; border-radius: 6px; margin-top: 15px; border-left: 4px solid #f39c12;">
                                    <p style="margin: 0; color: #856404;"><strong>⏳ Đang chờ xác nhận:</strong> Thông tin chủ nhà sẽ được hiển thị sau khi đơn đặt phòng được xác nhận.</p>
                                </div>
                            <?php endif; ?>
                            
                            <p><small>Ngày đặt: <?php echo date('d/m/Y H:i', strtotime($booking['ngay_tao'])); ?></small></p>
                            <div style="display: flex; gap: 10px; margin-top: 15px; flex-wrap: wrap;">
                                <a href="room_detail.php?id=<?php echo $booking['phong_id']; ?>" class="btn btn-primary">Xem chi tiết phòng</a>
                                <?php if ($booking['trang_thai'] === 'cho_xac_nhan' || $booking['trang_thai'] === 'da_xac_nhan'): ?>
                                    <a href="?cancel=<?php echo $booking['id']; ?>" 
                                       class="btn btn-danger" 
                                       onclick="return confirm('Bạn có chắc muốn hủy đặt phòng này? Hành động này không thể hoàn tác.')">
                                        Hủy đặt phòng
                                    </a>
                                <?php endif; ?>
                            </div>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>
    
    <?php include 'includes/footer.php'; ?>
</body>
</html>
