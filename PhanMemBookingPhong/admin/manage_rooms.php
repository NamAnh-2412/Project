<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireLogin();
requireChuNha(); // Chỉ Chủ nhà mới được đăng phòng

$pdo = getDBConnection();
$user = getCurrentUser();
$error = '';
$show_form = isset($_GET['add']) || isset($_POST['tieu_de']); // Hiện form khi có param add hoặc đang submit

// Xử lý thêm phòng mới
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['tieu_de'])) {
    $tieu_de = trim($_POST['tieu_de'] ?? '');
    $mo_ta = trim($_POST['mo_ta'] ?? '');
    $dia_chi = trim($_POST['dia_chi'] ?? '');
    $gia = $_POST['gia'] ?? '';
    $quan_huyen = trim($_POST['quan_huyen'] ?? '');
    $dien_tich = $_POST['dien_tich'] ?? '';
    $trang_thai = $_POST['trang_thai'] ?? 'con_trong';
    $images_input = trim($_POST['images'] ?? '');
    $tien_ich_ids = $_POST['tien_ich'] ?? [];
    
    // Validation
    if (empty($tieu_de) || empty($dia_chi) || empty($gia)) {
        $error = 'Vui lòng điền đầy đủ thông tin bắt buộc (tiêu đề, địa chỉ, giá)';
    } elseif (!is_numeric($gia) || $gia <= 0) {
        $error = 'Giá phòng phải là số dương';
    } else {
        try {
            $pdo->beginTransaction();
            
            // Thêm phòng
            $stmt = $pdo->prepare("INSERT INTO phong (tieu_de, mo_ta, dia_chi, quan_huyen, gia, dien_tich, trang_thai, nguoi_dung_id) 
                                   VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            $stmt->execute([
                $tieu_de,
                $mo_ta ?: null,
                $dia_chi,
                $quan_huyen ?: null,
                $gia,
                $dien_tich ?: null,
                $trang_thai,
                $user['id']
            ]);
            
            $phong_id = $pdo->lastInsertId();
            
            // Upload hình ảnh từ file
            $uploaded_files = [];
            if (isset($_FILES['images_upload']) && !empty($_FILES['images_upload']['name'][0])) {
                $upload_dir = '../uploads/';
                if (!is_dir($upload_dir)) {
                    mkdir($upload_dir, 0777, true);
                }
                
                $allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
                $max_size = 5 * 1024 * 1024; // 5MB
                
                foreach ($_FILES['images_upload']['name'] as $key => $filename) {
                    if ($_FILES['images_upload']['error'][$key] === UPLOAD_ERR_OK) {
                        $tmp_name = $_FILES['images_upload']['tmp_name'][$key];
                        $file_type = $_FILES['images_upload']['type'][$key];
                        $file_size = $_FILES['images_upload']['size'][$key];
                        
                        if (!in_array($file_type, $allowed_types)) {
                            continue;
                        }
                        
                        if ($file_size > $max_size) {
                            continue;
                        }
                        
                        $file_ext = pathinfo($filename, PATHINFO_EXTENSION);
                        $new_filename = 'room_' . $phong_id . '_' . time() . '_' . $key . '.' . $file_ext;
                        $upload_path = $upload_dir . $new_filename;
                        
                        if (move_uploaded_file($tmp_name, $upload_path)) {
                            $uploaded_files[] = 'uploads/' . $new_filename;
                        }
                    }
                }
            }
            
            // Thêm hình ảnh (từ upload hoặc URL)
            $all_images = [];
            foreach ($uploaded_files as $image_path) {
                $all_images[] = $image_path;
            }
            
            if (!empty($images_input)) {
                $images_list = explode(',', $images_input);
                $images_array = array_map('trim', $images_list);
                $images_array = array_filter($images_array);
                $all_images = array_merge($all_images, $images_array);
            }
            
            // Lưu vào database
            if (!empty($all_images)) {
                $first = true;
                foreach ($all_images as $image_path) {
                    $stmt = $pdo->prepare("INSERT INTO anh_phong (phong_id, duong_dan_anh, la_anh_chinh) VALUES (?, ?, ?)");
                    $stmt->execute([$phong_id, $image_path, $first ? 1 : 0]);
                    $first = false;
                }
            }
            
            // Thêm tiện ích
            if (!empty($tien_ich_ids)) {
                foreach ($tien_ich_ids as $tien_ich_id) {
                    $stmt = $pdo->prepare("INSERT INTO phong_tien_ich (phong_id, tien_ich_id) VALUES (?, ?)");
                    $stmt->execute([$phong_id, $tien_ich_id]);
                }
            }
            
            // Tự động chuyển thành chủ nhà nếu chưa phải chủ nhà
            if ($user['vai_tro'] !== 'chu_nha') {
                $update_stmt = $pdo->prepare("UPDATE nguoi_dung SET vai_tro = 'chu_nha' WHERE id = ?");
                $update_stmt->execute([$user['id']]);
                $_SESSION['vai_tro'] = 'chu_nha';
            }
            
            $pdo->commit();
            header('Location: manage_rooms.php?success=added');
            exit;
        } catch (PDOException $e) {
            $pdo->rollBack();
            $error = 'Có lỗi xảy ra: ' . $e->getMessage();
        }
    }
}

// Xóa phòng
if (isset($_GET['delete']) && $_GET['delete']) {
    $phong_id = $_GET['delete'];
    $stmt = $pdo->prepare("DELETE FROM phong WHERE id = ? AND nguoi_dung_id = ?");
    $stmt->execute([$phong_id, $user['id']]);
    header('Location: manage_rooms.php?success=deleted');
    exit;
}

// Lấy danh sách phòng của chủ nhà này
$stmt = $pdo->prepare("SELECT p.*, nd.ho_ten as chu_nha_ten 
                       FROM phong p 
                       LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id 
                       WHERE p.nguoi_dung_id = ?
                       ORDER BY p.id DESC, p.ngay_tao DESC");
$stmt->execute([$user['id']]);
$rooms = $stmt->fetchAll();

// Lấy danh sách tiện ích
$tien_ich_stmt = $pdo->query("SELECT * FROM tien_ich ORDER BY ten");
$tien_ich_list = $tien_ich_stmt->fetchAll();

$success_message = '';
if (isset($_GET['success'])) {
    $success_message = $_GET['success'] === 'deleted' ? 'Đã xóa phòng thành công!' : 
                       ($_GET['success'] === 'added' ? 'Đã thêm phòng thành công!' : 
                       ($_GET['success'] === 'updated' ? 'Đã cập nhật phòng thành công!' : ''));
}
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng phòng - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
    <style>
        .add-room-form-container {
            display: none;
            margin: 30px 0;
            padding: 20px;
            background: #f9f9f9;
            border-radius: 8px;
            border: 1px solid #ddd;
        }
        .add-room-form-container.show {
            display: block;
        }
        .toggle-form-btn {
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div class="page-header">
            <h1>Đăng phòng</h1>
            <button type="button" class="btn btn-primary toggle-form-btn" id="toggleFormBtn">
                + Đăng phòng mới
            </button>
        </div>
        
        <?php if ($success_message): ?>
            <div class="alert alert-success"><?php echo htmlspecialchars($success_message); ?></div>
        <?php endif; ?>
        
        <!-- Form đăng phòng -->
        <div class="add-room-form-container <?php echo $show_form ? 'show' : ''; ?>" id="addRoomForm">
            <h2>Thêm phòng mới</h2>
            
            <?php if ($error): ?>
                <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>
            
            <form method="POST" action="" class="room-form" enctype="multipart/form-data">
                <div class="form-row">
                    <div class="form-group">
                        <label>Tiêu đề phòng *</label>
                        <input type="text" name="tieu_de" required value="<?php echo htmlspecialchars($_POST['tieu_de'] ?? ''); ?>">
                    </div>
                    
                    <div class="form-group">
                        <label>Giá thuê (đồng/tháng) *</label>
                        <input type="number" name="gia" min="0" step="1000" required value="<?php echo htmlspecialchars($_POST['gia'] ?? ''); ?>">
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Địa chỉ *</label>
                    <textarea name="dia_chi" required><?php echo htmlspecialchars($_POST['dia_chi'] ?? ''); ?></textarea>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Quận/Huyện</label>
                        <input type="text" name="quan_huyen" value="<?php echo htmlspecialchars($_POST['quan_huyen'] ?? ''); ?>">
                    </div>
                    
                    <div class="form-group">
                        <label>Diện tích (m²)</label>
                        <input type="number" name="dien_tich" min="0" step="0.1" value="<?php echo htmlspecialchars($_POST['dien_tich'] ?? ''); ?>">
                    </div>
                    
                    <div class="form-group">
                        <label>Trạng thái</label>
                        <select name="trang_thai">
                            <option value="con_trong" <?php echo ($_POST['trang_thai'] ?? 'con_trong') === 'con_trong' ? 'selected' : ''; ?>>Còn trống</option>
                            <option value="da_thue" <?php echo ($_POST['trang_thai'] ?? '') === 'da_thue' ? 'selected' : ''; ?>>Đã thuê</option>
                        </select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea name="mo_ta" rows="5"><?php echo htmlspecialchars($_POST['mo_ta'] ?? ''); ?></textarea>
                </div>
                
                <div class="form-group">
                    <label>Hình ảnh *</label>
                    <input type="file" name="images_upload[]" id="images_upload" multiple accept="image/*" class="file-input">
                    <label for="images_upload" class="file-input-label">
                        <span class="file-input-text">📷 Chọn ảnh từ máy tính</span>
                        <span class="file-input-button">Chọn ảnh</span>
                    </label>
                    <small>Có thể chọn nhiều ảnh. Hình đầu tiên sẽ là ảnh chính. Hỗ trợ: JPG, PNG, GIF, WEBP (tối đa 5MB/ảnh)</small>
                    
                    <div id="image-preview" class="image-preview"></div>
                    
                    <div class="form-group" style="margin-top: 15px;">
                        <label>Hoặc nhập URL hình ảnh (tùy chọn)</label>
                        <input type="text" name="images" placeholder="uploads/room1.jpg, uploads/room2.jpg" value="<?php echo htmlspecialchars($_POST['images'] ?? ''); ?>">
                        <small>Nhập URL hình ảnh cách nhau bởi dấu phẩy nếu bạn muốn sử dụng ảnh có sẵn.</small>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Tiện ích</label>
                    <div class="amenities-checkboxes">
                        <?php foreach ($tien_ich_list as $tien_ich): ?>
                            <label class="checkbox-label">
                                <input type="checkbox" name="tien_ich[]" value="<?php echo $tien_ich['id']; ?>"
                                       <?php echo (isset($_POST['tien_ich']) && in_array($tien_ich['id'], $_POST['tien_ich'])) ? 'checked' : ''; ?>>
                                <?php if ($tien_ich['icon']): ?>
                                    <span class="amenity-icon"><?php echo htmlspecialchars($tien_ich['icon']); ?></span>
                                <?php endif; ?>
                                <?php echo htmlspecialchars($tien_ich['ten']); ?>
                            </label>
                        <?php endforeach; ?>
                    </div>
                </div>
                
                <div class="form-group info-box">
                    <p><strong>Thông tin liên hệ:</strong> Thông tin liên hệ sẽ lấy từ thông tin tài khoản của bạn (số điện thoại, email đã đăng ký).</p>
                </div>
                
                <button type="submit" class="btn btn-primary">Thêm phòng</button>
                <button type="button" class="btn btn-secondary" id="cancelFormBtn">Hủy</button>
            </form>
        </div>
        
        <!-- Danh sách phòng đã đăng -->
        <div class="rooms-list-section">
            <h2>Danh sách phòng đã đăng</h2>
            
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tiêu đề</th>
                            <th>Địa chỉ</th>
                            <th>Giá</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if (empty($rooms)): ?>
                            <tr>
                                <td colspan="6" class="text-center">Chưa có phòng nào. Hãy đăng phòng đầu tiên của bạn!</td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($rooms as $room): ?>
                                <tr>
                                    <td><?php echo $room['id']; ?></td>
                                    <td><?php echo htmlspecialchars($room['tieu_de']); ?></td>
                                    <td><?php echo htmlspecialchars(substr($room['dia_chi'], 0, 50)) . (strlen($room['dia_chi']) > 50 ? '...' : ''); ?></td>
                                    <td><?php echo number_format($room['gia']); ?> đ</td>
                                    <td>
                                        <span class="room-status status-<?php echo $room['trang_thai']; ?>">
                                            <?php echo getTrangThaiPhongText($room['trang_thai']); ?>
                                        </span>
                                    </td>
                                    <td>
                                        <a href="../room_detail.php?id=<?php echo $room['id']; ?>" class="btn btn-sm btn-secondary" target="_blank">Xem</a>
                                        <a href="edit_room.php?id=<?php echo $room['id']; ?>" class="btn btn-sm btn-warning">Sửa</a>
                                        <a href="?delete=<?php echo $room['id']; ?>" 
                                           class="btn btn-sm btn-danger" 
                                           onclick="return confirm('Bạn có chắc muốn xóa phòng này?')">Xóa</a>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <?php include '../includes/footer.php'; ?>
    
    <script src="../assets/js/image-preview.js"></script>
    <script>
        // Toggle form đăng phòng
        const toggleFormBtn = document.getElementById('toggleFormBtn');
        const addRoomForm = document.getElementById('addRoomForm');
        const cancelFormBtn = document.getElementById('cancelFormBtn');
        
        toggleFormBtn.addEventListener('click', function() {
            addRoomForm.classList.toggle('show');
            if (addRoomForm.classList.contains('show')) {
                toggleFormBtn.textContent = '− Ẩn form đăng phòng';
                // Scroll to form
                addRoomForm.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            } else {
                toggleFormBtn.textContent = '+ Đăng phòng mới';
            }
        });
        
        cancelFormBtn.addEventListener('click', function() {
            addRoomForm.classList.remove('show');
            toggleFormBtn.textContent = '+ Đăng phòng mới';
        });
        
        // Tự động mở form nếu có tham số ?add=1 trong URL
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('add') === '1' || addRoomForm.classList.contains('show')) {
            addRoomForm.classList.add('show');
            toggleFormBtn.textContent = '− Ẩn form đăng phòng';
            // Scroll to form sau khi trang load
            setTimeout(function() {
                addRoomForm.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }, 100);
        }
        
        // Initialize image preview khi form hiện
        function initImagePreview() {
            if (addRoomForm.classList.contains('show')) {
                initializeImagePreview('images_upload', 'image-preview');
            }
        }
        
        // Khởi tạo ngay khi trang load
        initImagePreview();
        
        // Re-initialize khi toggle form
        toggleFormBtn.addEventListener('click', function() {
            setTimeout(function() {
                initImagePreview();
            }, 100);
        });
    </script>
</body>
</html>
