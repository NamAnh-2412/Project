<?php
// FILE: admin/delete_user.php
require_once '../config/database.php';
require_once '../config/config.php';
requireAdmin(); // Bảo vệ file

$id = $_GET['id'] ?? 0;

if ($id) {
    $pdo = getDBConnection();
    try {
        // 1. Xóa các dữ liệu liên quan trước (nếu database chưa setup Cascade)
        // Xóa booking, bài đăng, ảnh... của user này (Tùy logic database của bạn)
        // Ở đây mình xóa trực tiếp user, nếu DB có ràng buộc khóa ngoại (Foreign Key) thì nó sẽ báo lỗi hoặc tự xóa theo.
        
        $stmt = $pdo->prepare("DELETE FROM nguoi_dung WHERE id = ?");
        $stmt->execute([$id]);
        
        echo "<script>
            alert('Đã xóa thành công!');
            window.location.href = 'users.php';
        </script>";
    } catch (PDOException $e) {
        // Lỗi thường gặp: User này đang có bài đăng hoặc booking, không xóa ngay được
        echo "<script>
            alert('Không thể xóa: Người dùng này đang có dữ liệu liên quan (Phòng trọ/Đơn đặt). Hãy xóa dữ liệu đó trước.');
            window.location.href = 'users.php';
        </script>";
    }
} else {
    header('Location: users.php');
}
?>

