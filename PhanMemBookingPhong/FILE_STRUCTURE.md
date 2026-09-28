# Cấu trúc File - Booking Phòng Trọ

## 📁 File dành cho ADMIN (Bảng `admin`)

### Dashboard & Quản lý tổng quan
- **`admin/dashboard.php`** - Dashboard tổng quan hệ thống (thống kê, menu quản lý)
- **`admin/manage_phong.php`** - Quản lý tất cả phòng trong hệ thống
- **`admin/manage_chu_nha.php`** - Quản lý tất cả chủ nhà
- **`admin/manage_nguoi_thue.php`** - Quản lý tất cả người thuê
- **`admin/manage_bookings.php`** - Quản lý tất cả booking trong hệ thống

### Xem chi tiết
- **`admin/view_chu_nha.php`** - Xem chi tiết chủ nhà + danh sách phòng của họ
- **`admin/view_nguoi_thue.php`** - Xem chi tiết người thuê + danh sách booking của họ

### Xử lý xóa (Admin)
- **`admin/delete_room_admin.php`** - Xóa phòng (Admin có quyền xóa bất kỳ phòng nào)
- **`admin/delete_user.php`** - Xóa người dùng (Admin)

---

## 📁 File dành cho CHỦ NHÀ (vai_tro = 'chu_nha')

### Quản lý phòng của mình
- **`admin/manage_rooms.php`** - Quản lý phòng của chủ nhà (thêm/sửa/xóa phòng của mình)
- **`admin/edit_room.php`** - Sửa phòng của chủ nhà (chỉ phòng của mình)

### Quản lý booking
- **`admin/bookings.php`** - Quản lý đơn đặt phòng cho phòng của mình (duyệt/từ chối)

### Thống kê
- **`admin/index.php`** - Thống kê phòng và booking của chủ nhà

---

## 📁 File dành cho NGƯỜI THUÊ (vai_tro = 'nguoi_thue')

### Đặt phòng
- **`booking.php`** - Đặt phòng trọ
- **`my_bookings.php`** - Xem danh sách đơn đặt phòng của mình

### Yêu thích
- **`favorites.php`** - Xem danh sách phòng yêu thích
- **`favorite_action.php`** - API thêm/xóa phòng yêu thích (AJAX)

---

## 📁 File DÙNG CHUNG (Tất cả người dùng)

### Trang công khai
- **`index.php`** - Trang chủ (danh sách phòng, tìm kiếm, lọc)
- **`room_detail.php`** - Chi tiết phòng (mọi người đều xem được)

### Xác thực
- **`auth/login.php`** - Đăng nhập (hỗ trợ cả Admin và User)
- **`auth/register.php`** - Đăng ký (chỉ Chủ nhà/Người thuê)
- **`auth/logout.php`** - Đăng xuất

### Cấu hình & Helper
- **`config/config.php`** - Cấu hình chung, hàm helper (isAdmin, isChuNha, isNguoiThue, etc.)
- **`config/database.php`** - Kết nối database

### Template
- **`includes/header.php`** - Header (menu động theo vai trò)
- **`includes/footer.php`** - Footer

---

## 📁 File SETUP (Chỉ dùng một lần)

- **`setup_admin.php`** - Tạo tài khoản admin mặc định (sau khi import database)
- **`setup_admin_password.php`** - Hash mật khẩu admin từ SQL (nếu mật khẩu là plain text)
- **`setup_tien_ich.sql`** - SQL setup tiện ích

**⚠️ Lưu ý:** Sau khi setup xong, nên xóa hoặc vô hiệu hóa các file setup này để bảo mật.

---

## 🔐 Phân quyền truy cập

### Hàm kiểm tra quyền trong `config/config.php`:
- **`requireAdmin()`** - Chỉ Admin mới truy cập được
- **`requireChuNha()`** - Chỉ Chủ nhà mới truy cập được
- **`requireNguoiThue()`** - Chỉ Người thuê mới truy cập được
- **`requireLogin()`** - Cần đăng nhập (bất kỳ vai trò nào)

### Ví dụ sử dụng:
```php
requireAdmin(); // Chỉ Admin
requireChuNha(); // Chỉ Chủ nhà
requireNguoiThue(); // Chỉ Người thuê
requireLogin(); // Cần đăng nhập
```

---

## 📊 Tóm tắt nhanh

| Vai trò | File chính |
|---------|-----------|
| **Admin** | `admin/dashboard.php`, `admin/manage_*.php`, `admin/view_*.php` |
| **Chủ nhà** | `admin/manage_rooms.php`, `admin/edit_room.php`, `admin/bookings.php`, `admin/index.php` |
| **Người thuê** | `booking.php`, `my_bookings.php`, `favorites.php` |
| **Chung** | `index.php`, `room_detail.php`, `auth/*.php` |

---

## 🗑️ File đã xóa (không cần thiết)

Các file sau đã bị xóa vì trùng lặp hoặc không còn sử dụng:
- ❌ `admin/all_rooms.php` → Thay thế bởi `admin/manage_phong.php`
- ❌ `admin/users.php` → Thay thế bởi `admin/manage_chu_nha.php` và `admin/manage_nguoi_thue.php`
- ❌ `admin/rooms.php` → Chỉ redirect, không cần
- ❌ `admin/add_room.php` → Chỉ redirect, không cần
- ❌ `setup_password.php` → File cũ, thay thế bởi `setup_admin_password.php`

---

**Cập nhật:** $(date)

