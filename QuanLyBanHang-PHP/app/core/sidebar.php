<?php
// Tính toán base URL động dựa trên file đang chạy
$current_script = $_SERVER['SCRIPT_NAME'];
// Xác định xem file đang chạy ở đâu
if (strpos($current_script, '/app/') !== false) {
    // File đang chạy trong thư mục app/
    // Đếm số cấp thư mục từ file hiện tại lên app/
    // Ví dụ: /QuanLyBanHang/app/khachhang/index.php
    // Từ khachhang/index.php lên app/ cần 2 cấp: ../../
    // Từ app/ lên root cần 1 cấp: ../
    // Vậy từ khachhang/index.php lên root cần 3 cấp: ../../../
    
    // Tách đường dẫn và đếm số cấp sau 'app'
    $path_after_app = substr($current_script, strpos($current_script, '/app/') + 5); // +5 để bỏ qua '/app/'
    $depth = substr_count($path_after_app, '/'); // Đếm số dấu / còn lại
    // Từ file hiện tại lên app/ cần (depth) cấp
    // Từ app/ lên root cần thêm 1 cấp
    $levels_to_root = $depth + 1;
    $levels_to_app = $depth;
    $root_path = str_repeat('../', $levels_to_root);
    $app_path = str_repeat('../', $levels_to_app);
} else {
    // File đang chạy ở root
    $root_path = '';
    $app_path = 'app/';
}

// LẤY TÊN CỬA HÀNG VÀ LOGO TỪ DATABASE
$shop_name = "CHILL GUYS"; // Giá trị mặc định
$shop_logo = ""; // Logo path

// Thử dùng helper function getSetting (nếu đã load bootstrap)
if (function_exists('getSetting')) {
    $shop_name = getSetting('ten_cua_hang', 'CHILL GUYS');
    if (empty($shop_name)) {
        $shop_name = getSetting('shop_name', 'CHILL GUYS'); // Fallback
    }
    $shop_logo = getSetting('logo_path', '');
} else if (function_exists('db_fetch_one')) {
    // Fallback: dùng db_fetch_one
    $shopSetting = db_fetch_one("SELECT `value` FROM settings WHERE `key` = 'ten_cua_hang'");
    if ($shopSetting && !empty($shopSetting['value'])) {
        $shop_name = $shopSetting['value'];
    } else {
        $shopSetting = db_fetch_one("SELECT `value` FROM settings WHERE `key` = 'shop_name'");
        if ($shopSetting && !empty($shopSetting['value'])) {
            $shop_name = $shopSetting['value'];
        }
    }
    $logoSetting = db_fetch_one("SELECT `value` FROM settings WHERE `key` = 'logo_path'");
    if ($logoSetting && !empty($logoSetting['value'])) {
        $shop_logo = $logoSetting['value'];
    }
} else {
    // Nếu chưa có helper function, dùng mysqli trực tiếp
    global $conn;
    if (isset($conn) && $conn) {
        $sql_brand = "SELECT `value` FROM settings WHERE `key` = 'ten_cua_hang'";
        $res_brand = mysqli_query($conn, $sql_brand);
        
        if ($res_brand && mysqli_num_rows($res_brand) > 0) {
            $row_brand = mysqli_fetch_assoc($res_brand);
            $shop_name = $row_brand['value'];
        } else {
            $sql_brand = "SELECT `value` FROM settings WHERE `key` = 'shop_name'";
            $res_brand = mysqli_query($conn, $sql_brand);
            if ($res_brand && mysqli_num_rows($res_brand) > 0) {
                $row_brand = mysqli_fetch_assoc($res_brand);
                $shop_name = $row_brand['value'];
            }
        }
        
        // Lấy logo
        $sql_logo = "SELECT `value` FROM settings WHERE `key` = 'logo_path'";
        $res_logo = mysqli_query($conn, $sql_logo);
        if ($res_logo && mysqli_num_rows($res_logo) > 0) {
            $row_logo = mysqli_fetch_assoc($res_logo);
            $shop_logo = $row_logo['value'];
        }
    }
}

// Kiểm tra logo có tồn tại không
$logo_exists = false;
$logo_url = '';
if (!empty($shop_logo)) {
    $rootPath = defined('ROOT_PATH') ? ROOT_PATH : dirname(dirname(__DIR__));
    $logo_full_path = $rootPath . '/' . $shop_logo;
    $logo_exists = file_exists($logo_full_path);
    if ($logo_exists) {
        // Tính base URL
        $logo_url = $root_path . $shop_logo;
    }
}

// Lấy chữ cái đầu của tên cửa hàng để làm fallback
$shop_initial = !empty($shop_name) ? strtoupper(substr(trim($shop_name), 0, 1)) : 'C';
?>
<aside class="sidebar" style="display:flex;flex-direction:column;">
    <!-- Logo và Tên Cửa Hàng -->
    <div class="sidebar-logo-section">
        <div class="sidebar-logo-container">
            <?php if ($logo_exists && !empty($logo_url)): ?>
                <img src="<?php echo htmlspecialchars($logo_url); ?>" alt="<?php echo htmlspecialchars($shop_name); ?>" class="sidebar-logo-image">
            <?php else: ?>
                <div class="sidebar-logo-placeholder">
                    <span class="sidebar-logo-initial"><?php echo htmlspecialchars($shop_initial); ?></span>
                </div>
            <?php endif; ?>
        </div>
        <h2 class="sidebar-shop-name"><?php echo htmlspecialchars($shop_name); ?></h2>
    </div>
    <div class="sidebar-divider"></div>
    <ul>
        <li><a href="<?php echo $root_path; ?>index.php" class="icon-link"><span class="icon"><i class="fas fa-home"></i></span> Quản Lý Tổng</a></li>
        <li><a href="<?php echo $app_path; ?>donhang/index.php" class="icon-link"><span class="icon"><i class="fas fa-shopping-cart"></i></span> Đơn hàng</a></li>
        <li class="menu-item has-submenu" id="spmenu">
          <a href="#" class="icon-link" onclick="toggleSubMenu('sp-submenu');return false;">
            <span class="icon"><i class="fas fa-box"></i></span> Sản phẩm <span class="submenu-toggle"><i class="fas fa-chevron-down"></i></span>
          </a>
          <ul class="submenu" id="sp-submenu">
            <li><a href="<?php echo $app_path; ?>sanpham/nguyenlieu.php">Nguyên liệu</a></li>
            <li><a href="<?php echo $app_path; ?>sanpham/thucdon.php">Thực đơn</a></li>
            <li><a href="<?php echo $app_path; ?>sanpham/nhomluachon.php">Nhóm lựa chọn</a></li>
            <li><a href="<?php echo $app_path; ?>sanpham/index.php">Sản phẩm</a></li>
          </ul>
        </li>
        <li><a href="<?php echo $app_path; ?>khachhang/index.php" class="icon-link"><span class="icon"><i class="fas fa-user-friends"></i></span> Khách hàng</a></li>
        <li><a href="<?php echo $app_path; ?>nhanvien/index.php" class="icon-link"><span class="icon"><i class="fas fa-user-tie"></i></span> Nhân viên</a></li>
        <li><a href="<?php echo $app_path; ?>kho/index.php" class="icon-link"><span class="icon"><i class="fas fa-warehouse"></i></span> Kho hàng</a></li>
        <li><a href="<?php echo $app_path; ?>nhacungcap/index.php" class="icon-link"><span class="icon"><i class="fas fa-truck"></i></span> Nhà cung cấp</a></li>
        <li><a href="<?php echo $app_path; ?>baocao/index.php" class="icon-link"><span class="icon"><i class="fas fa-chart-line"></i></span> Báo cáo</a></li>
        <li><a href="<?php echo $app_path; ?>caidat/index.php" class="icon-link"><span class="icon"><i class="fas fa-cog"></i></span> Cài đặt</a></li>
        <li><a href="<?php echo $app_path; ?>auth/logout.php" class="icon-link"><span class="icon"><i class="fas fa-sign-out-alt"></i></span> Đăng xuất</a></li>
    </ul>
    <div class="theme-toggle-wrapper">
        <button class="theme-toggle" aria-label="Toggle dark mode" title="Chuyển chế độ tối/sáng">
            <i class="fas fa-moon icon-moon"></i>
            <i class="fas fa-sun icon-sun"></i>
        </button>
    </div>
    <script>
    function toggleSubMenu(id){
      let e=document.getElementById(id);
      e.classList.toggle('active');
    }
    </script>
    <style>
    .sidebar {overflow-y:auto; scrollbar-width:thin;}
    .submenu {max-height:0;overflow:hidden;transition:max-height .3s;}
    .submenu.active{max-height:400px;}
    .submenu li{padding-left:10px;}
    .has-submenu>a{justify-content:space-between!important;}
    .submenu-toggle{margin-left:5px; font-size:14px;color:#f97316;}
    </style>
</aside>

