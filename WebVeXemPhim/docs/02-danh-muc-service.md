# 02. Danh mục module

Một ứng dụng Spring Boot (`vexemphim-api`). Frontend không phải “service SOA” — đó là **client**.

Package gốc khi code: đổi từ placeholder `btl.vexemphim` sang `ten_lop.tenkhongdauXXX.vexemphim` (SOS01).

Cấu trúc (SOS05: 3 tầng trong nhóm chức năng; BTL = module):

```
vexemphim-api/src/main/java/{package}/
  VexemphimApplication.java
  auth/
    controller/   AuthController
    service/      AuthService
    repository/   UserRepository
    entity/       User
  catalog/
    controller/   GenreController, MovieController, RoomController, ShowtimeController, ConcessionController, CinemaSettingsController
    service/      GenreService, MovieService, RoomService, ShowtimeService, FileStorageService, ConcessionService, CinemaSettingsService
    repository/
    entity/       Genre, Movie, Room, Seat, Showtime, ConcessionProduct, CinemaSettings
  booking/
    controller/   BookingController, PosBookingController, TicketController, ReportController
    service/      BookingService, HoldExpiryScheduler, ReportService
    repository/
    entity/       Reservation, ReservationSeat, ReservationConcession, Ticket, Payment
  common/
    config/       CorsConfig, ApiKeyInterceptor, ApiKeyConfig, StaticResourceConfig
    security/     JwtAuthFilter, SecurityConfig, JwtService
    exception/    GlobalExceptionHandler
```

Không tạo `api-gateway`, `auth-service`, `catalog-service`, `booking-service` riêng process.

---

## 1. common (không có bảng nghiệp vụ)

**Nhiệm vụ**

- CORS: `http://localhost:5173` (và `http://127.0.0.1:5173`). SOS08 dùng 5500 — BTL không cần trừ khi test HTML phụ.
- JWT filter (mọi path trừ public).
- API Key interceptor trên path ghi catalog (SOS10).
- Phục vụ file tĩnh `uploads/` (SOS07).
- Actuator `health` (tuỳ chọn, dễ `.bat`).

**Không làm:** logic giữ ghế, không ký JWT (auth ký).

---

## 2. Module auth (bảng `users`)

**Nhiệm vụ**

- Đăng ký khách (online).
- Đăng nhập → JWT.
- Claim: `sub` (username), `userId` (Long), `role`.
- Seed: ADMIN, CASHIER, CUSTOMER.
- `GET /api/auth/me`.

**Sở hữu:** `users` (id, username, password BCrypt, role, enabled).

**API**

- `POST /api/auth/register` — chỉ CUSTOMER (public).
- `POST /api/auth/login` — public.
- `GET /api/auth/me` — Bearer.

**Ranh giới**

- Không trả ghế / vé.
- Cashier/admin **không** tự đăng ký public; seed sẵn.
- Không làm CRUD user kiểu SOS06 `/api2025/users` trừ khi thừa thời gian (`POST /api/auth/users` ADMIN-only).

---

## 3. Module catalog (phim, thể loại, phòng, suất)

**Nhiệm vụ — danh mục chiếu, không bán vé**

- Thể loại (Genre) 1-N Phim — đúng pattern Category–Product SOS05.
- Phim (tên, thời lượng, rated, mô tả, poster).
- Phòng + layout ghế tĩnh (STANDARD/VIP).
- Suất (phim + phòng + giờ + giá). `end_at` = `start_at` + duration; **cấm overlap** cùng phòng.
- CRUD ADMIN (+ API Key).
- Đọc: public (phim đang chiếu, suất theo ngày); CUSTOMER/CASHIER/ADMIN đều đọc được.
- Tìm kiếm tên, phân trang, sắp xếp (SOS07).
- Upload poster multipart (SOS07).

**Sở hữu:** `genres`, `movies`, `rooms`, `seats`, `showtimes`. **Không** cột “ghế đã bán”.

**Ranh giới dễ sai**

- Không bảng `reservations` trong catalog.
- Không đếm ghế trống bằng SQL catalog — số trống = layout trừ HOLD/BOOKED ở booking.

**Bị gọi:** `BookingService` inject `ShowtimeService` / `Seat` query qua repository hoặc method public trên service catalog (cùng JVM).

**Phòng:** seed-only trong BTL (GET rooms + seats). Không làm màn CRUD phòng trừ khi thừa thời gian.

---

## 4. Module booking (chỗ ngồi và vé)

**Nhiệm vụ — nguồn sự thật cho ghế và vé**

- Hold ghế (timeout **8 phút**).
- Xác nhận (thanh toán giả lập).
- Huỷ hold / scheduler hết hạn.
- Phát hành vé (mã, snapshot).
- POS và online **cùng service**, khác `channel`: `ONLINE` | `POS`.

**Sở hữu:** `reservations`, `reservation_seats`, `tickets`, `payments`.

Unique `(showtime_id, seat_id)` trên ghế đang HOLD hoặc thuộc reservation CONFIRMED.

**API**

- `GET /api/bookings/showtimes/{showtimeId}/seat-map`
- `POST /api/bookings/hold`
- `POST /api/bookings/{id}/confirm`
- `DELETE /api/bookings/{id}` — chỉ HOLD
- `GET /api/bookings/my` — `userId` từ JWT, **không** tin query `userId=`
- `GET /api/bookings/{id}`
- `POST /api/pos/bookings/{id}/confirm-cash` — CASHIER
- `GET /api/tickets/{ticketCode}` — CASHIER/ADMIN
- `GET /api/bookings/occupancy?showtimeId=` — ADMIN

**Không làm:** CRUD phim; không lưu mật khẩu. POS vãng lai: `customerUserId = null`, `guestName` tuỳ chọn, `cashierUserId` bắt buộc.

---

## 5. Client React (cổng 5173) — không phải module backend

- `/` `/login` `/register` `/showtimes/:movieId` `/book/:showtimeId` `/checkout/:reservationId` `/tickets` — CUSTOMER
- `/pos/login` `/pos` `/pos/book/:showtimeId` `/pos/checkout/:reservationId` `/pos/tickets` (soát vé) — CASHIER
- `/admin/genres` `/admin/movies` `/admin/showtimes` `/admin/settings` — ADMIN

Chi tiết: [07-frontend-hai-kenh.md](07-frontend-hai-kenh.md).

---

## 6. Bảng ranh giới nhanh

| Nhu cầu | Module đúng | Module sai |
|---|---|---|
| Đăng nhập / JWT | auth | catalog |
| Thêm thể loại, phim, suất | catalog | booking |
| Ghế A5 đang HOLD | booking | catalog |
| In vé | booking (dữ liệu) + client in | catalog |
| Login POS | auth (role CASHIER) | booking |
| Upload poster | catalog + `uploads/` | booking |

## 7. Giao tiếp nội bộ

```
BookingService
  → ShowtimeService.getById(id)     // 404 / suất huỷ / đã bắt đầu → không hold
  → Room/Seat layout theo roomId
```

Nếu catalog method ném lỗi không mong đợi: hold trả 500; với suất không tồn tại: 404. Không còn kịch bản “tắt catalog-service → 503” của bản microservice. Demo tắt **cả** `vexemphim-api` thì client toast không kết nối (SOS08). Có thể demo 409 và unique thay cho 503 tắt service.

Muốn giữ demo “phụ thuộc lỗi”: giả lập `ShowtimeService` throw khi profile `demo` — không bắt buộc.
