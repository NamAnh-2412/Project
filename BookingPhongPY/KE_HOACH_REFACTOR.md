# Kế hoạch refactor

Đã làm ngày 24/10/2026: xóa file tĩnh không được gọi, gỡ Flask-Login, bỏ `conn.close()` trong route, chuyển `image_url` sang `url_anh`, tách route admin / chủ nhà / chat / mail, đưa SQL dashboard và đăng phòng vào service. CSS `nang-cap` và `giao-dien` được import từ `style.css`, không viết lại `rooms.css`. Hai file schema gốc giữ nguyên vì file có dữ liệu mẫu còn `DROP TABLE`. Nhánh đăng nhập admin so mật khẩu thô vẫn giữ, vì chưa đối chiếu hết các dòng mật khẩu trong bảng `admin`.

Rà soát ngày 24/10/2026 trên cây mã hiện tại. Làm từng đợt, mỗi đợt chạy `python -m unittest tests.test_nang_cap tests.test_khoa_don` và mở lại trang chủ, chi tiết phòng, đăng nhập.

Code hiện đã chia blueprint và gom đặt phòng vào `dich_vu/dat_phong_service.py`. Phần tiền, khóa phòng và mã giảm nên giữ nguyên hành vi. Chỗ dài nằm ở route và CSS, không phải vì dự án bị rối toàn bộ.

## Xóa được, không đổi hành vi

Các file sau không được template hay Python nào nạp:

| File | Vì sao xóa được |
| --- | --- |
| `tinh/js/room-images.js` | Không trang nào gắn script. Hàm `changeMainImage` tìm `#mainImage`, phần tử này đã bỏ khi trang chi tiết chuyển sang gallery. |
| `tinh/js/image-preview.js` | Không trang nào gọi `initImagePreview`. Form đăng phòng vẫn gửi file bình thường, không phụ thuộc file này. |
| `tinh/css/bookings.css` | Không file CSS nào `@import`, không template nào link. |

`Flask-Login` nằm trong `requirements.txt` nhưng không có dòng `import flask_login` trong mã dự án. Gỡ khỏi requirements ở đợt dọn phụ thuộc. Đừng gỡ `redis`: bản Docker dùng nó cho giới hạn request.

Giữ các file trông giống thừa nhưng vẫn đang dùng:

- `tinh/css/base.css`, `header.css`, `buttons.css`, `forms.css`, `auth.css`, `rooms.css`, `components.css`, `responsive.css` được `style.css` import.
- `co_so_du_lieu/schema_postgresql.sql` và `schema_postgresql_trong.sql` là schema gốc. `kich_ban/migrate.py` chỉ chạy file trống khi database chưa có bảng `nguoi_dung`. File có dữ liệu mẫu còn câu `DROP TABLE`. Không xóa, không chạy file đó lên database đang dùng.
- `tinh/css/chu-de.css` có selector `.image-preview`. Xóa file JS xem trước ảnh thì có thể xóa nốt selector này, không xóa cả file chủ đề.

## Chưa gọn, làm được mà không thêm chức năng

### 1. Route vừa điều phối vừa viết SQL

`tuyen/quan_tri.py` khoảng 470 dòng, vừa danh sách admin vừa đăng phòng của chủ nhà. `tuyen/tuong_tac.py` khoảng 320 dòng, `tuyen/xac_thuc.py` khoảng 330 dòng, `tuyen/phong.py` và `tuyen/quan_tri_mo_rong.py` mỗi file hơn 240 dòng.

Cách làm: tách SQL lặp sang service, theo kiểu `tao_don` đã làm. Route chỉ đọc form, gọi service, chọn template. Tách file theo vai trò, ví dụ danh sách admin riêng và form phòng của chủ nhà riêng, vẫn đăng ký cùng blueprint để URL không đổi.

Không gom mọi thứ vào một file “utils”.

### 2. `conn.close()` trong request không đóng kết nối

Trong một request, `get_db_connection()` trả về lớp bọc. `close()` của lớp đó không trả kết nối về pool. Pool được trả ở `giai_phong_ket_noi` khi request kết thúc. Nhiều route vẫn gọi `conn.close()` rồi dùng tiếp cursor, dễ hiểu nhầm là đã đóng.

Đợt sau: bỏ các lời gọi `close()` nằm giữa request. Giữ `close()` trong script ngoài request (`tao_admin.py`, job lịch, test).

### 3. `app.py` ôm helper giao diện

`image_url` là một lambda dài trong context processor. Chuyển thành hàm trong `phu_tro/tro_giup.py`. Context processor chỉ việc đưa hàm vào template.

Đếm thông báo đang cache 20 giây trong session. Giữ nguyên khi tách, và nhớ `session.pop('_tb')` ở trang thông báo.

### 4. CSS chồng lớp

`rooms.css` là file dài nhất phần giao diện. `nang-cap.css` và `giao-dien.css` vá thêm lên trên. Chưa gộp một lần, vì dễ vỡ trang chủ và trang chi tiết. Đợt CSS chỉ làm sau khi chụp lại trang chủ, chi tiết phòng, đăng nhập, chế độ tối.

### 5. Hai file schema gần giống nhau

`schema_postgresql.sql` có dữ liệu mẫu và `DROP TABLE`. `schema_postgresql_trong.sql` là cùng cấu trúc, không có mẫu. Khi sửa cột gốc phải sửa hai nơi, rồi còn migrations. Đợt sau: một file cấu trúc không có `DROP`, một file seed tùy chọn, migrations chỉ việc thêm cột mới. Làm trên database trống trước, đối chiếu với database hiện tại, không chạy đè.

### 6. Đăng nhập admin còn so sánh mật khẩu thô

`tuyen/xac_thuc.py` chấp nhận `admin['mat_khau'] == password` rồi mới hash lại. Đó là đường nâng cấp từ mật khẩu cũ lưu thẳng. Khi chắc không còn dòng mật khẩu thô trong bảng `admin`, xóa nhánh so sánh đó và thêm test đăng nhập sai mật khẩu.

### 7. Test còn mỏng

Đã có test hai người đặt một phòng, hóa đơn, CSRF, trang chủ, `/suc-khoe`. Chưa có test webhook, chat, duyệt tin, hoàn cọc. Viết test cho những đường đó trước khi sửa `dat_phong_service.py`.

## Thứ tự nên làm

1. Xóa ba file tĩnh không được gọi và gỡ Flask-Login khỏi requirements. Chạy test, mở trang chủ.
2. Bỏ `conn.close()` thừa trong route. Không đổi SQL.
3. Đưa `image_url` ra helper. Mở một ảnh phòng và ảnh mặc định.
4. Tách SQL của `quan_tri.py` trước, vì file dài nhất và vừa sửa lỗi lộ exception ra màn hình.
5. Tách `tuong_tac.py` và `xac_thuc.py`.
6. Gộp CSS chỉ khi có ảnh trước và sau của trang chủ, chi tiết phòng và chế độ tối.
7. Gộp schema trên database bản sao, không đụng database đang chạy.

Mỗi đợt một commit riêng khi được yêu cầu commit. Không đổi URL, không đưa bản đồ trở lại, không viết lại sang framework khác.
