# Booking Phòng Trọ

Web đặt phòng trọ viết bằng Flask 3 và PostgreSQL. Một phòng chỉ có một người thuê. Người thuê tìm phòng, hẹn xem, đặt cọc giữ chỗ. Chủ nhà duyệt tin, nhận đơn, lập hợp đồng và xuất hóa đơn. Admin duyệt tin, khóa tài khoản và theo dõi tiền.

Đăng nhập bằng session (`user_id` cho người dùng, `admin_id` cho quản trị). Admin và người dùng là hai bảng khác nhau. Admin đăng nhập bằng tên đăng nhập, không bằng email, và không đi qua xác thực Gmail.

## Chức năng nổi bật

- **Một phòng không bị đặt trùng.** `dich_vu/dat_phong_service.py` khóa dòng phòng bằng `SELECT … FOR UPDATE` trong một transaction. Hai người bấm đặt cùng lúc chỉ một đơn được giữ. Có test hai luồng trên PostgreSQL thật.
- **Sổ tiền tách khỏi trạng thái đơn.** Cọc và tiền thuê nằm ở `giao_dich_thanh_toan`. Xác nhận cọc lần hai không tạo thêm dòng. Mã giảm chỉ trừ tiền thuê tháng đầu, không trừ tiền cọc.
- **Giữ chỗ có hạn.** Đơn chờ quá hạn mà chưa nhận cọc thì tiến trình lịch tự hủy. Bản dev gắn lịch vào `python app.py`. Bản nhiều người chạy lịch ở một tiến trình riêng, khóa advisory của Postgres để khỏi chạy trùng.
- **VietQR.** Trang thanh toán hiện mã chuyển khoản. Webhook nhận xác nhận khi có đúng `X-Webhook-Secret`.
- **Email xác thực trước khi đặt và đánh giá.** Duyệt phòng, yêu thích và chat không bắt buộc xác thực email. Link xác thực sống 24 giờ. Gửi lại bị giới hạn 8 lần mỗi phút.
- **Lịch xem phòng** theo ba khung 09:00, 14:00, 18:00. Chủ nhà chấp nhận hoặc từ chối.
- **Hợp đồng và hóa đơn tháng**, in trên web và xuất PDF tiếng Việt.
- **Chat theo phòng**, có thẻ phòng. Tab đang mở hỏi tin mới mỗi 4 giây rồi giãn tới 12 giây nếu không có tin. Tab ẩn thì 20 giây. API bị giới hạn 40 lần lấy và 30 lần gửi mỗi phút.
- **Duyệt tin, báo cáo, khóa tài khoản, tiện ích, banner, mã giảm, gói VIP.**
- **Giao diện công khai:** giá kiểu “2,1 triệu”, huy hiệu còn trống hoặc đang có người hẹn xem, gallery và hộp ảnh, cột giá và phí dính khi cuộn, chế độ sáng tối nhớ trong `localStorage` khóa `chu-de`.

## Chạy trên máy dev

Cần Python 3.11+ và PostgreSQL 12+. Máy này đang dùng Python 3.13 và PostgreSQL 18.

```bash
cd BookingPhongPY
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Điền `DB_PASSWORD` và `SECRET_KEY` trong `.env`. Mật khẩu database không nằm trong mã nguồn.

Database mới:

```bash
psql -U postgres -c "CREATE DATABASE bookingphong;"
python kich_ban/migrate.py
python kich_ban/tao_admin.py
python app.py
```

`migrate.py` thấy chưa có bảng `nguoi_dung` thì nạp `schema_postgresql_trong.sql`, rồi chạy toàn bộ file trong `co_so_du_lieu/migrations`. Database đã có bảng thì chỉ chạy migrations. File `schema_postgresql.sql` có dữ liệu mẫu và có `DROP TABLE`. Chỉ chạy tay trên database trống khi muốn dữ liệu demo.

Mở http://127.0.0.1:5000

Lần đầu trên máy dev, `tao_admin.py` tạo `admin` / `admin123` nếu chưa có admin. Chạy lại script mà không đặt `ADMIN_PASSWORD` thì không ghi đè mật khẩu đang có. Console sẽ cảnh báo nếu mật khẩu vẫn là `admin123`. Bản dev được phép dùng mật khẩu đó.

`python app.py` bật debug, một tiến trình, lịch nhắc việc chạy trong process con của reloader. Giới hạn request nằm trong bộ nhớ và mất khi tắt server. Đủ cho một người phát triển.

Kiểm thử:

```bash
python -m unittest tests.test_nang_cap tests.test_khoa_don
```

## Chạy nhiều người

`docker compose up -d --build` rồi mở http://127.0.0.1:8080 . Postgres trong container lắng nghe cổng 5433 trên máy host để không đè Postgres đang chạy sẵn.

Trong chế độ này:

- `web` chạy Gunicorn. Số worker lấy từ `WEB_WORKERS` (mặc định 3), mỗi worker 2 thread. Mỗi process giữ pool tối đa `DB_POOL_MAX` kết nối (mặc định 8).
- `redis` giữ bộ đếm giới hạn đăng nhập, gửi lại email và chat, dùng chung cho mọi worker. Redis không ghi đĩa. Redis lỗi thì giới hạn rơi về bộ nhớ của từng worker, trang không trả 500 chỉ vì mất Redis.
- `lich` là một tiến trình, không phục vụ HTTP. Nó hủy đơn quá hạn mỗi phút và nhắc lịch xem cùng hóa đơn nợ lúc 08:00 giờ Việt Nam.
- `nginx` trả thẳng `/tinh` và `/uploads`, phần còn lại chuyển tới Gunicorn. Có header `X-Forwarded-For`. App chỉ tin header đó khi `BEHIND_PROXY=1`.
- `GET /suc-khoe` trả `{"ok": true}` khi database trả lời được.
- `FLASK_ENV=production` thì app từ chối khởi động nếu `SECRET_KEY` còn là chuỗi mẫu hoặc ngắn hơn 24 ký tự, hoặc còn admin đăng nhập được bằng `admin123`. Đặt `ADMIN_PASSWORD` (ít nhất 10 ký tự, khác `admin123`) rồi chạy `python kich_ban/tao_admin.py` trước.
- Cookie phiên có `HttpOnly` và `SameSite=Lax`. `COOKIE_SECURE=1` chỉ khi site đã có HTTPS. Bản Nginx cổng 8080 đang là HTTP, để `0`.

Google OAuth, SMTP và VietQR chỉ chạy khi `.env` có đủ biến tương ứng. Chưa có SMTP thì trang quên mật khẩu và xác thực email hiện link ngay trên trang nếu không phải production.

## Cây thư mục

```
BookingPhongPY/
├── app.py
├── wsgi.py
├── gunicorn.conf.py
├── requirements.txt
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── .gitignore
├── README.md
├── VIEC_CON_LAI.md
├── KE_HOACH_REFACTOR.md
├── cau_hinh/
│   ├── cai_dat.py
│   ├── co_so_du_lieu.py
│   └── van_hanh.py
├── dich_vu/
│   ├── dat_phong_service.py
│   ├── phong_dang_service.py
│   ├── thong_ke_service.py
│   ├── email_service.py
│   ├── hoa_don_service.py
│   ├── lich_trinh.py
│   ├── payment_service.py
│   ├── thong_bao_service.py
│   └── upload_service.py
├── tuyen/
│   ├── trang_chu.py
│   ├── xac_thuc.py
│   ├── xac_thuc_mail.py
│   ├── phong.py
│   ├── dat_cho.py
│   ├── quan_tri.py
│   ├── chu_nha_phong.py
│   ├── quan_tri_mo_rong.py
│   ├── chu_nha_pms.py
│   ├── tuong_tac.py
│   ├── hoi_thoai.py
│   ├── tai_khoan.py
│   └── api.py
├── phu_tro/
│   ├── bao_mat.py
│   ├── decorators_xac_thuc.py
│   ├── mo_rong.py
│   └── tro_giup.py
├── kich_ban/
│   ├── migrate.py
│   ├── tao_admin.py
│   └── chay_lich.py
├── co_so_du_lieu/
│   ├── schema_postgresql.sql
│   ├── schema_postgresql_trong.sql
│   └── migrations/
│       ├── 001_nang_cap.sql
│       ├── 002_khoa_don.sql
│       ├── 003_so_tien.sql
│       ├── 004_xac_thuc_va_chat.sql
│       ├── 005_banner_ma_giam.sql
│       └── 006_chi_muc_chat.sql
├── mau/
├── tinh/
├── deploy/nginx.conf
├── tests/
└── uploads/
```

Mỗi thư mục Python có `__init__.py` rỗng để thành package. `uploads/` chứa ảnh phòng và ảnh đại diện đã nén WebP. Thư mục này không đưa vào Git và không copy vào image Docker.

## Từng file ở gốc

- `app.py` tạo ứng dụng Flask, gắn blueprint, CSRF, giới hạn request, OAuth Google. Context processor đưa user, quyền, đếm thông báo và hàm định dạng tiền vào mọi template. `before_request` đá người dùng bị khóa. `/uploads/…` phục vụ ảnh. `/suc-khoe` để kiểm tra sống. `python app.py` bật debug cổng 5000 và khởi động lịch.
- `wsgi.py` là object `app` cho Gunicorn. Không khởi động lịch, để nhiều worker không mỗi đứa một bộ hẹn giờ.
- `gunicorn.conf.py` đọc `WEB_WORKERS`, `WEB_THREADS`, cổng bind. Worker tái tạo sau khoảng 800 request để nhả bộ nhớ.
- `requirements.txt` khóa phiên bản Flask, Postgres, giới hạn request, ảnh, PDF, Gunicorn, Redis, APScheduler.
- `Dockerfile` image Python 3.12, font DejaVu cho PDF, lệnh mặc định là migrate rồi Gunicorn.
- `docker-compose.yml` năm service: `db`, `redis`, `web`, `lich`, `nginx`.
- `.dockerignore` chặn `.env`, `.venv`, `uploads` khỏi image.
- `.env.example` là mẫu biến môi trường, không có mật khẩu thật.
- `VIEC_CON_LAI.md` ghi phần còn thiếu so với sàn đang có trên thị trường.
- `KE_HOACH_REFACTOR.md` là kế hoạch dọn code, chưa thực hiện.

## cau_hinh

- `cai_dat.py` đọc `.env`: khóa bí mật, thư mục upload, Google, webhook.
- `co_so_du_lieu.py` pool kết nối theo process. Trong request, một kết nối được mượn và trả về pool khi request xong. Ngoài request thì mở kết nối riêng. `giao_dich()` tắt autocommit để nhiều câu SQL cùng commit hoặc cùng rollback. Pool mặc định autocommit bật, nên câu lẻ không cần commit.
- `van_hanh.py` chặn `SECRET_KEY` yếu và mật khẩu admin `admin123` khi chạy production. Bản dev chỉ in cảnh báo.

## dich_vu

- `dat_phong_service.py` là nơi duy nhất tạo đơn, xác nhận, hủy, ghi cọc, tạo hợp đồng và hủy đơn quá hạn. Database là bên quyết định còn trống hay đã thuê: có hợp đồng hiệu lực hoặc đơn đã xác nhận thì phòng thành `da_thue`.
- `phong_dang_service.py` ghi tin phòng mới và cập nhật tin của chủ nhà, gồm tiện ích và ảnh.
- `thong_ke_service.py` đếm số liệu dashboard của admin và của chủ nhà.
- `payment_service.py` tạo nội dung chuyển khoản `COCPHONG{id}` và URL VietQR từ cấu hình ngân hàng.
- `email_service.py` gửi SMTP, cổng 587 STARTTLS. Mật khẩu ứng dụng Gmail được bỏ khoảng trắng.
- `hoa_don_service.py` tính điện, nước, tiền phòng, dịch vụ, và vẽ PDF.
- `lich_trinh.py` hai job: mỗi phút hủy đơn quá hạn, 08:00 nhắc lịch xem trong ngày và hóa đơn chưa đóng.
- `thong_bao_service.py` ghi một dòng thông báo trong web.
- `upload_service.py` lưu ảnh WebP, đặt ảnh chính, xóa file khi xóa phòng.

## tuyen

- `trang_chu.py` trang chủ: tìm theo chữ, giá, diện tích, quận, loại phòng, tiện ích, sắp xếp, phân trang 9 phòng. Chip loại phòng giữ các bộ lọc khác.
- `xac_thuc.py` đăng ký, đăng nhập, đăng xuất. Mật khẩu mới hash bằng Werkzeug. Hash bcrypt cũ của PHP (`$2y$`) vẫn kiểm tra được.
- `xac_thuc_mail.py` quên mật khẩu, đặt lại mật khẩu, Google, gửi và xác nhận email. Cùng blueprint `auth`, URL không đổi.
- `phong.py` chi tiết phòng, form đặt phòng, đơn của tôi, phòng yêu thích.
- `dat_cho.py` đặt lịch xem, lịch của tôi, trang chuyển khoản, chủ nhà xác nhận cọc, webhook thanh toán.
- `quan_tri.py` dashboard admin, danh sách phòng, chủ nhà, người thuê, đơn.
- `chu_nha_phong.py` chủ nhà xem thống kê, đăng phòng, sửa phòng, duyệt đơn của mình. Cùng blueprint `admin`.
- `quan_tri_mo_rong.py` duyệt tin chờ, báo cáo, tiện ích, xác minh tay, khóa tài khoản, VIP, tài khoản ngân hàng, banner, mã giảm, nhật ký admin.
- `chu_nha_pms.py` tiền tố `/chu-nha`: lịch hẹn, hợp đồng, hóa đơn, bản in, PDF, đánh dấu đã đóng.
- `tuong_tac.py` đánh giá, trả lời đánh giá, hỏi đáp, báo cáo tin, thông báo.
- `hoi_thoai.py` danh sách chat, một cuộc chat, và hai hàm API lấy/gửi tin. `api.py` gọi lại hai hàm đó.
- `tai_khoan.py` hồ sơ: tên, số điện thoại, số CCCD dạng chữ.
- `api.py` bật tắt yêu thích và hai đầu chat cho JavaScript. Lỗi yêu thích không trả câu lỗi database ra trình duyệt.

## phu_tro

- `decorators_xac_thuc.py` `require_login`, `require_admin`, `require_chu_nha`, `require_nguoi_thue`. User và admin được cache trên `g` trong một request. Hàm đưa vào template là `is_admin()` và `is_chu_nha()`. Không truyền biến cùng tên vào `render_template`, nếu không template gọi hàm sẽ vỡ.
- `mo_rong.py` CSRF, Limiter, OAuth. Nơi lưu bộ đếm lấy từ `RATELIMIT_STORAGE_URI` lúc khởi tạo app.
- `bao_mat.py` cắt chuỗi, bỏ ký tự null, ép số.
- `tro_giup.py` nhãn trạng thái, phân trang, câu SQL ảnh đầu tiên, `dinh_dang_tien` (15000000 thành “15 triệu”), `url_anh` đổi đường dẫn lưu trong database thành URL, và `anh_va_lich_hen` lấy tối đa 4 ảnh cùng phòng đang có lịch xem sắp tới.

## kich_ban

- `migrate.py` nạp schema trống nếu database trắng, rồi chạy migrations theo tên file.
- `tao_admin.py` tạo hoặc đổi mật khẩu admin. Production bắt buộc `ADMIN_PASSWORD`. Script không in mật khẩu ra màn hình, trừ lần tạo đầu trên máy dev với `admin123`.
- `chay_lich.py` tiến trình lịch cho Docker. Vòng lặp ngủ để process không thoát.

## co_so_du_lieu

- `schema_postgresql.sql` schema gốc kèm vài dòng dữ liệu mẫu. Có `DROP TABLE`. Không chạy trên database đang có người dùng.
- `schema_postgresql_trong.sql` cùng bảng gốc, không có mẫu. `migrate.py` dùng file này khi database trắng.
- `001_nang_cap.sql` thêm CCCD, Google, duyệt tin, giá điện nước, lịch xem, đánh giá, hỏi đáp, báo cáo, thông báo, chat, hợp đồng, hóa đơn, chỉ mục tìm kiếm.
- `002_khoa_don.sql` hạn giữ chỗ, giá chốt, mã giảm trên đơn, chỉ mục chống đặt trùng.
- `003_so_tien.sql` sổ `giao_dich_thanh_toan`.
- `004_xac_thuc_va_chat.sql` cờ email đã xác thực và thẻ phòng trong chat.
- `005_banner_ma_giam.sql` banner trang chủ và mã giảm.
- `006_chi_muc_chat.sql` chỉ mục `(phong_id, id)` cho lần hỏi tin mới.

## mau

Template Jinja. `base.html` là khung: chống nháy khi đổi chủ đề, Font Awesome, SweetAlert2, thông báo flash, CSS chủ đề nạp sau cùng để thắng các file khác.

`includes/header.html` menu và nút Tối / Sáng. `includes/footer.html` chân trang. `includes/phan_trang.html` số trang. `includes/the_phong.html` một thẻ phòng: ảnh trượt, huy hiệu, tim yêu thích, giá.

`index.html` bộ lọc và lưới phòng. `rooms/room_detail.html` gallery, mô tả dạng chữ thường, cột đặt cọc và lịch xem. `rooms/booking.html` form đặt. `rooms/my_bookings.html` đơn của người thuê. `rooms/favorites.html` phòng đã lưu.

`auth/login.html`, `register.html`, `quen_mat_khau.html`, `dat_lai_mat_khau.html`, `xac_thuc_email.html`.

`tai_khoan/ho_so.html` sửa hồ sơ.

`dat_cho/thanh_toan.html` VietQR và nội dung chuyển khoản. `dat_cho/lich_cua_toi.html` lịch xem của người thuê.

`tuong_tac/chat_ds.html` danh sách hội thoại. `tuong_tac/chat.html` một cuộc chat và đoạn hỏi tin mới. `tuong_tac/thong_bao.html` hộp thông báo.

`chu_nha/lich_hen.html`, `hop_dong.html`, `hoa_don.html`, `hoa_don_in.html`.

`admin/dashboard.html` số liệu. `manage_phong.html`, `manage_chu_nha.html`, `view_chu_nha.html`, `manage_nguoi_thue.html`, `view_nguoi_thue.html`, `manage_bookings.html` là màn admin. `chu_nha_index.html`, `manage_rooms.html`, `edit_room.html`, `chu_nha_bookings.html` là màn chủ nhà. `duyet_tin.html`, `bao_cao.html`, `tien_ich.html`, `chuyen_khoan.html`, `banner.html`, `ma_giam.html` là màn mở rộng.

## tinh

URL tĩnh là `/tinh/…` vì Flask lấy tên thư mục `tinh` làm đường dẫn.

- `css/style.css` import `base`, `header`, `buttons`, `forms`, `auth`, `rooms`, `components`, `responsive`, rồi `nang-cap` và `giao-dien`.
- `css/nang-cap.css` chỉnh thêm sau đợt nâng cấp giữa.
- `css/giao-dien.css` chip loại phòng, ảnh trượt, huy hiệu, gallery, cột dính trên trang chi tiết.
- `css/chu-de.css` biến màu cho `html[data-chu-de="toi"]`. Bản in luôn sáng.
- `css/admin.css` chỉ các trang admin gắn thêm.
- `js/chu-de.js` đổi sáng tối, nhớ `localStorage`.
- `js/giao-dien.js` toast, trượt ảnh, hộp ảnh phóng to, đổi flash thành toast.
- `js/favorites.js` tim yêu thích, gọi `/api/favorite`.
- `js/password-toggle.js` hiện mật khẩu trên form đăng nhập và đăng ký.
- `images/no-image.svg` ảnh thế chỗ khi phòng chưa có ảnh.

## tests

- `conftest.py` đặt `TESTING=1` để tắt giới hạn request và không chạy lịch khi import app.
- `test_nang_cap.py` hóa đơn, phân trang, làm sạch chuỗi, mã cọc, mật khẩu SMTP bỏ khoảng trắng, VietQR thiếu số tài khoản, trang chủ, CSRF, `/suc-khoe`, khóa bí mật mẫu.
- `test_khoa_don.py` hai khách đặt một phòng, mã giảm, ghi cọc một lần, hủy đơn quá hạn. Test này ghi vào database `bookingphong` rồi xóa dữ liệu của chính nó.

## deploy

`deploy/nginx.conf` phục vụ `/tinh/` và `/uploads/` từ đĩa, chuyển request còn lại tới `web:5000`, giới hạn upload 8 MB khớp `MAX_CONTENT_LENGTH` của Flask.
