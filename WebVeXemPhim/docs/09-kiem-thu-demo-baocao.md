# 09. Kiểm thử, demo giảng viên, dàn ý báo cáo

Postman: **đăng nhập tài khoản cá nhân** (SOS01). Ảnh test phải thấy user góc phải nếu thầy bắt nộp screenshot theo lab.

## 1. Kiểm thử API (Postman)

Collection (tạo khi code):

**Auth / RBAC**

1. Register + login 3 role → lưu JWT.
2. Login sai mật khẩu → 401.
3. POST genre không token → 401; token CUSTOMER → 403; ADMIN thiếu `X-API-KEY` → 401; đủ JWT+key → 201.

**Catalog (SOS05 + SOS07)**

4. POST 2 genre, POST movie gắn `category`/`genre.id`.
5. GET `/api/genres/{id}/movies`.
6. GET `/api/movies?name=...&page=0&size=2&sort=title,asc` — `content`, `totalPages`.
7. name không khớp → `content=[]`.
8. Upload poster ADMIN → 200, file trong `uploads/`; CUSTOMER → 403; file rỗng → 400.
9. POST suất chồng giờ cùng phòng → 409 `SHOWTIME_OVERLAP`.
9b. GET `/api/movies?status=COMING` → chỉ phim sắp chiếu. POST suất cho phim `COMING` → 400 `MOVIE_NOT_ON_SALE`. Chuyển phim đang có suất sang `COMING` rồi hold → cũng 400 `MOVIE_NOT_ON_SALE`.

**Booking**

10. Seat-map có token.
11. Hold A5 customer → 201.
12. Hold trùng A5 cashier → **409 `SEAT_TAKEN`**.
13. Confirm mock → tickets.
14. GET `/api/bookings/my` (không `?userId=`).
15. GET booking của người khác bằng token customer → **403**.
16. Hold POS → confirm-cash → 200.
17. DELETE hold → 204; seat-map AVAILABLE.
18. Hold rồi hết hạn (profile `demo` 60s) → confirm 409 `HOLD_EXPIRED`.

**CRUD HTTP như SOS03** (phim/genre): 200, 201, 204, 404, 400 sai JSON.

## 2. Kiểm thử UI

- Đăng ký, login, tìm phim, đổi trang, đặt 2 ghế, mã vé.
- Tab Sắp chiếu: thẻ phim chỉ có nút **Thông tin**, trang chi tiết không hiện lịch chiếu.
- Màu ghế đúng chốt ở [docs/05 §3.1](05-luong-nghiep-vu.md): chọn = vàng, người khác giữ / đã bán = xám, VIP = viền đỏ.
- Chọn quá 8 ghế → chặn kèm thông báo.
- Stepper 4 bước sáng đúng màn (Suất → Ghế → Thanh toán).
- Ẩn danh: POS cùng suất, ghế BOOKED không chọn.
- Admin thêm suất, khách F5 thấy suất mới.
- Admin upload poster, khách thấy ảnh.
- CUSTOMER gõ `/pos` bị đẩy về trang khách.
- Soát vé mã đúng / sai.

## 3. Kịch bản demo 8–10 phút

1. Sơ đồ monolith + 3 module + 2 kênh (1 phút).
2. Postman 409 bán trùng (2 phút).
3. Đặt online ra vé (2 phút).
4. POS in (print preview) (1–2 phút).
5. Admin: page/search hoặc upload + API Key (1 phút) — chứng SOS07/10.
6. Nêu: `.bat` chỉ chạy; SOA là REST + module, không phải 4 JAR.

Profile `demo`: hold 60 giây.

Không còn bắt buộc “tắt catalog-service 503” (không còn process đó). Thay bằng 409 + mất kết nối cả API nếu muốn.

## 4. Dàn ý báo cáo môn

1. Đặt vấn đề: bán vé hai kênh, cần API dùng chung.
2. Tên đề tài và phạm vi (API + client).
3. Kiến trúc: Modular monolith, so 3 tầng SOS, so microservice CRS, sơ đồ.
4. Thiết kế module: auth, catalog, booking; 3 tầng; 1 DB; quan hệ 1-N.
5. Hợp đồng REST; hold/confirm; Page; upload; API Key.
6. Client: tiêu thụ API, POS vs online vs admin.
7. Bảo mật: JWT nâng SOS06; IDOR; API Key SOS10.
8. Triển khai: Java/MySQL/React, `.bat`.
9. Kiểm thử: Postman, UI.
10. Hạn chế và hướng mở: tách microservice, payment-service, Docker.

Tránh chương chỉ mô tả màn hình mà không có API/module.

## 5. Tiêu chí tự chấm trước khi nộp

- [ ] Client không JDBC; chỉ `:8080`
- [ ] Hai kênh cùng hold; 409 trùng ghế
- [ ] JWT userId, không IDOR `/my`, không `?role=`
- [ ] Một DB; Genre 1-N Movie
- [ ] Search/page phim; upload poster ADMIN
- [ ] X-API-KEY khi ghi catalog
- [ ] Package đúng SOS01 khi tạo project
- [ ] Tên đề tài có hướng dịch vụ / API–Client
