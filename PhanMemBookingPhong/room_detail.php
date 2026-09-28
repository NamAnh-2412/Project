<?php
require_once 'config/database.php';
require_once 'config/config.php';

$phong_id = $_GET['id'] ?? 0;

$pdo = getDBConnection();
$stmt = $pdo->prepare("SELECT p.*, nd.ho_ten as chu_nha_ten, nd.email as chu_nha_email, nd.so_dien_thoai as chu_nha_sdt 
                       FROM phong p 
                       LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
                       WHERE p.id = ?");
$stmt->execute([$phong_id]);
$phong = $stmt->fetch();

if (!$phong) {
    header('Location: index.php');
    exit;
}

// Lấy hình ảnh của phòng
$images_stmt = $pdo->prepare("SELECT * FROM anh_phong WHERE phong_id = ? ORDER BY la_anh_chinh DESC, id ASC");
$images_stmt->execute([$phong_id]);
$images = $images_stmt->fetchAll();

// Lấy tiện ích của phòng
$tien_ich_stmt = $pdo->prepare("SELECT ti.* FROM tien_ich ti 
                                 INNER JOIN phong_tien_ich pti ON ti.id = pti.tien_ich_id 
                                 WHERE pti.phong_id = ?");
$tien_ich_stmt->execute([$phong_id]);
$tien_ich_list = $tien_ich_stmt->fetchAll();

$user = getCurrentUser();
$is_chu_nha = isChuNha();
$is_admin = isAdmin(); // Kiểm tra Admin
$is_owner = isLoggedIn() && $phong['nguoi_dung_id'] == $user['id']; // Kiểm tra có phải chủ phòng này không

// Kiểm tra xem người dùng có booking đã được xác nhận cho phòng này không
$has_confirmed_booking = false;
if (isLoggedIn() && $user && !$is_owner && !$is_admin) {
    $booking_stmt = $pdo->prepare("SELECT COUNT(*) as count FROM dat_phong 
                                   WHERE phong_id = ? AND nguoi_dung_id = ? AND trang_thai = 'da_xac_nhan'");
    $booking_stmt->execute([$phong_id, $user['id']]);
    $has_confirmed_booking = $booking_stmt->fetch()['count'] > 0;
}

// Kiểm tra yêu thích (Admin và Chủ phòng không cần chức năng này)
$is_favorite = false;
if (isLoggedIn() && $user && !$is_owner && !$is_admin) {
    $fav_stmt = $pdo->prepare("SELECT COUNT(*) as count FROM yeu_thich WHERE nguoi_dung_id = ? AND phong_id = ?");
    $fav_stmt->execute([$user['id'], $phong_id]);
    $is_favorite = $fav_stmt->fetch()['count'] > 0;
}

// Lấy thông báo lỗi nếu có
$error_message = $_GET['error'] ?? '';
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($phong['tieu_de']); ?> - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
    <?php include 'includes/header.php'; ?>
    
    <div class="container">
            <div class="room-detail">
            <a href="index.php" class="back-link">← Quay lại danh sách</a>
            
            <?php if ($error_message === 'own_room'): ?>
                <div class="alert alert-error">Bạn không thể đặt phòng của chính mình!</div>
            <?php endif; ?>
            
            <div class="room-detail-header">
                <h1><?php echo htmlspecialchars($phong['tieu_de']); ?></h1>
                <div class="header-actions">
                    <span class="room-status status-<?php echo $phong['trang_thai']; ?>">
                        <?php echo getTrangThaiPhongText($phong['trang_thai']); ?>
                    </span>
                    <?php if (isLoggedIn() && !$is_owner && !$is_admin && !$is_chu_nha): ?>
                        <button class="favorite-btn-large <?php echo $is_favorite ? 'active' : ''; ?>" 
                                onclick="toggleFavorite(<?php echo $phong_id; ?>, this, {changeInnerHTML: true})" 
                                title="<?php echo $is_favorite ? 'Bỏ yêu thích' : 'Yêu thích'; ?>">
                            <?php echo $is_favorite ? '❤️ Đã yêu thích' : '🤍 Yêu thích'; ?>
                        </button>
                    <?php endif; ?>
                </div>
            </div>
            
            <!-- Hình ảnh -->
            <?php if (!empty($images)): ?>
                <div class="room-images">
                    <div class="main-image">
                        <img id="mainImage" src="<?php echo htmlspecialchars($images[0]['duong_dan_anh']); ?>" 
                             alt="<?php echo htmlspecialchars($phong['tieu_de']); ?>"
                             onerror="this.src='assets/images/no-image.jpg'">
                    </div>
                    <?php if (count($images) > 1): ?>
                        <div class="thumbnail-images">
                            <?php foreach ($images as $index => $image): ?>
                                <img src="<?php echo htmlspecialchars($image['duong_dan_anh']); ?>" 
                                     alt="Hình ảnh <?php echo $index + 1; ?>"
                                     onclick="changeMainImage('<?php echo htmlspecialchars($image['duong_dan_anh']); ?>')"
                                     class="<?php echo $index === 0 ? 'active' : ''; ?>"
                                     onerror="this.src='assets/images/no-image.jpg'">
                            <?php endforeach; ?>
                        </div>
                    <?php endif; ?>
                </div>
            <?php else: ?>
                <div class="room-images">
                    <div class="main-image">
                        <img src="assets/images/no-image.jpg" alt="Không có hình ảnh">
                    </div>
                </div>
            <?php endif; ?>
            
            <div class="room-detail-content">
                <div class="room-info-section">
                    <!-- Thông tin giá -->
                    <div class="info-box">
                        <h3>💰 Giá thuê</h3>
                        <p class="room-price-large"><?php echo number_format($phong['gia']); ?> đ/tháng</p>
                    </div>
                    
                    <!-- Thông tin cơ bản -->
                    <div class="info-box">
                        <h3>📋 Thông tin phòng</h3>
                        <ul>
                            <li><strong>Địa chỉ:</strong> <?php echo htmlspecialchars($phong['dia_chi']); ?></li>
                            <?php if ($phong['quan_huyen']): ?>
                                <li><strong>Quận/Huyện:</strong> <?php echo htmlspecialchars($phong['quan_huyen']); ?></li>
                            <?php endif; ?>
                            <?php if ($phong['dien_tich']): ?>
                                <li><strong>Diện tích:</strong> <?php echo $phong['dien_tich']; ?> m²</li>
                            <?php endif; ?>
                            <li><strong>Trạng thái:</strong> 
                                <?php echo getTrangThaiPhongText($phong['trang_thai']); ?>
                            </li>
                            <?php 
                            // Chỉ hiển thị tên chủ nhà nếu:
                            // 1. Người xem là chủ nhà, Admin
                            // 2. Hoặc người thuê đã có booking được xác nhận
                            if ($phong['chu_nha_ten'] && ($is_owner || $has_confirmed_booking || $is_admin)): 
                            ?>
                                <li><strong>Chủ nhà:</strong> <?php echo htmlspecialchars($phong['chu_nha_ten']); ?></li>
                            <?php endif; ?>
                        </ul>
                    </div>
                    
                    <!-- Tiện ích -->
                    <?php if (!empty($tien_ich_list)): ?>
                        <div class="info-box">
                            <h3>🏠 Tiện ích</h3>
                            <div class="amenities-list">
                                <?php foreach ($tien_ich_list as $tien_ich): ?>
                                    <span class="amenity-item">
                                        <?php if ($tien_ich['icon']): ?>
                                            <span class="amenity-icon"><?php echo htmlspecialchars($tien_ich['icon']); ?></span>
                                        <?php endif; ?>
                                        <?php echo htmlspecialchars($tien_ich['ten']); ?>
                                    </span>
                                <?php endforeach; ?>
                            </div>
                        </div>
                    <?php endif; ?>
                    
                    <!-- Mô tả -->
                    <?php if ($phong['mo_ta']): ?>
                        <div class="info-box">
                            <h3>📝 Mô tả</h3>
                            <p><?php echo nl2br(htmlspecialchars($phong['mo_ta'])); ?></p>
                        </div>
                    <?php endif; ?>
                    
                    <?php if ($is_owner || $has_confirmed_booking || $is_admin): ?>
                        <div class="info-box">
                            <h3>📞 Thông tin liên hệ chủ nhà</h3>
                            <ul>
                                <?php if ($phong['chu_nha_ten']): ?>
                                    <li><strong>Tên chủ nhà:</strong> <?php echo htmlspecialchars($phong['chu_nha_ten']); ?></li>
                                <?php endif; ?>
                                <?php if ($phong['chu_nha_sdt']): ?>
                                    <li><strong>Điện thoại:</strong> <?php echo htmlspecialchars($phong['chu_nha_sdt']); ?></li>
                                <?php endif; ?>
                                <?php if ($phong['chu_nha_email']): ?>
                                    <li><strong>Email:</strong> <?php echo htmlspecialchars($phong['chu_nha_email']); ?></li>
                                <?php endif; ?>
                            </ul>
                            <?php if ($is_admin): ?>
                                <p><em>👮‍♂️ (Thông tin hiển thị cho Admin)</em></p>
                            <?php elseif ($has_confirmed_booking): ?>
                                <p><em>✅ (Hiển thị vì đơn đặt phòng đã được xác nhận)</em></p>
                            <?php else: ?>
                                <p><em>(Thông tin của bạn)</em></p>
                            <?php endif; ?>
                        </div>
                    <?php else: ?>
                        <div class="info-box">
                            <h3>📞 Thông tin liên hệ</h3>
                            <p><em>🔒 Thông tin chủ nhà sẽ được hiển thị sau khi đơn đặt phòng của bạn được xác nhận.</em></p>
                        </div>
                    <?php endif; ?>
                    
                    <!-- Nút đặt phòng - Hiển thị khác nhau tùy trạng thái -->
                    <?php if ($phong['trang_thai'] === 'da_thue'): ?>
                        <div class="info-box">
                            <p class="alert alert-warning">⚠️ Phòng này đã được thuê. Vui lòng tìm phòng khác.</p>
                        </div>
                    <?php elseif (!isLoggedIn()): ?>
                        <!-- Người chưa đăng nhập -->
                        <div class="info-box">
                            <p class="alert alert-info">ℹ️ Bạn cần đăng nhập để đặt phòng.</p>
                            <div style="margin-top: 15px;">
                                <a href="auth/login.php" class="btn btn-primary">Đăng nhập</a>
                                <a href="auth/register.php" class="btn btn-secondary">Đăng ký tài khoản</a>
                            </div>
                        </div>
                    <?php elseif (isLoggedIn() && $is_owner): ?>
                        <div class="info-box">
                            <p class="alert alert-info">ℹ️ Đây là phòng của bạn.</p>
                            <div style="margin-top: 15px;">
                                <a href="admin/edit_room.php?id=<?php echo $phong_id; ?>" class="btn btn-warning">Chỉnh sửa phòng</a>
                                <a href="admin/manage_rooms.php" class="btn btn-secondary">Quản lý phòng</a>
                            </div>
                        </div>

                    <?php elseif (isLoggedIn() && $is_admin): ?>
                        <div class="info-box">
                            <p class="alert alert-danger" style="background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;">
                                🛡️ <strong>Góc nhìn Admin:</strong> Bạn không thể đặt phòng với tài khoản quản trị.
                            </p>
                            <div style="margin-top: 15px;">
                                <a href="admin/all_rooms.php" class="btn btn-secondary">Về trang quản lý</a>
                                <button onclick="confirmAdminDelete(<?php echo $phong_id; ?>)" class="btn btn-danger">Xóa bài đăng này</button>
                            </div>
                        </div>
                        
                    <?php elseif (isLoggedIn() && $is_chu_nha): ?>
                        <div class="info-box">
                            <p class="alert alert-warning">🚫 Tài khoản <strong>Chủ nhà</strong> không có chức năng đặt phòng. Vui lòng sử dụng tài khoản Người thuê.</p>
                        </div>
                        
                    <?php elseif (isLoggedIn() && $phong['trang_thai'] === 'con_trong'): ?>
                        <div class="info-box booking-box">
                            <h3 style="margin-top: 0; color: #27ae60;">✅ Phòng còn trống</h3>
                            <p><strong>Bạn muốn đặt phòng này?</strong></p>
                            <p style="color: #666; font-size: 14px;">Sau khi đặt phòng, chủ nhà sẽ xem và xác nhận đơn đặt của bạn.</p>
                            <div style="margin-top: 20px;">
                                <a href="booking.php?phong_id=<?php echo $phong_id; ?>" class="btn btn-success btn-large">📞 Đặt phòng ngay</a>
                            </div>
                        </div>
                    <?php endif; ?>
                </div>
            </div>
            
            <!-- Nút quản lý cho Chủ phòng -->
            <?php if ($is_owner): ?>
                <div class="admin-actions">
                    <a href="admin/edit_room.php?id=<?php echo $phong_id; ?>" class="btn btn-warning">Chỉnh sửa phòng</a>
                    <a href="admin/manage_rooms.php" class="btn btn-secondary">Quản lý phòng</a>
                </div>
            <?php endif; ?>
        </div>
    </div>
    
    <?php include 'includes/footer.php'; ?>
    
    <script src="assets/js/favorites.js"></script>
    <script src="assets/js/room-images.js"></script>
    
    <script>
        function confirmAdminDelete(id) {
            Swal.fire({
                title: 'Admin xóa bài này?',
                text: "Bạn có chắc chắn muốn xóa phòng trọ này?",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: 'Xóa ngay',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = 'admin/delete_room_admin.php?id=' + id;
                }
            })
        }
    </script>
</body>
</html>
