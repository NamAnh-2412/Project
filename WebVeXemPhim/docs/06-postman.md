# Test API trên Postman (báo cáo)

Bộ request nằm ở `postman/`. Dùng để chụp màn hình báo cáo và kiểm tra JWT, API Key, giữ ghế, thanh toán giả lập, soát vé.

Nếu sửa collection bằng tay rồi muốn sinh lại từ inventory: `node postman/generate-collection.mjs`.

## Chuẩn bị

1. Cài [Postman](https://www.postman.com/downloads/) và **đăng nhập tài khoản cá nhân** (thường cần screenshot có tên user góc Postman).
2. Bật API:
   - MySQL + API: `start-all.bat` ở thư mục gốc, hoặc
   - chỉ API: trong `vexemphim-api` chạy `mvn spring-boot:run`
3. API nghe `http://localhost:8080`. DB đã seed (`seed.sql`) có user, phòng, phim.

## Import

1. Postman → **Import** → chọn:
   - `postman/CineVe.postman_collection.json`
   - `postman/CineVe.local.postman_environment.json`
2. Góc trên bên phải chọn environment **CineVe Local**.
3. (Tuỳ chọn) Đổi `customerUser` / `customerPass` sang tài khoản bạn đang dùng.

## Tài khoản sẵn có

| Vai | Username | Password |
|---|---|---|
| ADMIN | `admin` | `193850091011` |
| CUSTOMER | `customer1` | `customer123` |
| CASHIER | `NhanVien` | `NhanVien123` |

Header ghi catalog (POST/PUT/DELETE genres, movies, showtimes, rooms, concessions, settings): `X-API-KEY` = `SECRET_KEY_BTL` (đã có trong environment).

Voucher / booking / tickets / reports: chỉ cần JWT, **không** cần API Key.

## Inventory API (đã rà soát controller)

| Nhóm | Method | Path | Auth | Ghi chú |
|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | public | Trả JWT + role |
| Auth | POST | `/api/auth/register` | public | Chỉ tạo CUSTOMER |
| Auth | GET | `/api/auth/me` | JWT | |
| Genres | GET | `/api/genres`, `/{id}`, `/{id}/movies` | public | |
| Genres | POST/PUT/DELETE | `/api/genres…` | ADMIN + API Key | |
| Movies | GET | `/api/movies`, `/{id}` | public | `status`, `name`, `genreId`, pageable |
| Movies | POST/PUT/DELETE | `/api/movies…` | ADMIN + API Key | DELETE = ẩn (HIDDEN) |
| Movies | POST | `/api/movies/{id}/upload-poster` | ADMIN + API Key | `multipart file` |
| Rooms | GET | `/api/rooms`, `/{id}/seats` | public | |
| Rooms | POST/PUT | `/api/rooms…` | ADMIN + API Key | |
| Showtimes | GET | `/api/showtimes`, `/{id}` | public* | *ADMIN `includePast`; CASHIER thấy tới `endAt` + occupancy |
| Showtimes | POST/PUT/DELETE | `/api/showtimes…` | ADMIN + API Key | DELETE = `CANCELLED`, không xóa cứng |
| Concessions | GET | `/api/concessions` | public | |
| Concessions | POST/PUT | `/api/concessions…` | ADMIN + API Key | |
| Settings | GET | `/api/settings` | public | |
| Settings | PUT, POST `/logo`, `/image` | `/api/settings…` | ADMIN + API Key | upload multipart |
| Booking | GET | `/api/bookings/showtimes/{id}/seat-map` | JWT | |
| Booking | POST | `/api/bookings/hold` | CUSTOMER / CASHIER | `ONLINE` hoặc `POS` |
| Booking | POST | `/api/bookings/{id}/apply-voucher` | CUSTOMER | |
| Booking | PUT | `/api/bookings/{id}/concessions` | CUSTOMER / CASHIER | |
| Booking | GET | `/api/bookings/{id}/mock-qr` | CUSTOMER | PNG |
| Booking | POST | `/api/bookings/{id}/momo` | CUSTOMER | MoMo — khi đã cấu hình khóa |
| Booking | POST | `/api/bookings/{id}/create-payment-link` | CUSTOMER | PayOS — tuỳ chọn |
| Booking | POST | `/api/bookings/{id}/confirm` | CUSTOMER | `ONLINE_MOCK` cho demo |
| Booking | DELETE | `/api/bookings/{id}` | CUSTOMER / CASHIER / ADMIN | hủy HOLD |
| Booking | POST | `/api/bookings/{id}/cancel-tickets` | CUSTOMER | hủy vé online đã thanh toán, hoàn `totalAmount` |
| Booking | GET | `/api/notices` | ADMIN / CASHIER | thông báo hủy vé, mỗi role một danh sách |
| Booking | GET | `/api/bookings/my` | CUSTOMER | |
| Booking | GET | `/api/bookings/{id}` | JWT | |
| Booking | GET | `/api/bookings/occupancy` | ADMIN / CASHIER | `?showtimeId=` |
| POS | POST | `/api/pos/bookings/{id}/confirm-cash` | CASHIER | |
| Tickets | GET | `/api/tickets/{code}` | CASHIER / ADMIN | |
| Tickets | POST | `/api/tickets/{code}/check-in` | CASHIER / ADMIN | |
| Vouchers | GET/POST | `/api/vouchers` | ADMIN | không API Key |
| Reports | GET | `/api/reports/revenue` | ADMIN / CASHIER | `grain`, `from`, `to` |
| Payments | GET | `/api/payments/options` | đã đăng nhập | `{ momo: true }` khi có khóa |
| Payments | POST | `/api/payments/momo/ipn` | public | MoMo xác nhận vé |
| Payments | GET | `/api/payments/momo/return` | public | MoMo trả khách về trang thanh toán |
| Payments | POST | `/api/payments/webhook` | public | PayOS — không cần cho `ONLINE_MOCK` |

Upload poster / logo / image: trong Postman chọn Body → form-data → key `file` (type File). Không gói sẵn binary trong collection.

## Thứ tự chạy để chụp báo cáo

Chạy folder **05 Demo báo cáo (chạy lần lượt)** từ trên xuống:

| Bước | Request | Kỳ vọng |
|---|---|---|
| A0 | List movies + rooms | `200`, lưu `movieId` / `roomId` |
| A1 | Login Admin | `200`, có `token` |
| A2 | Create showtime ngày mai | `201`, lưu `showtimeId` |
| A3 | Login Customer | `200` |
| A4 | Seat map | `200`, tự chọn ghế `AVAILABLE` |
| A5 | Hold ONLINE | `201`, lưu `reservationId` |
| A6 | Confirm ONLINE_MOCK | `200`, `status=CONFIRMED`, có `ticketCode` |
| A7 | Login Cashier | `200` |
| A8 | Check-in | `200`, `CHECKED_IN` |

Ảnh lỗi — folder **06 Ảnh lỗi**:

| Request | Kỳ vọng | Ý nghĩa |
|---|---|---|
| E1 Login sai mật khẩu | `401 AUTH_FAILED` | Bảo mật đăng nhập |
| E2 Create showtime thiếu API Key | `401 API_KEY_INVALID` | API Key khi ghi catalog |
| E3 Hold trùng ghế | `409 SEAT_TAKEN` | Khóa ghế hai kênh |
| E4 Check-in lần 2 | `409 ALREADY_CHECKED_IN` | Soát vé một lần |

E3 cần chạy khi ghế vừa HOLD/BOOKED (ngay sau A5, hoặc Hold ONLINE ở folder 03). E4 chạy sau A8.

## Lưu ý khi demo

- Folder demo **tự tạo suất ngày mai**, không phụ thuộc lịch cũ đã qua giờ.
- Nếu A2 trả `409 SHOWTIME_OVERLAP`, đổi `roomId` trong environment (thử `2`) rồi chạy lại A2.
- Request **Cancel hold** chỉ dùng khi đơn còn `HOLD`. Đã confirm rồi thì bỏ qua.
- PayOS webhook / create-payment-link không cần cho báo cáo nếu demo bằng `ONLINE_MOCK`.
- Ô đăng nhập Postman phải hiện tên tài khoản của bạn trước khi chụp.
- **Cancel showtime** (DELETE) chỉ chạy trên suất demo không còn dùng; suất đã bán vé → `409 HAS_TICKETS`.

## Collection theo folder

| Folder | Nội dung |
|---|---|
| 00 Auth | Login 3 role, me, register, login sai |
| 01 Catalog | GET công khai movies/genres/rooms/showtimes/concessions/settings |
| 02 Admin | CRUD catalog + suất + voucher + doanh thu + thiếu API key |
| 03 Booking ONLINE | Seat map → hold → SEAT_TAKEN → bắp/voucher → mock QR → confirm → my |
| 04 POS + Soát vé | Hold POS (tự lấy ghế trống) → cash → lookup → check-in → occupancy |
| 05 Demo báo cáo | A0→A8 — luồng chính để chụp |
| 06 Ảnh lỗi | 401 / 409 cho phần âm tính |

## Checklist ảnh báo cáo gợi ý

1. Postman đã login, collection CineVe mở sẵn  
2. `POST /api/auth/login` admin — 200  
3. `POST /api/showtimes` có header `Authorization` + `X-API-KEY` — 201  
4. `POST /api/showtimes` thiếu API Key — 401  
5. `POST /api/bookings/hold` ONLINE — 201  
6. Hold trùng ghế — 409 `SEAT_TAKEN`  
7. `POST /api/bookings/{id}/confirm` — 200 + mã `VX-...`  
8. `POST /api/tickets/{code}/check-in` — 200  
9. Check-in lần 2 — 409 `ALREADY_CHECKED_IN`  
10. `GET /api/reports/revenue` — 200  
