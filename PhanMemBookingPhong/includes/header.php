<?php
// Đảm bảo config đã được load
if (!defined('BASE_URL')) {
    // Xác định đường dẫn config tùy thuộc vào vị trí file hiện tại
    $script_dir = dirname($_SERVER['SCRIPT_FILENAME']);
    $header_dir = __DIR__;
    
    // Nếu header.php được include từ thư mục gốc
    if (basename($script_dir) !== 'admin' && basename($script_dir) !== 'auth') {
        require_once 'config/config.php';
        require_once 'config/database.php';
    } else {
        // Nếu header.php được include từ thư mục admin hoặc auth
        require_once __DIR__ . '/../config/config.php';
        require_once __DIR__ . '/../config/database.php';
    }
}

$user = getCurrentUser();
$admin = getCurrentAdmin();

// Xác định vị trí file hiện tại để highlight menu
$script_path = $_SERVER['SCRIPT_NAME'];
$is_admin = (strpos($script_path, '/admin/') !== false || strpos($script_path, '\\admin\\') !== false);
$is_auth = (strpos($script_path, '/auth/') !== false || strpos($script_path, '\\auth\\') !== false);

// Lấy đường dẫn hiện tại để highlight menu
$current_path = $_SERVER['REQUEST_URI'];
$current_file = basename($_SERVER['PHP_SELF']);

// Kiểm tra các trang active
$is_home = ($current_file === 'index.php' && !$is_admin);
$is_my_bookings = ($current_file === 'my_bookings.php');
$is_favorites = ($current_file === 'favorites.php');
$is_booking = ($current_file === 'booking.php');
$is_room_detail = ($current_file === 'room_detail.php');
$is_add_room = ($current_file === 'add_room.php' || strpos($current_path, 'add_room') !== false);
$is_admin_index = ($current_file === 'index.php' && $is_admin);
$is_admin_rooms = (($current_file === 'rooms.php' || $current_file === 'manage_rooms.php') && $is_admin);
$is_admin_bookings = ($current_file === 'bookings.php' && $is_admin);
$is_admin_edit_room = ($current_file === 'edit_room.php' && $is_admin);
// Removed: $is_admin_users - file users.php đã được thay thế bằng manage_chu_nha.php và manage_nguoi_thue.php

// Dùng BASE_URL cho tất cả link để đảm bảo đúng
// Xử lý khoảng trống trong tên thư mục bằng cách encode
if (defined('BASE_URL')) {
    $base = BASE_URL;
} else {
    // Tự động detect BASE_URL nếu chưa có
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    $script_name = dirname($_SERVER['SCRIPT_NAME']);
    // Loại bỏ admin hoặc auth khỏi đường dẫn
    $script_name = preg_replace('#/(admin|auth)/?$#', '', $script_name);
    $base = $protocol . '://' . $host . $script_name . '/';
}
?>
<header class="header">
    <div class="container">
        <!-- Hàng trên: User section ở góc phải -->
        <?php if (isLoggedIn()): ?>
            <div class="header-top">
                <div class="header-user-section">
                    <span class="user-info">
                        <?php if (isAdmin() && $admin): ?>
                            Xin chào, <strong><?php echo htmlspecialchars($admin['ho_ten']); ?></strong>
                            <span class="badge" style="background: #dc3545; color: white; padding: 3px 8px; border-radius: 4px; margin-left: 5px;">🛡️ Admin</span>
                        <?php elseif ($user): ?>
                            Xin chào, <strong><?php echo htmlspecialchars($user['ho_ten']); ?></strong>
                            <?php if (isChuNha()): ?>
                                <span class="badge badge-admin">🏠 Chủ nhà</span>
                            <?php endif; ?>
                        <?php endif; ?>
                    </span>
                    <a href="<?php echo $base; ?>auth/logout.php" class="btn btn-outline">Đăng xuất</a>
                </div>
            </div>
        <?php endif; ?>
        
        <!-- Hàng dưới: Logo bên trái, Navigation menu căn giữa -->
        <div class="header-content">
            <div class="logo">
                <a href="<?php echo $base; ?>index.php">
                    <h2>🏠 Booking Phòng Trọ</h2>
                </a>
            </div>
            
            <nav class="nav">
                <a href="<?php echo $base; ?>index.php" class="nav-link <?php echo $is_home ? 'active' : ''; ?>">Trang chủ</a>
                <?php if (isLoggedIn()): ?>
                    <?php if (isAdmin()): ?>
                        <!-- Menu cho Admin -->
                        <a href="<?php echo $base; ?>admin/dashboard.php" class="nav-link <?php echo $is_admin_index ? 'active' : ''; ?>">Quản lý tổng</a>
                        <a href="<?php echo $base; ?>admin/manage_phong.php" class="nav-link">Quản lý phòng</a>
                        <a href="<?php echo $base; ?>admin/manage_chu_nha.php" class="nav-link">Quản lý chủ nhà</a>
                        <a href="<?php echo $base; ?>admin/manage_nguoi_thue.php" class="nav-link">Quản lý người thuê</a>
                    <?php elseif (isChuNha()): ?>
                        <!-- Menu cho Chủ nhà -->
                        <a href="<?php echo $base; ?>admin/manage_rooms.php" class="nav-link btn btn-primary <?php echo ($is_add_room || $is_admin_rooms || $is_admin_edit_room) ? 'active' : ''; ?>" style="padding: 5px 15px; font-size: 14px;">+ Đăng phòng</a>
                        <a href="<?php echo $base; ?>admin/bookings.php" class="nav-link <?php echo $is_admin_bookings ? 'active' : ''; ?>">Đơn đặt phòng</a>
                        <a href="<?php echo $base; ?>admin/index.php" class="nav-link <?php echo $is_admin_index ? 'active' : ''; ?>">Thống kê</a>
                    <?php else: ?>
                        <!-- Menu cho Người thuê -->
                        <a href="<?php echo $base; ?>my_bookings.php" class="nav-link <?php echo $is_my_bookings ? 'active' : ''; ?>">Đặt phòng của tôi</a>
                        <a href="<?php echo $base; ?>favorites.php" class="nav-link <?php echo $is_favorites ? 'active' : ''; ?>">Yêu thích</a>
                    <?php endif; ?>
                <?php else: ?>
                    <a href="<?php echo $base; ?>auth/login.php" class="nav-link <?php echo $current_file === 'login.php' ? 'active' : ''; ?>">Đăng nhập</a>
                    <a href="<?php echo $base; ?>auth/register.php" class="nav-link btn btn-primary <?php echo $current_file === 'register.php' ? 'active' : ''; ?>">Đăng ký</a>
                <?php endif; ?>
            </nav>
        </div>
    </div>
</header>
