# Booking Phòng Trọ - Python/Flask

Dự án chuyển đổi từ PHP (PhanMemBookingPhong) sang Python sử dụng Flask.

## Yêu cầu

- Python 3.8+
- PostgreSQL 12+ (đã chuyển từ MySQL)

## Cài đặt

### 1. Tạo môi trường ảo và cài đặt dependencies

```bash
cd BookingPhongPY
python -m venv .venv
.venv\Scripts\activate   # Windows
# hoặc: source .venv/bin/activate   # Linux/Mac

pip install -r requirements.txt
```

### 2. Tạo database PostgreSQL

```bash
# Tạo database
psql -U postgres -c "CREATE DATABASE bookingphong;"

# Import schema (đường dẫn tùy vị trí file)
psql -U postgres -d bookingphong -f co_so_du_lieu/schema_postgresql.sql
```

Hoặc dùng **pgAdmin**: tạo database `bookingphong`, sau đó mở và chạy file `co_so_du_lieu/schema_postgresql.sql`.

### 3. Cấu hình database

Chỉnh sửa `cau_hinh/co_so_du_lieu.py` hoặc dùng biến môi trường:

```python
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'user': 'postgres',
    'password': 'postgres',   # Mật khẩu PostgreSQL của bạn
    'dbname': 'bookingphong',
}
```

Biến môi trường: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`

### 4. Tạo tài khoản Admin (lần đầu)

```bash
python kich_ban/tao_admin.py
```

Mặc định: **admin** / **admin123** (hãy đổi mật khẩu sau khi đăng nhập!)

### 5. Chạy ứng dụng

```bash
python app.py
```

Truy cập: http://127.0.0.1:5000

## Cấu trúc dự án

```
BookingPhongPY/
├── app.py                    # Ứng dụng Flask chính
├── cau_hinh/                 # Cấu hình
│   ├── co_so_du_lieu.py      # Kết nối PostgreSQL
│   └── cai_dat.py            # Cấu hình chung
├── co_so_du_lieu/            # Schema database
│   ├── schema_postgresql.sql
│   └── schema_postgresql_trong.sql
├── tuyen/                    # Tuyến (routes)
│   ├── trang_chu.py          # Trang chủ, danh sách phòng
│   ├── xac_thuc.py          # Đăng nhập, đăng ký, đăng xuất
│   ├── phong.py             # Chi tiết phòng, đặt phòng, yêu thích
│   ├── quan_tri.py          # Quản trị (Admin + Chủ nhà)
│   └── api.py               # API (favorite toggle)
├── mau/                      # Jinja2 templates
├── tinh/                     # CSS, JS, images
├── phu_tro/                  # Hàm phụ trợ
├── uploads/                  # Ảnh phòng upload
└── kich_ban/
    └── tao_admin.py          # Tạo admin mặc định
```

## Chức năng

- **Trang chủ:** Danh sách phòng, tìm kiếm, lọc theo trạng thái/giá/diện tích
- **Chi tiết phòng:** Xem thông tin, tiện ích, đặt phòng
- **Đăng ký/Đăng nhập:** Hỗ trợ Admin và User (Chủ nhà/Người thuê)
- **Người thuê:** Đặt phòng, yêu thích, xem đơn đặt phòng
- **Chủ nhà:** Đăng phòng, quản lý phòng, duyệt đơn đặt phòng
- **Admin:** Quản lý phòng, chủ nhà, người thuê, booking

## So sánh với PHP

| PHP | Python/Flask |
|-----|--------------|
| index.php | / (main.index) |
| room_detail.php?id=X | /room/X |
| auth/login.php | /auth/login |
| admin/dashboard.php | /admin/dashboard |
| favorite_action.php | /api/favorite (POST) |
