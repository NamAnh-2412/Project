<?php
require_once '../config/database.php';
require_once '../config/config.php';

$error = '';
$success = '';

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
    $confirm_password = $_POST['confirm_password'] ?? '';
    $ho_ten = trim($_POST['ho_ten'] ?? '');
    $so_dien_thoai = trim($_POST['so_dien_thoai'] ?? '');
    $vai_tro = $_POST['vai_tro'] ?? 'nguoi_thue';
    
    // Validation
    if (empty($email) || empty($password) || empty($ho_ten)) {
        $error = 'Vui lòng điền đầy đủ thông tin bắt buộc';
    } elseif ($password !== $confirm_password) {
        $error = 'Mật khẩu xác nhận không khớp';
    } elseif (strlen($password) < 6) {
        $error = 'Mật khẩu phải có ít nhất 6 ký tự';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Email không hợp lệ';
    } else {
        try {
            $pdo = getDBConnection();
            
            // Kiểm tra email đã tồn tại
            $stmt = $pdo->prepare("SELECT id FROM nguoi_dung WHERE email = ?");
            $stmt->execute([$email]);
            if ($stmt->fetch()) {
                $error = 'Email đã tồn tại';
            } else {
                // Tạo user mới
                $hashed_password = password_hash($password, PASSWORD_DEFAULT);
                $stmt = $pdo->prepare("INSERT INTO nguoi_dung (email, mat_khau, ho_ten, so_dien_thoai, vai_tro) VALUES (?, ?, ?, ?, ?)");
                $stmt->execute([$email, $hashed_password, $ho_ten, $so_dien_thoai ?: null, $vai_tro]);
                
                $success = 'Đăng ký thành công! <a href="login.php">Đăng nhập ngay</a>';
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
    <title>Đăng ký - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
</head>
<body class="auth-page">
    <div class="container">
        <div class="auth-form">
            <h2>Đăng ký tài khoản</h2>
            
            <?php if ($error): ?>
                <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>
            
            <?php if ($success): ?>
                <div class="alert alert-success"><?php echo $success; ?></div>
            <?php endif; ?>
            
            <form method="POST" action="">
                <div class="form-group">
                    <label>Họ và tên *</label>
                    <input type="text" name="ho_ten" required value="<?php echo htmlspecialchars($_POST['ho_ten'] ?? ''); ?>">
                </div>
                
                <div class="form-group">
                    <label>Email *</label>
                    <input type="email" name="email" required value="<?php echo htmlspecialchars($_POST['email'] ?? ''); ?>">
                </div>
                
                <div class="form-group">
                    <label>Số điện thoại</label>
                    <input type="tel" name="so_dien_thoai" value="<?php echo htmlspecialchars($_POST['so_dien_thoai'] ?? ''); ?>">
                </div>
                
                <div class="form-group">
                    <label>Bạn là *</label>
                    <select name="vai_tro" required>
                        <option value="nguoi_thue" <?php echo ($_POST['vai_tro'] ?? 'nguoi_thue') === 'nguoi_thue' ? 'selected' : ''; ?>>Người thuê</option>
                        <option value="chu_nha" <?php echo ($_POST['vai_tro'] ?? '') === 'chu_nha' ? 'selected' : ''; ?>>Chủ nhà</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Mật khẩu *</label>
                    <div class="password-input-wrapper">
                        <input type="password" name="password" id="password" required>
                        <button type="button" class="password-toggle" onclick="togglePassword('password')" title="Hiển thị mật khẩu">
                            <span class="password-icon hide" id="password-icon"></span>
                        </button>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Xác nhận mật khẩu *</label>
                    <div class="password-input-wrapper">
                        <input type="password" name="confirm_password" id="confirm_password" required>
                        <button type="button" class="password-toggle" onclick="togglePassword('confirm_password')" title="Hiển thị mật khẩu">
                            <span class="password-icon hide" id="confirm_password-icon"></span>
                        </button>
                    </div>
                </div>
                
                <button type="submit" class="btn btn-primary">Đăng ký</button>
            </form>
            
            <p class="text-center">Đã có tài khoản? <a href="login.php">Đăng nhập ngay</a></p>
        </div>
    </div>
    
    <script src="../assets/js/password-toggle.js"></script>
</body>
</html>
