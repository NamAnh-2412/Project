<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireAdmin();

$pdo = getDBConnection();
$admin = getCurrentAdmin();

// Thống kê tổng quan
$stats = [];

// Tổng số phòng
$stmt = $pdo->query("SELECT COUNT(*) as total FROM phong");
$result = $stmt->fetch();
$stats['total_rooms'] = $result ? $result['total'] : 0;

// Phòng còn trống
$stmt = $pdo->query("SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'con_trong'");
$result = $stmt->fetch();
$stats['available_rooms'] = $result ? $result['total'] : 0;

// Phòng đã thuê
$stmt = $pdo->query("SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'da_thue'");
$result = $stmt->fetch();
$stats['rented_rooms'] = $result ? $result['total'] : 0;

// Tổng số booking
$stmt = $pdo->query("SELECT COUNT(*) as total FROM dat_phong");
$result = $stmt->fetch();
$stats['total_bookings'] = $result ? $result['total'] : 0;

// Booking chờ xác nhận
$stmt = $pdo->query("SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'cho_xac_nhan'");
$result = $stmt->fetch();
$stats['pending_bookings'] = $result ? $result['total'] : 0;

// Booking đã xác nhận
$stmt = $pdo->query("SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'da_xac_nhan'");
$result = $stmt->fetch();
$stats['confirmed_bookings'] = $result ? $result['total'] : 0;

// Tổng số người thuê
$stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'nguoi_thue'");
$result = $stmt->fetch();
$stats['total_nguoi_thue'] = $result ? $result['total'] : 0;

// Tổng số chủ nhà
$stmt = $pdo->query("SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'chu_nha'");
$result = $stmt->fetch();
$stats['total_chu_nha'] = $result ? $result['total'] : 0;

// Tổng số admin
$stmt = $pdo->query("SELECT COUNT(*) as total FROM admin WHERE trang_thai = 'active'");
$result = $stmt->fetch();
$stats['total_admin'] = $result ? $result['total'] : 0;
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý tổng - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
    <style>
        .dashboard-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
            transition: transform 0.3s;
        }
        .stat-card:hover {
            transform: translateY(-5px);
        }
        .stat-card h3 {
            margin: 0 0 15px 0;
            color: #666;
            font-size: 14px;
            text-transform: uppercase;
        }
        .stat-number {
            font-size: 3em;
            font-weight: bold;
            margin: 0;
            color: #333;
        }
        .stat-card.blue { border-left: 4px solid #3498db; }
        .stat-card.green { border-left: 4px solid #27ae60; }
        .stat-card.orange { border-left: 4px solid #f39c12; }
        .stat-card.red { border-left: 4px solid #e74c3c; }
        .stat-card.purple { border-left: 4px solid #9b59b6; }
        .stat-card.teal { border-left: 4px solid #1abc9c; }
        
        .admin-menu {
            margin-top: 40px;
        }
        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }
        .menu-card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-decoration: none;
            color: #333;
            transition: all 0.3s;
            border: 2px solid transparent;
        }
        .menu-card:hover {
            border-color: #667eea;
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.15);
        }
        .menu-card h3 {
            margin: 0 0 10px 0;
            font-size: 1.5em;
        }
        .menu-card p {
            margin: 0;
            color: #666;
        }
    </style>
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div class="dashboard-header">
            <h1>🛡️ Quản lý tổng</h1>
            <p>Xin chào, <strong><?php echo htmlspecialchars($admin['ho_ten']); ?></strong> (<?php echo htmlspecialchars($admin['ten_dang_nhap']); ?>)</p>
            <p style="margin: 5px 0; opacity: 0.9;">Vai trò: <strong><?php echo $admin['vai_tro'] === 'super_admin' ? 'Super Admin' : 'Admin'; ?></strong></p>
        </div>
        
        <!-- Thống kê tổng quan -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <h3>🏠 Tổng số phòng</h3>
                <p class="stat-number"><?php echo $stats['total_rooms']; ?></p>
            </div>
            <div class="stat-card green">
                <h3>✅ Phòng còn trống</h3>
                <p class="stat-number"><?php echo $stats['available_rooms']; ?></p>
            </div>
            <div class="stat-card orange">
                <h3>🔒 Phòng đã thuê</h3>
                <p class="stat-number"><?php echo $stats['rented_rooms']; ?></p>
            </div>
            <div class="stat-card purple">
                <h3>📋 Tổng số booking</h3>
                <p class="stat-number"><?php echo $stats['total_bookings']; ?></p>
            </div>
            <div class="stat-card teal">
                <h3>⏳ Booking chờ xác nhận</h3>
                <p class="stat-number"><?php echo $stats['pending_bookings']; ?></p>
            </div>
            <div class="stat-card blue">
                <h3>👥 Tổng số người thuê</h3>
                <p class="stat-number"><?php echo $stats['total_nguoi_thue']; ?></p>
            </div>
            <div class="stat-card green">
                <h3>🏠 Tổng số chủ nhà</h3>
                <p class="stat-number"><?php echo $stats['total_chu_nha']; ?></p>
            </div>
            <div class="stat-card red">
                <h3>🛡️ Tổng số Admin</h3>
                <p class="stat-number"><?php echo $stats['total_admin']; ?></p>
            </div>
        </div>
        
        <!-- Menu quản lý -->
        <div class="admin-menu">
            <h2>Quản lý hệ thống</h2>
            <div class="menu-grid">
                <a href="manage_chu_nha.php" class="menu-card">
                    <h3>🏠 Quản lý Chủ nhà</h3>
                    <p>Xem danh sách, thêm, sửa, xóa chủ nhà (<?php echo $stats['total_chu_nha']; ?> chủ nhà)</p>
                </a>
                <a href="manage_nguoi_thue.php" class="menu-card">
                    <h3>👥 Quản lý Người thuê</h3>
                    <p>Xem danh sách, thêm, sửa, xóa người thuê (<?php echo $stats['total_nguoi_thue']; ?> người thuê)</p>
                </a>
                <a href="manage_phong.php" class="menu-card">
                    <h3>🏘️ Quản lý Phòng</h3>
                    <p>Xem danh sách, thêm, sửa, xóa phòng trọ (<?php echo $stats['total_rooms']; ?> phòng)</p>
                </a>
                <a href="manage_bookings.php" class="menu-card">
                    <h3>📋 Quản lý Booking</h3>
                    <p>Xem và quản lý tất cả đơn đặt phòng (<?php echo $stats['total_bookings']; ?> booking)</p>
                </a>
            </div>
        </div>
    </div>
    
    <?php include '../includes/footer.php'; ?>
</body>
</html>

