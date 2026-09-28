<?php
require_once 'config/database.php';
require_once 'config/config.php';

header('Content-Type: application/json');

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'message' => 'Vui lòng đăng nhập']);
    exit;
}

$user = getCurrentUser();
if (!$user) {
    echo json_encode(['success' => false, 'message' => 'Vui lòng đăng nhập']);
    exit;
}

// Chỉ Người thuê mới được yêu thích phòng
if (!isNguoiThue()) {
    echo json_encode(['success' => false, 'message' => 'Chỉ Người thuê mới có thể yêu thích phòng']);
    exit;
}

$phong_id = $_POST['phong_id'] ?? 0;
$action = $_POST['action'] ?? '';

if (empty($phong_id) || $action !== 'toggle') {
    echo json_encode(['success' => false, 'message' => 'Thông tin không hợp lệ']);
    exit;
}

try {
    $pdo = getDBConnection();
    
    // Kiểm tra không được yêu thích phòng của chính mình
    $check_owner_stmt = $pdo->prepare("SELECT nguoi_dung_id FROM phong WHERE id = ?");
    $check_owner_stmt->execute([$phong_id]);
    $phong_owner = $check_owner_stmt->fetch();
    if ($phong_owner && $phong_owner['nguoi_dung_id'] == $user['id']) {
        echo json_encode(['success' => false, 'message' => 'Bạn không thể yêu thích phòng của chính mình']);
        exit;
    }
    
    // Kiểm tra phòng có tồn tại không
    $stmt = $pdo->prepare("SELECT id FROM phong WHERE id = ?");
    $stmt->execute([$phong_id]);
    if (!$stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Phòng không tồn tại']);
        exit;
    }
    
    // Kiểm tra đã yêu thích chưa
    $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM yeu_thich WHERE nguoi_dung_id = ? AND phong_id = ?");
    $stmt->execute([$user['id'], $phong_id]);
    $is_favorite = $stmt->fetch()['count'] > 0;
    
    if ($is_favorite) {
        // Bỏ yêu thích
        $stmt = $pdo->prepare("DELETE FROM yeu_thich WHERE nguoi_dung_id = ? AND phong_id = ?");
        $stmt->execute([$user['id'], $phong_id]);
        echo json_encode(['success' => true, 'favorited' => false, 'message' => 'Đã bỏ yêu thích']);
    } else {
        // Thêm yêu thích
        $stmt = $pdo->prepare("INSERT INTO yeu_thich (nguoi_dung_id, phong_id) VALUES (?, ?)");
        $stmt->execute([$user['id'], $phong_id]);
        echo json_encode(['success' => true, 'favorited' => true, 'message' => 'Đã thêm vào yêu thích']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Có lỗi xảy ra: ' . $e->getMessage()]);
}
?>

