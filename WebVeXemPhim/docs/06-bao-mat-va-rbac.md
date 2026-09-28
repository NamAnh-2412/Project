# 06. Bảo mật và RBAC

SOS06 lab dùng `?role=ADMIN` và mật khẩu plaintext, ghi *tạm thời, chưa Spring Security*. BTL **không** làm vậy trên sản phẩm nộp.

BTL: Spring Security + JWT (cùng ý CRS: claim `userId` trên `Authentication` credentials, Number → Long). Thêm API Key SOS10 trên API ghi catalog.

## 1. Vai trò

| Role | Được | Cấm |
|---|---|---|
| CUSTOMER | Đăng ký, login, xem lịch, hold ONLINE, confirm mock, `/bookings/my` | POS, CRUD catalog, xem vé người khác |
| CASHIER | Login, seat-map, hold POS, confirm-cash, soát mã, in | Register ra CASHIER, xoá phim, hold ONLINE |
| ADMIN | CRUD genre/movie/showtime, occupancy, xem booking | Không bán POS |

Spring: `ROLE_CUSTOMER` = `"ROLE_" + role`.

## 2. JWT

- `sub`: username
- `userId`: Long
- `role`: ADMIN \| CASHIER \| CUSTOMER
- `exp`: 24h (`jwt.expiration-ms=86400000`)

Một `jwt.secret` trên monolith. Không nhét danh sách ghế vào JWT.

## 3. API Key (SOS10)

`app.api.key` trong properties (dev có thể `SECRET_KEY_BTL` — chỉ local).

Header `X-API-KEY`. Interceptor `HandlerInterceptor` như lab, path:

- `/api/genres/**` — chỉ POST, PUT, DELETE (GET public)
- `/api/movies/**` — POST, PUT, DELETE, upload
- `/api/showtimes/**` — POST, PUT, DELETE
- `/api/rooms/**` — POST, PUT
- `/api/concessions/**` — POST, PUT
- `/api/settings/**` — PUT, POST upload

Thiếu/sai → 401 body `API Key invalid or missing` / code `API_KEY_INVALID`.

React admin gắn key từ `VITE_API_KEY` (file `.env` local, không public).

JWT vẫn bắt buộc trên các path này: API Key ≠ đăng nhập user. Đúng SOS10 “lớp bổ sung”.

## 4. Endpoint × role

| Endpoint | Public | CUSTOMER | CASHIER | ADMIN |
|---|---|---|---|---|
| POST /api/auth/login, register | x | | | |
| GET movies, genres, showtimes, rooms | x | x | x | x |
| GET seat-map | | x | x | x |
| POST hold ONLINE | | x | | |
| POST hold POS | | | x | |
| POST confirm mock | | x | | |
| POST confirm-cash | | | x | |
| GET /bookings/my | | x | | |
| Catalog POST/PUT/DELETE + upload | | | | x (+ API Key) |

`SecurityFilterChain`: permitAll login/register + GET catalog public; còn lại authenticated + `@PreAuthorize`.

Không dùng `@RequestParam String role` như SOS06.

## 5. IDOR

- `GET /api/bookings/my`: `customer_user_id = jwt.userId`. Không `?userId=`.
- `GET /api/bookings/{id}`: CUSTOMER chỉ chủ; CASHIER/ADMIN được.
- Confirm: đúng chủ / cashier theo channel.
- Không lộ token trên URL.

## 6. POS trên máy quầy

- Không lưu mật khẩu ngoài phiên.
- Route `/pos` + ProtectedRoute CASHIER.
- CORS chỉ origin Vite lúc dev.

## 7. Mật khẩu và seed

BCrypt. Tài khoản demo chỉ local/báo cáo.

Không commit mật khẩu production / API key thật. Repo BTL private.

Không đưa mật khẩu mẫu trong PDF SOS04 vào git.

## 8. Hold không thay JWT

Unique + 409 là toàn vẹn dữ liệu. Vẫn JWT để không hold hàng loạt vô danh. Giới hạn 8 ghế / hold, 1 hold active / user / suất.
