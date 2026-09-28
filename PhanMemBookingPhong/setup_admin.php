<?php
/**
 * Script tạo tài khoản Admin
 * 
 * CHÚ Ý: File này chỉ dùng một lần để tạo tài khoản admin đầu tiên
 * Sau khi sử dụng xong, nên xóa hoặc comment file này để bảo mật
 * 
 * Truy cập: http://localhost/PhanMemBookingPhong/setup_admin.php
 */

// Uncomment dòng dưới để vô hiệu hóa file này sau khi đã setup xong
// die('File này đã được vô hiệu hóa. Vui lòng xóa file nếu không cần thiết.');

require_once 'config/database.php';

$ten_dang_nhap = 'admin';
$password = 'admin123';
$ho_ten = 'Administrator';
$email = 'admin@booking.com';
$hashed_password = password_hash($password, PASSWORD_DEFAULT);

try {
    $pdo = getDBConnection();
    
    // Kiểm tra xem đã có admin chưa
    $check_stmt = $pdo->prepare("SELECT id FROM admin WHERE ten_dang_nhap = ?");
    $check_stmt->execute([$ten_dang_nhap]);
    $existing = $check_stmt->fetch();
    
    if ($existing) {
        // Nếu đã có, cập nhật
        $stmt = $pdo->prepare("UPDATE admin SET mat_khau = ?, ho_ten = ?, email = ?, vai_tro = 'super_admin', trang_thai = 'active' WHERE ten_dang_nhap = ?");
        $stmt->execute([$hashed_password, $ho_ten, $email, $ten_dang_nhap]);
        
        echo "✅ Tài khoản admin đã được cập nhật thành công!<br>";
    } else {
        // Nếu chưa có, tạo mới trong bảng admin
        $stmt = $pdo->prepare("INSERT INTO admin (ten_dang_nhap, mat_khau, ho_ten, email, vai_tro, trang_thai) VALUES (?, ?, ?, ?, 'super_admin', 'active')");
        $stmt->execute([$ten_dang_nhap, $hashed_password, $ho_ten, $email]);
        
        echo "✅ Tài khoản admin đã được tạo thành công!<br>";
    }
    
    echo "<br><strong>Thông tin đăng nhập:</strong><br>";
    echo "Tên đăng nhập: <strong>{$ten_dang_nhap}</strong><br>";
    echo "Password: <strong>{$password}</strong><br>";
    echo "Email: <strong>{$email}</strong><br>";
    echo "<br><strong>⚠️ LƯU Ý: Vui lòng xóa hoặc vô hiệu hóa file này sau khi sử dụng!</strong><br>";
    echo "<br><a href='auth/login.php'>Đăng nhập ngay</a>";
} catch (Exception $e) {
    echo "❌ Lỗi: " . $e->getMessage();
}
?>

