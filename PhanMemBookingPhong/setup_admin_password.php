<?php
/**
 * Script cập nhật mật khẩu admin từ plain text sang hash
 * 
 * CHÚ Ý: File này chỉ dùng một lần sau khi import database
 * Sau khi sử dụng xong, nên xóa hoặc comment file này để bảo mật
 * 
 * Truy cập: http://localhost/PhanMemBookingPhong/setup_admin_password.php
 */

// Uncomment dòng dưới để vô hiệu hóa file này sau khi đã setup xong
// die('File này đã được vô hiệu hóa. Vui lòng xóa file nếu không cần thiết.');

require_once 'config/database.php';

// Mật khẩu từ file SQL (plain text)
$plain_password = '193850091011';

// Hash mật khẩu
$hashed_password = password_hash($plain_password, PASSWORD_DEFAULT);

try {
    $pdo = getDBConnection();
    
    // Cập nhật mật khẩu admin
    $stmt = $pdo->prepare("UPDATE admin SET mat_khau = ? WHERE ten_dang_nhap = 'admin'");
    $stmt->execute([$hashed_password]);
    
    echo "<h2>✅ Cập nhật mật khẩu Admin thành công!</h2>";
    echo "<div style='background: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0;'>";
    echo "<h3>Thông tin đăng nhập Admin:</h3>";
    echo "<p><strong>Tên đăng nhập:</strong> <code>admin</code></p>";
    echo "<p><strong>Mật khẩu:</strong> <code>193850091011</code></p>";
    echo "<p><strong>Email:</strong> admin@bookingphong.com</p>";
    echo "<p><strong>Vai trò:</strong> Super Admin</p>";
    echo "</div>";
    
    echo "<div style='background: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0;'>";
    echo "<strong>⚠️ LƯU Ý:</strong> Vui lòng xóa hoặc vô hiệu hóa file này sau khi sử dụng!";
    echo "</div>";
    
    echo "<br><a href='auth/login.php' style='display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px;'>Đăng nhập ngay</a>";
    
} catch (Exception $e) {
    echo "<div style='background: #f8d7da; padding: 20px; border-radius: 8px; color: #721c24;'>";
    echo "<h3>❌ Lỗi:</h3>";
    echo "<p>" . htmlspecialchars($e->getMessage()) . "</p>";
    echo "</div>";
}
?>

