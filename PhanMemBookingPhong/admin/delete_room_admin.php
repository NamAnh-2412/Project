<?php
// FILE: admin/delete_room_admin.php
require_once '../config/database.php';
require_once '../config/config.php';

// Chỉ Admin mới được chạy file này
requireAdmin();

$id = $_GET['id'] ?? 0;

if ($id) {
    $pdo = getDBConnection();
    try {
        // 1. Xóa các dữ liệu liên quan (ảnh, booking...) nếu database chưa set Cascade
        // Xóa ảnh phòng
        $stmt = $pdo->prepare("DELETE FROM anh_phong WHERE phong_id = ?");
        $stmt->execute([$id]);
        
        // Xóa booking liên quan
        $stmt = $pdo->prepare("DELETE FROM dat_phong WHERE phong_id = ?");
        $stmt->execute([$id]);

        // 2. Xóa phòng chính
        $stmt = $pdo->prepare("DELETE FROM phong WHERE id = ?");
        $stmt->execute([$id]);
        
        echo "<script>
            alert('Admin đã xóa bài đăng thành công!');
            window.location.href = 'all_rooms.php';
        </script>";
    } catch (PDOException $e) {
        echo "<script>
            alert('Lỗi khi xóa phòng: " . addslashes($e->getMessage()) . "');
            window.location.href = 'all_rooms.php';
        </script>";
    }
} else {
    header('Location: all_rooms.php');
}
?>

