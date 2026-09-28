<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireLogin();
requireChuNha(); // Chỉ Chủ nhà mới được quản lý booking

$pdo = getDBConnection();
$user = getCurrentUser();

// Xử lý duyệt/từ chối booking
if (isset($_POST['action']) && isset($_POST['booking_id'])) {
    $booking_id = $_POST['booking_id'];
    $action = $_POST['action'];
    
    // Kiểm tra booking thuộc về phòng của chủ nhà này
    $check_stmt = $pdo->prepare("SELECT dp.* FROM dat_phong dp 
                                  INNER JOIN phong p ON dp.phong_id = p.id 
                                  WHERE dp.id = ? AND p.nguoi_dung_id = ?");
    $check_stmt->execute([$booking_id, $user['id']]);
    $booking = $check_stmt->fetch();
    
    if ($booking) {
        if ($action === 'approve') {
            $stmt = $pdo->prepare("UPDATE dat_phong SET trang_thai = 'da_xac_nhan' WHERE id = ?");
            $stmt->execute([$booking_id]);
            
            // Cập nhật trạng thái phòng thành đã thuê
            $stmt = $pdo->prepare("UPDATE phong SET trang_thai = 'da_thue' WHERE id = (SELECT phong_id FROM dat_phong WHERE id = ?)");
            $stmt->execute([$booking_id]);
        } elseif ($action === 'reject') {
            $stmt = $pdo->prepare("UPDATE dat_phong SET trang_thai = 'da_huy' WHERE id = ?");
            $stmt->execute([$booking_id]);
        }
    }
    
    header('Location: bookings.php');
    exit;
}

// Lấy danh sách bookings cho phòng của chủ nhà này (LOẠI BỎ các đơn đã hủy)
$stmt = $pdo->prepare("SELECT dp.*, 
                              p.tieu_de as phong_tieu_de, 
                              p.dia_chi as phong_dia_chi,
                              p.gia as phong_gia,
                              p.id as phong_id,
                              nd.ho_ten as khach_hang_ten, 
                              nd.so_dien_thoai as khach_hang_sdt,
                              nd.email as khach_hang_email
                       FROM dat_phong dp 
                       INNER JOIN phong p ON dp.phong_id = p.id 
                       INNER JOIN nguoi_dung nd ON dp.nguoi_dung_id = nd.id
                       WHERE p.nguoi_dung_id = ? AND dp.trang_thai != 'da_huy'
                       ORDER BY dp.nguoi_dung_id, dp.ngay_tao DESC");
$stmt->execute([$user['id']]);
$all_bookings = $stmt->fetchAll();

// Group bookings theo khách hàng và loại bỏ các booking đã hủy
$grouped_bookings = [];
foreach ($all_bookings as $booking) {
    // Bỏ qua các booking đã hủy
    if ($booking['trang_thai'] === 'da_huy') {
        continue;
    }
    
    $customer_id = $booking['nguoi_dung_id'];
    if (!isset($grouped_bookings[$customer_id])) {
        $grouped_bookings[$customer_id] = [
            'customer_info' => [
                'id' => $customer_id,
                'ten' => $booking['khach_hang_ten'],
                'email' => $booking['khach_hang_email'],
                'sdt' => $booking['khach_hang_sdt']
            ],
            'bookings' => []
        ];
    }
    $grouped_bookings[$customer_id]['bookings'][] = $booking;
}

// Đếm số lượng booking theo trạng thái
$stats = [
    'cho_xac_nhan' => 0,
    'da_xac_nhan' => 0,
    'total' => 0
];

foreach ($all_bookings as $booking) {
    if ($booking['trang_thai'] === 'cho_xac_nhan') {
        $stats['cho_xac_nhan']++;
    } elseif ($booking['trang_thai'] === 'da_xac_nhan') {
        $stats['da_xac_nhan']++;
    }
    $stats['total']++;
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đặt phòng - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div class="bookings-page">
            <h1 class="page-title">📋 Quản lý đặt phòng</h1>
            
            <!-- Thống kê -->
            <div class="stats-bar">
                <div class="stat-badge pending">
                    <span class="stat-label">Chờ xác nhận:</span>
                    <span class="stat-value"><?php echo $stats['cho_xac_nhan']; ?></span>
                </div>
                <div class="stat-badge confirmed">
                    <span class="stat-label">Đã xác nhận:</span>
                    <span class="stat-value"><?php echo $stats['da_xac_nhan']; ?></span>
                </div>
                <div class="stat-badge">
                    <span class="stat-label">Tổng đơn:</span>
                    <span class="stat-value"><?php echo $stats['total']; ?></span>
                </div>
            </div>
            
            <!-- Danh sách bookings -->
            <?php if (empty($grouped_bookings)): ?>
                <div class="empty-state">
                    <div class="empty-state-icon">📭</div>
                    <h3>Chưa có đơn đặt phòng nào</h3>
                    <p>Các đơn đặt phòng sẽ hiển thị ở đây khi khách hàng đặt phòng của bạn</p>
                </div>
            <?php else: ?>
                <div class="bookings-grouped">
                    <?php 
                    foreach ($grouped_bookings as $customer_id => $group): 
                        $booking_count = count($group['bookings']);
                        $customer = $group['customer_info'];
                    ?>
                        <div class="customer-booking-group">
                            <div class="customer-header" onclick="toggleCustomerDetails(<?php echo $customer_id; ?>)">
                                <div class="customer-info">
                                    <div class="customer-main">
                                        <strong><?php echo htmlspecialchars($customer['ten']); ?></strong>
                                        <span class="booking-count"><?php echo $booking_count; ?> đơn</span>
                                    </div>
                                    <div class="customer-contact">
                                        <span>📧 <?php echo htmlspecialchars($customer['email']); ?></span>
                                        <?php if ($customer['sdt']): ?>
                                            <span>📞 <?php echo htmlspecialchars($customer['sdt']); ?></span>
                                        <?php endif; ?>
                                    </div>
                                </div>
                                <div class="toggle-icon" id="icon-<?php echo $customer_id; ?>">▼</div>
                            </div>
                            
                            <div class="customer-details" id="details-<?php echo $customer_id; ?>">
                                <?php foreach ($group['bookings'] as $booking): ?>
                                    <div class="booking-item">
                                        <div class="booking-header-row">
                                            <div>
                                                <div class="booking-id">Mã đơn: #<?php echo $booking['id']; ?></div>
                                            </div>
                                            <?php 
                                            $trang_thai_info = getTrangThaiBookingInfo($booking['trang_thai']);
                                            ?>
                                            <span class="booking-status" style="background-color: <?php echo $trang_thai_info['color']; ?>; color: white;">
                                                <?php echo $trang_thai_info['text']; ?>
                                            </span>
                                        </div>
                                        
                                        <div class="booking-room-info">
                                            <div class="booking-room-title"><?php echo htmlspecialchars($booking['phong_tieu_de']); ?></div>
                                            <div class="booking-room-meta">
                                                <?php if ($booking['phong_gia']): ?>
                                                    <span class="booking-price">💰 <?php echo number_format($booking['phong_gia']); ?> đ/tháng</span>
                                                <?php endif; ?>
                                                <span class="booking-address">📍 <?php echo htmlspecialchars($booking['phong_dia_chi']); ?></span>
                                                <span>📅 <?php echo date('d/m/Y H:i', strtotime($booking['ngay_tao'])); ?></span>
                                            </div>
                                        </div>
                                        
                                        <?php if ($booking['so_dien_thoai_lien_he'] || $booking['tin_nhan']): ?>
                                            <div class="booking-extra-info">
                                                <?php if ($booking['so_dien_thoai_lien_he']): ?>
                                                    <div><strong>📞 SĐT liên hệ:</strong> <?php echo htmlspecialchars($booking['so_dien_thoai_lien_he']); ?></div>
                                                <?php endif; ?>
                                                <?php if ($booking['tin_nhan']): ?>
                                                    <div><strong>💬 Tin nhắn:</strong> <?php echo htmlspecialchars($booking['tin_nhan']); ?></div>
                                                <?php endif; ?>
                                            </div>
                                        <?php endif; ?>
                                        
                                        <div class="booking-actions">
                                            <?php if ($booking['trang_thai'] === 'cho_xac_nhan'): ?>
                                                <form method="POST" style="display: inline-block;">
                                                    <input type="hidden" name="booking_id" value="<?php echo $booking['id']; ?>">
                                                    <button type="submit" name="action" value="approve" class="btn-sm btn-success" onclick="return confirm('Xác nhận đơn đặt phòng này?')">
                                                        ✓ Xác nhận
                                                    </button>
                                                </form>
                                                <form method="POST" style="display: inline-block;">
                                                    <input type="hidden" name="booking_id" value="<?php echo $booking['id']; ?>">
                                                    <button type="submit" name="action" value="reject" class="btn-sm btn-danger" onclick="return confirm('Từ chối đơn đặt phòng này? Đơn sẽ bị hủy và không hiển thị trong danh sách nữa.')">
                                                        ✕ Từ chối
                                                    </button>
                                                </form>
                                            <?php else: ?>
                                                <span class="no-action">Đơn này đã được xử lý</span>
                                            <?php endif; ?>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php endif; ?>
        </div>
    </div>
    
    <?php include '../includes/footer.php'; ?>
    
    <script>
        function toggleCustomerDetails(customerId) {
            const details = document.getElementById('details-' + customerId);
            const icon = document.getElementById('icon-' + customerId);
            
            if (details.classList.contains('expanded')) {
                details.classList.remove('expanded');
                icon.textContent = '▼';
                icon.style.transform = 'rotate(0deg)';
            } else {
                details.classList.add('expanded');
                icon.textContent = '▲';
                icon.style.transform = 'rotate(180deg)';
            }
        }
    </script>
</body>
</html>
