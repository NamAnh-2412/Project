<?php
require_once '../config/database.php';
require_once '../config/config.php';

// Kiểm tra xem có thông tin reset password trong session không
if (!isset($_SESSION['reset_password'])) {
    header('Location: ' . BASE_URL . 'auth/forgot_password.php');
    exit;
}

$reset_info = $_SESSION['reset_password'];
$new_password = $reset_info['new_password'];

// Xóa thông tin reset khỏi session sau khi hiển thị
unset($_SESSION['reset_password']);

// Tên file ảnh background
$background_image = 'thiet-ke-noi-that-phong-ngu-bs5.jpeg.webp';
$background_image_path = __DIR__ . '/../assets/images/' . $background_image;
$background_image_url = BASE_URL . 'assets/images/' . rawurlencode($background_image);
$has_background_image = file_exists($background_image_path);
?>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mật khẩu mới - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <style>
        .auth-page {
            <?php if ($has_background_image): ?>
            background-image: url('<?php echo $background_image_url; ?>');
            <?php else: ?>
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            <?php endif; ?>
            background-size: cover;
            background-position: center;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .auth-form {
            background: rgba(255, 255, 255, 0.95);
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            max-width: 500px;
            width: 100%;
        }
        .auth-form h2 {
            margin-bottom: 25px;
            color: #333;
            text-align: center;
        }
        .success-message {
            background: #d4edda;
            color: #155724;
            padding: 20px;
            border-radius: 5px;
            border: 1px solid #c3e6cb;
            margin-bottom: 25px;
        }
        .password-display {
            background: #f8f9fa;
            border: 2px solid #667eea;
            border-radius: 5px;
            padding: 20px;
            margin: 20px 0;
            text-align: center;
        }
        .password-display label {
            display: block;
            margin-bottom: 10px;
            color: #555;
            font-weight: 500;
        }
        .password-display .password-value {
            font-size: 24px;
            font-weight: bold;
            color: #667eea;
            letter-spacing: 2px;
            padding: 10px;
            background: white;
            border-radius: 5px;
            margin: 10px 0;
            word-break: break-all;
        }
        .copy-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            margin-top: 10px;
        }
        .copy-btn:hover {
            background: #5568d3;
        }
        .warning-box {
            background: #fff3cd;
            color: #856404;
            padding: 15px;
            border-radius: 5px;
            border: 1px solid #ffeaa7;
            margin: 20px 0;
        }
        .warning-box strong {
            display: block;
            margin-bottom: 5px;
        }
        .btn-primary {
            width: 100%;
            padding: 12px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 10px;
            text-decoration: none;
            display: block;
            text-align: center;
        }
        .btn-primary:hover {
            background: #5568d3;
        }
        .text-center {
            text-align: center;
            margin-top: 20px;
        }
    </style>
</head>
<body class="auth-page">
    <div class="container">
        <div class="auth-form">
            <h2>Mật khẩu mới đã được tạo</h2>
            
            <div class="success-message">
                <strong>✓ Thành công!</strong>
                <p style="margin: 10px 0 0 0;">
                    <?php if ($reset_info['type'] === 'admin'): ?>
                        Mật khẩu mới đã được tạo cho tài khoản Admin: <strong><?php echo htmlspecialchars($reset_info['ten_dang_nhap']); ?></strong>
                    <?php else: ?>
                        Mật khẩu mới đã được tạo cho tài khoản: <strong><?php echo htmlspecialchars($reset_info['email']); ?></strong>
                        <?php if (!empty($reset_info['ho_ten'])): ?>
                            <br>(<?php echo htmlspecialchars($reset_info['ho_ten']); ?>)
                        <?php endif; ?>
                    <?php endif; ?>
                </p>
            </div>
            
            <div class="password-display">
                <label>Mật khẩu mới của bạn:</label>
                <div class="password-value" id="password-display"><?php echo htmlspecialchars($new_password); ?></div>
                <button type="button" class="copy-btn" onclick="copyPassword()">Sao chép mật khẩu</button>
            </div>
            
            <div class="warning-box">
                <strong>⚠ Lưu ý quan trọng:</strong>
                <ul style="margin: 10px 0 0 20px; padding: 0;">
                    <li>Mật khẩu này chỉ hiển thị một lần duy nhất</li>
                    <li>Vui lòng sao chép và lưu mật khẩu ở nơi an toàn</li>
                    <li>Nên đổi mật khẩu sau khi đăng nhập thành công</li>
                </ul>
            </div>
            
            <a href="login.php" class="btn btn-primary">Đăng nhập ngay</a>
        </div>
    </div>
    
    <script>
        function copyPassword() {
            const password = document.getElementById('password-display').textContent;
            navigator.clipboard.writeText(password).then(function() {
                const btn = event.target;
                const originalText = btn.textContent;
                btn.textContent = 'Đã sao chép!';
                btn.style.background = '#27ae60';
                setTimeout(function() {
                    btn.textContent = originalText;
                    btn.style.background = '#667eea';
                }, 2000);
            }).catch(function(err) {
                alert('Không thể sao chép. Vui lòng sao chép thủ công.');
            });
        }
    </script>
</body>
</html>

