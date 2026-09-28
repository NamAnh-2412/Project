<?php
require_once '../config/database.php';
require_once '../config/config.php';

requireLogin();
requireChuNha(); // Chỉ Chủ nhà mới được chỉnh sửa phòng

$pdo = getDBConnection();
$user = getCurrentUser();
$phong_id = $_GET['id'] ?? 0;

// Lấy thông tin phòng (chỉ phòng của chủ nhà này)
$stmt = $pdo->prepare("SELECT * FROM phong WHERE id = ? AND nguoi_dung_id = ?");
$stmt->execute([$phong_id, $user['id']]);
$phong = $stmt->fetch();

if (!$phong) {
    header('Location: manage_rooms.php');
    exit;
}

// Lấy hình ảnh hiện tại
$images_stmt = $pdo->prepare("SELECT * FROM anh_phong WHERE phong_id = ? ORDER BY la_anh_chinh DESC, id ASC");
$images_stmt->execute([$phong_id]);
$current_images = $images_stmt->fetchAll();

// Lấy tiện ích hiện tại
$tien_ich_stmt = $pdo->prepare("SELECT tien_ich_id FROM phong_tien_ich WHERE phong_id = ?");
$tien_ich_stmt->execute([$phong_id]);
$current_tien_ich = array_column($tien_ich_stmt->fetchAll(), 'tien_ich_id');

// Lấy danh sách tiện ích
$all_tien_ich_stmt = $pdo->query("SELECT * FROM tien_ich ORDER BY ten");
$all_tien_ich = $all_tien_ich_stmt->fetchAll();

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
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
            
            // Cập nhật phòng (chỉ các cột có trong bảng phong)
            $stmt = $pdo->prepare("UPDATE phong SET tieu_de = ?, mo_ta = ?, dia_chi = ?, quan_huyen = ?, gia = ?, dien_tich = ?, trang_thai = ? WHERE id = ? AND nguoi_dung_id = ?");
            $stmt->execute([
                $tieu_de,
                $mo_ta ?: null,
                $dia_chi,
                $quan_huyen ?: null,
                $gia,
                $dien_tich ?: null,
                $trang_thai,
                $phong_id,
                $user['id']
            ]);
            
            // Upload hình ảnh mới từ file
            $uploaded_files = [];
            $has_new_images = false;
            
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
                        
                        // Validate file type
                        if (!in_array($file_type, $allowed_types)) {
                            continue;
                        }
                        
                        // Validate file size
                        if ($file_size > $max_size) {
                            continue;
                        }
                        
                        // Generate unique filename
                        $file_ext = pathinfo($filename, PATHINFO_EXTENSION);
                        $new_filename = 'room_' . $phong_id . '_' . time() . '_' . $key . '.' . $file_ext;
                        $upload_path = $upload_dir . $new_filename;
                        
                        // Move uploaded file
                        if (move_uploaded_file($tmp_name, $upload_path)) {
                            $uploaded_files[] = 'uploads/' . $new_filename;
                            $has_new_images = true;
                        }
                    }
                }
            }
            
            // Xử lý hình ảnh: nếu có ảnh mới (upload hoặc URL), xóa ảnh cũ và thêm ảnh mới
            $all_images = [];
            if (!empty($uploaded_files) || !empty($images_input)) {
                $has_new_images = true;
                
                // Thêm ảnh đã upload
                foreach ($uploaded_files as $image_path) {
                    $all_images[] = $image_path;
                }
                
                // Thêm ảnh từ URL (nếu có)
                if (!empty($images_input)) {
                    $images_list = explode(',', $images_input);
                    $images_array = array_map('trim', $images_list);
                    $images_array = array_filter($images_array);
                    $all_images = array_merge($all_images, $images_array);
                }
            }
            
            // Nếu có ảnh mới, xóa ảnh cũ và thêm ảnh mới
            if ($has_new_images && !empty($all_images)) {
                // Xóa tất cả hình ảnh cũ
                $stmt = $pdo->prepare("DELETE FROM anh_phong WHERE phong_id = ?");
                $stmt->execute([$phong_id]);
                
                // Thêm hình ảnh mới
                $first = true;
                foreach ($all_images as $image_path) {
                    $stmt = $pdo->prepare("INSERT INTO anh_phong (phong_id, duong_dan_anh, la_anh_chinh) VALUES (?, ?, ?)");
                    $stmt->execute([$phong_id, $image_path, $first ? 1 : 0]);
                    $first = false;
                }
            }
            
            // Cập nhật tiện ích
            // Xóa tiện ích cũ
            $stmt = $pdo->prepare("DELETE FROM phong_tien_ich WHERE phong_id = ?");
            $stmt->execute([$phong_id]);
            
            // Thêm tiện ích mới
            if (!empty($tien_ich_ids)) {
                foreach ($tien_ich_ids as $tien_ich_id) {
                    $stmt = $pdo->prepare("INSERT INTO phong_tien_ich (phong_id, tien_ich_id) VALUES (?, ?)");
                    $stmt->execute([$phong_id, $tien_ich_id]);
                }
            }
            
            $pdo->commit();
            header('Location: manage_rooms.php?success=updated');
            exit;
        } catch (PDOException $e) {
            $pdo->rollBack();
            $error = 'Có lỗi xảy ra: ' . $e->getMessage();
        }
    }
}

// Chuẩn bị dữ liệu cho form
$images_string = '';
foreach ($current_images as $img) {
    $images_string .= $img['duong_dan_anh'] . ', ';
}
$images_string = rtrim($images_string, ', ');
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa phòng - Booking Phòng Trọ</title>
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="assets/css/admin.css">
</head>
<body>
    <?php include '../includes/header.php'; ?>
    
    <div class="container">
        <div class="page-header">
            <h1>Chỉnh sửa phòng</h1>
            <a href="manage_rooms.php" class="btn btn-secondary">Quay lại</a>
        </div>
        
        <?php if ($error): ?>
            <div class="alert alert-error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <form method="POST" action="" class="room-form" enctype="multipart/form-data">
            <div class="form-row">
                <div class="form-group">
                    <label>Tiêu đề phòng *</label>
                    <input type="text" name="tieu_de" required value="<?php echo htmlspecialchars($_POST['tieu_de'] ?? $phong['tieu_de']); ?>">
                </div>
                
                <div class="form-group">
                    <label>Giá thuê (đồng/tháng) *</label>
                    <input type="number" name="gia" min="0" step="1000" required value="<?php echo htmlspecialchars($_POST['gia'] ?? $phong['gia']); ?>">
                </div>
            </div>
            
            <div class="form-group">
                <label>Địa chỉ *</label>
                <textarea name="dia_chi" required><?php echo htmlspecialchars($_POST['dia_chi'] ?? $phong['dia_chi']); ?></textarea>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>Quận/Huyện</label>
                    <input type="text" name="quan_huyen" value="<?php echo htmlspecialchars($_POST['quan_huyen'] ?? $phong['quan_huyen']); ?>">
                </div>
                
                <div class="form-group">
                    <label>Diện tích (m²)</label>
                    <input type="number" name="dien_tich" min="0" step="0.1" value="<?php echo htmlspecialchars($_POST['dien_tich'] ?? $phong['dien_tich']); ?>">
                </div>
                
                <div class="form-group">
                    <label>Trạng thái</label>
                    <select name="trang_thai">
                        <option value="con_trong" <?php echo ($_POST['trang_thai'] ?? $phong['trang_thai']) === 'con_trong' ? 'selected' : ''; ?>>Còn trống</option>
                        <option value="da_thue" <?php echo ($_POST['trang_thai'] ?? $phong['trang_thai']) === 'da_thue' ? 'selected' : ''; ?>>Đã thuê</option>
                    </select>
                </div>
            </div>
            
            <div class="form-group">
                <label>Mô tả</label>
                <textarea name="mo_ta" rows="5"><?php echo htmlspecialchars($_POST['mo_ta'] ?? $phong['mo_ta']); ?></textarea>
            </div>
            
            <div class="form-group">
                <label>Hình ảnh</label>
                
                <!-- Hiển thị ảnh hiện tại -->
                <?php if (!empty($current_images)): ?>
                    <div class="current-images" style="margin-bottom: 15px;">
                        <p><strong>Ảnh hiện tại:</strong></p>
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; margin-top: 10px;">
                            <?php foreach ($current_images as $img): ?>
                                <div style="position: relative; border: 2px solid #ddd; border-radius: 4px; overflow: hidden;">
                                    <img src="../<?php echo htmlspecialchars($img['duong_dan_anh']); ?>" 
                                         alt="Ảnh phòng" 
                                         style="width: 100%; height: 150px; object-fit: cover; display: block;">
                                    <div style="padding: 5px; background: <?php echo $img['la_anh_chinh'] ? '#3498db' : '#95a5a6'; ?>; color: white; font-size: 12px; text-align: center;">
                                        <?php echo $img['la_anh_chinh'] ? 'Ảnh chính' : 'Ảnh phụ'; ?>
                                    </div>
                                </div>
                            <?php endforeach; ?>
                        </div>
                        <small style="display: block; margin-top: 10px; color: #666;">Chọn ảnh mới bên dưới để thay thế ảnh hiện tại.</small>
                    </div>
                <?php endif; ?>
                
                <!-- Upload ảnh mới -->
                <input type="file" name="images_upload[]" id="images_upload" multiple accept="image/*" class="file-input">
                <label for="images_upload" class="file-input-label">
                    <span class="file-input-text">📷 Chọn ảnh mới từ máy tính</span>
                    <span class="file-input-button">Chọn ảnh</span>
                </label>
                <small>Có thể chọn nhiều ảnh. Hình đầu tiên sẽ là ảnh chính. Hỗ trợ: JPG, PNG, GIF, WEBP (tối đa 5MB/ảnh)</small>
                
                <div id="image-preview" class="image-preview" data-show-warning="true"></div>
                
                <div class="form-group" style="margin-top: 15px;">
                    <label>Hoặc nhập URL hình ảnh (tùy chọn)</label>
                    <input type="text" name="images" placeholder="uploads/room1.jpg, uploads/room2.jpg" value="<?php echo htmlspecialchars($_POST['images'] ?? ''); ?>">
                    <small>Nhập URL hình ảnh cách nhau bởi dấu phẩy nếu bạn muốn sử dụng ảnh có sẵn.</small>
                </div>
            </div>
            
            <div class="form-group">
                <label>Tiện ích</label>
                <div class="amenities-checkboxes">
                    <?php foreach ($all_tien_ich as $tien_ich): ?>
                        <label class="checkbox-label">
                            <input type="checkbox" name="tien_ich[]" value="<?php echo $tien_ich['id']; ?>"
                                   <?php echo (isset($_POST['tien_ich']) && in_array($tien_ich['id'], $_POST['tien_ich'])) || (!isset($_POST['tien_ich']) && in_array($tien_ich['id'], $current_tien_ich)) ? 'checked' : ''; ?>>
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
            
            <button type="submit" class="btn btn-primary">Cập nhật phòng</button>
            <a href="manage_rooms.php" class="btn btn-secondary">Hủy</a>
        </form>
    </div>
    
    <?php include '../includes/footer.php'; ?>
    
    <script src="../assets/js/image-preview.js"></script>
</body>
</html>
