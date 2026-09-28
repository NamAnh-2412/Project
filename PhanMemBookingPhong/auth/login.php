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

// Tên file ảnh background - thay đổi tên file tại đây
// Lưu ý: Nên dùng .jpg hoặc .png để tương thích tốt hơn với mọi trình duyệt
// Đặt ảnh vào thư mục: assets/images/
$background_image = 'thiet-ke-noi-that-phong-ngu-bs5.jpeg.webp';
$background_image_path = __DIR__ . '/../assets/images/' . $background_image;
$background_image_url = BASE_URL . 'assets/images/' . rawurlencode($background_image);

// Kiểm tra file ảnh có tồn tại không, nếu không thì dùng màu nền mặc định
$has_background_image = file_exists($background_image_path);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';
    $login_type = $_POST['login_type'] ?? 'user'; // 'user' hoặc 'admin'
    
    if (empty($email) || empty($password)) {
        $error = 'Vui lòng nhập thông tin đăng nhập';
    } else {
        try {
            $pdo = getDBConnection();
            
            if ($login_type === 'admin') {
                // Đăng nhập Admin (dùng ten_dang_nhap)
                $stmt = $pdo->prepare("SELECT * FROM admin WHERE ten_dang_nhap = ? AND trang_thai = 'active'");
                $stmt->execute([$email]);
                $admin = $stmt->fetch();
                
                $login_success = false;
                
                if ($admin) {
                    // Thử verify với password_verify() trước (nếu mật khẩu đã hash)
                    if (password_verify($password, $admin['mat_khau'])) {
                        $login_success = true;
                    } 
                    // Nếu không match, kiểm tra xem có phải plain text không
                    else if ($admin['mat_khau'] === $password) {
                        // Mật khẩu là plain text, tự động hash và cập nhật vào database
                        $hashed_password = password_hash($password, PASSWORD_DEFAULT);
                        $update_password_stmt = $pdo->prepare("UPDATE admin SET mat_khau = ? WHERE id = ?");
                        $update_password_stmt->execute([$hashed_password, $admin['id']]);
                        $login_success = true;
                    }
                }
                
                if ($login_success) {
                    $_SESSION['admin_id'] = $admin['id'];
                    $_SESSION['admin_ten_dang_nhap'] = $admin['ten_dang_nhap'];
                    $_SESSION['admin'] = $admin;
                    
                    // Cập nhật lần đăng nhập cuối
                    $update_stmt = $pdo->prepare("UPDATE admin SET lan_dang_nhap_cuoi = NOW() WHERE id = ?");
                    $update_stmt->execute([$admin['id']]);
                    
                    header('Location: ' . BASE_URL . 'admin/dashboard.php');
                    exit;
                } else {
                    $error = 'Tên đăng nhập hoặc mật khẩu không đúng';
                }
            } else {
                // Đăng nhập User (dùng email)
                $stmt = $pdo->prepare("SELECT * FROM nguoi_dung WHERE email = ?");
                $stmt->execute([$email]);
                $user = $stmt->fetch();
                
                if ($user && password_verify($password, $user['mat_khau'])) {
                    $_SESSION['user_id'] = $user['id'];
                    $_SESSION['email'] = $user['email'];
                    $_SESSION['vai_tro'] = $user['vai_tro'];
                    $_SESSION['user'] = $user;
                    
                    // Redirect theo vai trò
                    if ($user['vai_tro'] === 'chu_nha') {
                        header('Location: ' . BASE_URL . 'admin/index.php');
                    } else {
                        header('Location: ' . BASE_URL . 'index.php');
                    }
                    exit;
                } else {
                    $error = 'Email hoặc mật khẩu không đúng';
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
    <title>Đăng nhập - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
</head>
<body class="auth-page">
    <div class="container">
        <div class="auth-form">
            <h2>Đăng nhập</h2>
            
            <?php if ($error): ?>
                <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>
            
            <form method="POST" action="">
                <div class="form-group">
                    <label>Loại đăng nhập</label>
                    <select name="login_type" id="login_type" onchange="updateLoginForm()" style="width: 100%; padding: 10px; margin-bottom: 15px;">
                        <option value="user">Người dùng (Email)</option>
                        <option value="admin">Admin (Tên đăng nhập)</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label id="email_label">Email</label>
                    <input type="text" name="email" id="email_input" required value="<?php echo htmlspecialchars($_POST['email'] ?? ''); ?>" placeholder="Nhập email hoặc tên đăng nhập">
                </div>
                
                <div class="form-group">
                    <label>Mật khẩu</label>
                    <div class="password-input-wrapper">
                        <input type="password" name="password" id="password" required>
                        <button type="button" class="password-toggle" onclick="togglePassword('password')" title="Hiển thị mật khẩu">
                            <span class="password-icon hide" id="password-icon"></span>
                        </button>
                    </div>
                </div>
                
                <button type="submit" class="btn btn-primary">Đăng nhập</button>
            </form>
            
            <script>
                function updateLoginForm() {
                    const loginType = document.getElementById('login_type').value;
                    const emailLabel = document.getElementById('email_label');
                    const emailInput = document.getElementById('email_input');
                    
                    if (loginType === 'admin') {
                        emailLabel.textContent = 'Tên đăng nhập';
                        emailInput.placeholder = 'Nhập tên đăng nhập';
                        emailInput.type = 'text';
                    } else {
                        emailLabel.textContent = 'Email';
                        emailInput.placeholder = 'Nhập email';
                        emailInput.type = 'email';
                    }
                }
            </script>
            
            <p class="text-center">
                <a href="forgot_password.php">Quên mật khẩu?</a> | 
                Chưa có tài khoản? <a href="register.php">Đăng ký ngay</a>
            </p>
        </div>
    </div>
    
    <script src="../assets/js/password-toggle.js"></script>
</body>
</html>
