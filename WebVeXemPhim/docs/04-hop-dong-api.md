# 04. Hợp đồng API

Mọi URL là phía client, gốc `http://localhost:8080`. Không còn Gateway rewrite.

Quy ước:

- JSON UTF-8.
- `Authorization: Bearer <token>` khi không ghi Public.
- Ghi catalog (POST/PUT/DELETE/upload genre-movie-showtime): thêm `X-API-KEY` (SOS10). Key local: `app.api.key` trong `application.properties` — không commit key production.
- Lỗi: `{ "message": "...", "code": "SEAT_TAKEN" }`.
- HTTP: 400 validation, 401 thiếu/sai JWT hoặc API Key, 403 sai role, 404, 409 xung đột ghế, 204 xoá thành công (SOS03).

---

## 1. Auth

### POST `/api/auth/register` — Public

```json
{ "username": "customer2", "password": "customer123" }
```

201: `{ "id": 4, "username": "customer2", "role": "CUSTOMER" }`. Không chọn role. 409 username trùng.

### POST `/api/auth/login` — Public

```json
{ "username": "customer1", "password": "customer123" }
```

200:

```json
{
  "token": "<jwt>",
  "userId": 3,
  "username": "customer1",
  "role": "CUSTOMER"
}
```

Sai mật khẩu: 401. Không trả password (khác SOS06 login trả entity).

### GET `/api/auth/me` — Bearer

`userId`, `username`, `role`.

---

## 2. Catalog — thể loại (SOS05)

Public GET. Ghi: ADMIN + API Key.

- `GET /api/genres` — mảng `{ id, name }` (không nhét full movies để nhẹ).
- `GET /api/genres/{id}`
- `GET /api/genres/{id}/movies` — phim thuộc thể loại (1-N).
- `POST /api/genres` body `{ "name": "Hành động" }` → 201
- `PUT /api/genres/{id}`
- `DELETE /api/genres/{id}` — 409 nếu còn phim `SHOWING` (an toàn hơn cascade xoá phim).

---

## 3. Catalog — phim (đọc + SOS07 page)

### GET `/api/movies` — Public

Query:

| Tham số | Ý nghĩa |
|---|---|
| `name` | title chứa chuỗi, ignore case |
| `genreId` | lọc thể loại |
| `status` | mặc định `SHOWING`; `COMING` = sắp chiếu; ADMIN có thể `HIDDEN` hoặc `ALL` |
| `page` | mặc định 0 |
| `size` | mặc định 8 |
| `sort` | ví dụ `title,asc` / `durationMinutes,desc` |

Response Spring `Page` (SOS07/08): `content`, `totalPages`, `totalElements`, `number`, `size`.

Phần tử: `{ id, title, durationMinutes, rated, posterUrl, genreId, genreName, status }`.

### GET `/api/movies/{id}` — Public nếu SHOWING; ADMIN thấy HIDDEN

Chi tiết + mô tả. 404 nếu không có / khách gặp HIDDEN.

### POST `/api/movies` — ADMIN + API Key

Body: title, durationMinutes, rated, description, genre `{ "id": 1 }`, status. 201. 400 nếu genre không tồn tại (SOS05).

### PUT `/api/movies/{id}` — ADMIN + API Key

### DELETE `/api/movies/{id}` — ADMIN + API Key

Mềm: `HIDDEN`. 204.

### POST `/api/movies/{id}/upload-poster` — ADMIN + API Key

`multipart/form-data` field `file`. 200 `{ "posterUrl": "uploads/uuid_ten.jpg" }`. CUSTOMER: 403. File rỗng: 400.

Static: `http://localhost:8080/uploads/...` (SOS07).

---

## 4. Catalog — phòng

- `GET /api/rooms` — public.
- `GET /api/rooms/{id}/seats` — layout thuần.
- `POST /api/rooms` — ADMIN + API Key (mở rộng): `{ name, rowCount, seatsPerRow, vipRowCount }`, server sinh ghế. Trùng tên → 409.
- `PUT /api/rooms/{id}` — ADMIN + API Key: `{ name?, status? }` — đổi tên hoặc `ACTIVE` / `INACTIVE`. Không sửa lưới, không xóa. Phòng `INACTIVE` không tạo/sửa suất; khách không thấy suất của phòng đó; hold → 400 `ROOM_INACTIVE`.

---

## 4b. Catalog — bắp nước

- `GET /api/concessions` — public: chỉ `ACTIVE`. ADMIN JWT: cả `HIDDEN`.
- `POST /api/concessions` — ADMIN + API Key: `{ name, price, type: FOOD|DRINK|COMBO, status? }`.
- `PUT /api/concessions/{id}` — ADMIN + API Key. Ẩn = `HIDDEN`. Không xóa cứng.

---

## 4c. Catalog — cài đặt rạp

- `GET /api/settings` — public: `{ name, logoUrl, imageUrl }`. Chưa có hàng thì tạo mặc định `CineVe`.
- `PUT /api/settings` — ADMIN + API Key: `{ name }`.
- `POST /api/settings/logo` và `POST /api/settings/image` — ADMIN + API Key, `multipart file` (JPG/PNG/WEBP/GIF, 5MB), trả settings mới.

Nền sáng/tối không lưu server.

---

## 5. Catalog — suất

### GET `/api/showtimes?movieId=&date=2026-09-20` — Public

`date` ISO ngày local. Có thể page nếu danh sách dài; BTL list ngắn, Page tuỳ chọn.

```json
[
  {
    "id": 10,
    "movieId": 1,
    "movieTitle": "Phim A",
    "roomId": 1,
    "roomName": "Phòng 1",
    "startAt": "2026-09-20T20:00:00",
    "endAt": "2026-09-20T22:00:00",
    "basePrice": 75000
  }
]
```

### GET `/api/showtimes/{id}`

### POST `/api/showtimes` — ADMIN + API Key

movieId, roomId, startAt, basePrice. Server tính `endAt`. 409 `SHOWTIME_OVERLAP` nếu chồng phòng. 400 `MOVIE_NOT_ON_SALE` nếu phim đang `COMING` / `HIDDEN`. 400 `SHOWTIME_PAST` nếu `startAt` không ở tương lai. 400 `ROOM_INACTIVE` nếu phòng đang dừng.

Public list ẩn suất đã qua và suất thuộc phòng `INACTIVE`. ADMIN + `includePast=true` thấy cả suất đã bắt đầu.

### PUT `/api/showtimes/{id}` — ADMIN + API Key

Cùng rule phim/phòng/`startAt` như POST. 409 `HAS_TICKETS` nếu đã bán; 409 `HAS_HOLDS` nếu đang giữ ghế.

### DELETE `/api/showtimes/{id}` — ADMIN + API Key

Huỷ (`CANCELLED`) nếu occupancy booked = 0 và hold = 0; 409 `HAS_TICKETS` / `HAS_HOLDS`.

---

## 6. Booking — sơ đồ ghế

### GET `/api/bookings/showtimes/{showtimeId}/seat-map` — Bearer (CUSTOMER/CASHIER/ADMIN)

**Không** đặt path dưới `/api/showtimes/...` (tránh lẫn catalog).

```json
{
  "showtimeId": 10,
  "movieTitle": "Phim A",
  "startAt": "2026-09-20T20:00:00",
  "roomName": "Phòng 1",
  "holdMinutes": 8,
  "seats": [
    {
      "seatId": 101,
      "label": "A5",
      "row": "A",
      "number": 5,
      "type": "STANDARD",
      "status": "AVAILABLE",
      "price": 75000
    }
  ]
}
```

`status`: `AVAILABLE` | `HOLD` | `HOLD_MINE` | `BOOKED`.

---

## 7. Hold

### POST `/api/bookings/hold` — CUSTOMER (ONLINE) hoặc CASHIER (POS)

```json
{
  "showtimeId": 10,
  "seatIds": [101, 102],
  "channel": "ONLINE",
  "guestName": null
}
```

POS: `"channel": "POS"`, `guestName` tuỳ chọn. CUSTOMER gửi POS → 403. CASHIER gửi ONLINE → 403. ADMIN không hold.

201: `reservationId`, `status`, `holdExpiresAt`, `totalAmount`, `seats`.

Lỗi:

- 404 suất không tồn tại / CANCELLED
- 409 `SEAT_TAKEN`
- 400 suất đã bắt đầu (`start_at <= now`) hoặc quá 8 ghế
- 400 `MOVIE_NOT_ON_SALE` phim đang `COMING` / `HIDDEN`
- 400 `ROOM_INACTIVE` phòng đang dừng
- 409 `SHOWTIME_OVERLAP` không dùng ở hold

Một user một HOLD active / suất: huỷ hold cũ rồi tạo mới trong transaction.

### PUT `/api/bookings/{id}/concessions` — chủ HOLD

`{ "items": [{ "productId": 1, "qty": 2 }] }`. Qty 0 bỏ món. Tối đa 10 món, qty 1–8. Tính lại `totalAmount` = ghế + bắp nước. 409 `HOLD_EXPIRED`.

### GET `/api/reports/revenue?from=&to=&grain=DAY|WEEK|MONTH` — ADMIN

Thẻ hôm nay / tuần / tháng, series chart, top 5 phim theo số vé, top bắp nước. Tách `ticket` / `concession` theo dòng, không lấy nguyên `payments.amount` làm tiền vé.

---

## 8. Confirm và thanh toán giả lập

### POST `/api/bookings/{id}/confirm` — CUSTOMER

```json
{ "paymentMethod": "ONLINE_MOCK" }
```

HOLD còn hạn, `customer_user_id` = JWT. Payment SUCCESS, CONFIRMED, tạo tickets.

200: reservation + `tickets[]` `{ ticketCode, seatLabel, qrPayload }`.

409 hết hạn. 403 không phải chủ.

Confirm lần 2 cùng id: 200 đã CONFIRMED, không tạo vé trùng.

### POST `/api/pos/bookings/{id}/confirm-cash` — CASHIER

Body tuỳ chọn `{ "amountReceived": 200000 }`. Payment `CASH`.

---

## 9. Huỷ hold

### DELETE `/api/bookings/{id}` — chủ hold hoặc CASHIER trên hold POS

Chỉ HOLD. Xoá reservation_seats, `CANCELLED`. **204**.

---

## 10. Vé / occupancy

### GET `/api/bookings/my` — CUSTOMER

`userId` từ JWT. Cấm `?userId=`.

### GET `/api/bookings/{id}` — chủ | CASHIER | ADMIN

### GET `/api/tickets/{ticketCode}` — CASHIER | ADMIN (soát vé)

### GET `/api/bookings/occupancy?showtimeId=` — ADMIN

`{ "hold": 2, "booked": 15 }`.

---

## 11. Public vs bảo vệ

Public (không JWT, không API Key):

- `/api/auth/login`, `/api/auth/register`
- `GET /api/movies`, `GET /api/movies/{id}`, `GET /api/genres`, `GET /api/genres/{id}`, `GET /api/genres/{id}/movies`
- `GET /api/showtimes`, `GET /api/showtimes/{id}`
- `GET /api/rooms`, `GET /api/rooms/{id}/seats`
- `GET /api/concessions` (chỉ ACTIVE nếu không phải ADMIN)
- `GET /api/settings`
- static `/uploads/**`

Seat-map và hold **bắt login**.

API Key: mọi POST/PUT/DELETE/upload của `/api/genres/**`, `/api/movies/**`, `/api/showtimes/**`, `/api/rooms/**`, `/api/concessions/**`, `/api/settings/**`. Thiếu/sai key: **401** (đúng SOS10). Sai role dù đúng key: **403**.

---

## 12. Mã lỗi `code` thống nhất

| code | HTTP | Khi nào |
|---|---|---|
| `SEAT_TAKEN` | 409 | Ghế HOLD/BOOKED |
| `HOLD_EXPIRED` | 409 | Confirm quá hạn |
| `SHOWTIME_OVERLAP` | 409 | Tạo suất chồng phòng |
| `MOVIE_NOT_ON_SALE` | 400 | Hold suất của phim `COMING` / `HIDDEN` |
| `API_KEY_INVALID` | 401 | Thiếu/sai X-API-KEY |
| `GENRE_IN_USE` | 409 | Xoá thể loại còn phim |
