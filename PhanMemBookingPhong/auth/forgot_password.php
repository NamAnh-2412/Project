<?php
require_once '../config/database.php';
require_once '../config/config.php';

// Nếu đã đăng nhập, redirect về trang chủ
if (isLoggedIn()) {
    if (isAdmin()) {
        header('Location: ' . BASE_URL . 'admin/dashboard.php');
    } else {
        $user = getCurrentUser();
        if ($user && $user['vai_tro'] === 'chu_nha') {
            header('Location: ' . BASE_URL . 'admin/index.php');
        } else {
            header('Location: ' . BASE_URL . 'index.php');
        }
    }
    exit;
}

$error = '';
$success = '';

// Tên file ảnh background
$background_image = 'thiet-ke-noi-that-phong-ngu-bs5.jpeg.webp';
$background_image_path = __DIR__ . '/../assets/images/' . $background_image;
$background_image_url = BASE_URL . 'assets/images/' . rawurlencode($background_image);
$has_background_image = file_exists($background_image_path);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $account_type = $_POST['account_type'] ?? 'user'; // 'user' hoặc 'admin'
    
    if (empty($email)) {
        $error = 'Vui lòng nhập email hoặc tên đăng nhập';
    } else {
        try {
            $pdo = getDBConnection();
            
            if ($account_type === 'admin') {
                // Reset mật khẩu Admin
                $stmt = $pdo->prepare("SELECT * FROM admin WHERE ten_dang_nhap = ? AND trang_thai = 'active'");
                $stmt->execute([$email]);
                $admin = $stmt->fetch();
                
                if ($admin) {
                    // Tạo mật khẩu mới ngẫu nhiên (8 ký tự)
                    $new_password = bin2hex(random_bytes(4)); // 8 ký tự hex
                    $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);
                    
                    // Cập nhật mật khẩu mới
                    $update_stmt = $pdo->prepare("UPDATE admin SET mat_khau = ? WHERE id = ?");
                    $update_stmt->execute([$hashed_password, $admin['id']]);
                    
                    // Lưu thông tin vào session để hiển thị ở trang tiếp theo
                    $_SESSION['reset_password'] = [
                        'type' => 'admin',
                        'ten_dang_nhap' => $admin['ten_dang_nhap'],
                        'new_password' => $new_password
                    ];
                    
                    header('Location: ' . BASE_URL . 'auth/reset_password.php');
                    exit;
                } else {
                    $error = 'Không tìm thấy tài khoản Admin với tên đăng nhập này';
                }
            } else {
                // Reset mật khẩu User
                $stmt = $pdo->prepare("SELECT * FROM nguoi_dung WHERE email = ?");
                $stmt->execute([$email]);
                $user = $stmt->fetch();
                
                if ($user) {
                    // Tạo mật khẩu mới ngẫu nhiên (8 ký tự)
                    $new_password = bin2hex(random_bytes(4)); // 8 ký tự hex
                    $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);
                    
                    // Cập nhật mật khẩu mới
                    $update_stmt = $pdo->prepare("UPDATE nguoi_dung SET mat_khau = ? WHERE id = ?");
                    $update_stmt->execute([$hashed_password, $user['id']]);
                    
                    // Lưu thông tin vào session để hiển thị ở trang tiếp theo
                    $_SESSION['reset_password'] = [
                        'type' => 'user',
                        'email' => $user['email'],
                        'ho_ten' => $user['ho_ten'],
                        'new_password' => $new_password
                    ];
                    
                    header('Location: ' . BASE_URL . 'auth/reset_password.php');
                    exit;
                } else {
                    $error = 'Không tìm thấy tài khoản với email này';
                }
            }
        } catch (PDOException $e) {
            $error = 'Có lỗi xảy ra: ' . $e->getMessage();
        }
    }
}
?>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - Booking Phòng Trọ</title>
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
            max-width: 450px;
            width: 100%;
        }
        .auth-form h2 {
            margin-bottom: 25px;
            color: #333;
            text-align: center;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #555;
            font-weight: 500;
        }
        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            box-sizing: border-box;
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
        }
        .btn-primary:hover {
            background: #5568d3;
        }
        .alert {
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .alert-error {
            background: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }
        .text-center {
            text-align: center;
            margin-top: 20px;
        }
        .text-center a {
            color: #667eea;
            text-decoration: none;
        }
        .text-center a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body class="auth-page">
    <div class="container">
        <div class="auth-form">
            <h2>Quên mật khẩu</h2>
            
            <?php if ($error): ?>
                <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>
            
            <?php if ($success): ?>
                <div class="alert alert-success"><?php echo htmlspecialchars($success); ?></div>
            <?php endif; ?>
            
            <form method="POST" action="">
                <div class="form-group">
                    <label>Loại tài khoản</label>
                    <select name="account_type" id="account_type" onchange="updateForm()" style="width: 100%; padding: 10px; margin-bottom: 15px;">
                        <option value="user">Người dùng (Email)</option>
                        <option value="admin">Admin (Tên đăng nhập)</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label id="input_label">Email</label>
                    <input type="text" name="email" id="email_input" required value="<?php echo htmlspecialchars($_POST['email'] ?? ''); ?>" placeholder="Nhập email hoặc tên đăng nhập">
                </div>
                
                <button type="submit" class="btn btn-primary">Đặt lại mật khẩu</button>
            </form>
            
            <script>
                function updateForm() {
                    const accountType = document.getElementById('account_type').value;
                    const inputLabel = document.getElementById('input_label');
                    const emailInput = document.getElementById('email_input');
                    
                    if (accountType === 'admin') {
                        inputLabel.textContent = 'Tên đăng nhập';
                        emailInput.placeholder = 'Nhập tên đăng nhập';
                        emailInput.type = 'text';
                    } else {
                        inputLabel.textContent = 'Email';
                        emailInput.placeholder = 'Nhập email';
                        emailInput.type = 'email';
                    }
                }
            </script>
            
            <p class="text-center">
                <a href="login.php">← Quay lại đăng nhập</a>
            </p>
        </div>
    </div>
</body>
</html>

