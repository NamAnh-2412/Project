# 12. Khóa ghế khi đặt đồng thời

Ghi chú kỹ thuật: **ai được ghế khi hai request hold cùng `showtimeId` + `seatId`.**

Không Redis, không WebSocket, không lock phân tán. Một process Spring Boot + một MySQL — đủ để chốt quyền sở hữu **xác định (deterministic)** trong BTL.

---

## 1. Vấn đề

UI chỉ là dự đoán. Hai thu ngân / một POS + một khách có thể cùng thấy ghế A5 `AVAILABLE` và cùng bấm.

Nếu tin UI: bán trùng.  
Nếu chỉ `SELECT` rồi `INSERT` không khóa: race — cả hai thấy trống, cả hai insert.

Cần một **trọng tài duy nhất** quyết định ai thắng, không phụ thuộc thứ tự click trên trình duyệt.

---

## 2. Các hướng (và vì sao BTL chọn DB)

| Hướng | Ý tưởng | Khi nào | BTL |
|---|---|---|---|
| **Optimistic** | Đọc version, ghi nếu version còn khớp; conflict → retry/409 | Ít đụng độ | Không đủ: hai hold mới cùng “trống” vẫn insert được nếu không có unique |
| **Pessimistic (row lock)** | `SELECT … FOR UPDATE` trên hàng suất / ghế | Một DB, một app | **Có** — khóa hàng `showtimes` trong transaction hold |
| **Constraint (source of truth)** | `UNIQUE (showtime_id, seat_id)` | Mọi scale một DB | **Bắt buộc** — request commit trước thắng, sau = lỗi unique → 409 |
| **Hold tạm** | Hàng `reservation_seats` + `HOLD` + `hold_expires_at` | Bán vé rạp | **Có** — 8 phút, scheduler 30s |
| **Redis lock** | `SET key NX EX 480` rồi ghi DB | Nhiều instance API | Không — một JVM, Redis thêm điểm hỏng |
| **WebSocket** | Đẩy seat-map realtime | UX | Không — poll 5s đủ demo; lock vẫn phải ở DB |

Redis/WS **không thay** unique DB. Chúng chỉ tăng tốc hiển thị. Quyền sở hữu vẫn là hàng `reservation_seats`.

**Chốt BTL:** pessimistic lock trên suất (tránh hai transaction kiểm tra “còn trống” song song) **và** unique ghế (trọng tài cuối nếu lock miss / race). Ai **commit INSERT thành công trước** là chủ ghế.

---

## 3. Mô hình quyền sở hữu

```
Ghế vật lý (seats.id)     ≠    chỗ ngồi theo suất
Phòng có A5 một lần. Suất 14 và suất 15 đều có A5 — unique theo (showtime_id, seat_id).
```

Trạng thái **không** lưu trên bảng `seats`. Suy ra từ `reservation_seats` + `reservations.status`:

| Hàng reservation_seats | Reservation | Seat-map |
|---|---|---|
| không có | — | `AVAILABLE` |
| có | `HOLD` + đúng user đang login | `HOLD_MINE` |
| có | `HOLD` + user khác / kênh khác | `HOLD` |
| có | `CONFIRMED` | `BOOKED` |
| đã xóa | `EXPIRED` / `CANCELLED` | `AVAILABLE` lại |

Nhả ghế = **xóa hàng** `reservation_seats` (unique được giải), không giữ hàng “hết hạn”.

Timeout: `app.hold-minutes=8`. `HoldExpiryScheduler` 30s: `HOLD` và `hold_expires_at < now` → xóa seats → `EXPIRED`.

---

## 4. Luồng lock (một lần bấm ghế)

```
Client  A, B cùng A5
        |                    |
        | POST /api/bookings/hold { showtimeId, seatIds:[A5], channel }
        |                    |
        v                    v
   BEGIN TX             BEGIN TX
   lock showtimes       đợi hàng showtimes (FOR UPDATE)
   (PESSIMISTIC_WRITE)
        |                    |
   exists A5? không          |
   INSERT reservation_seats  |
   COMMIT  → 201             |
                             |
                        lock được suất
                        exists A5? có
                        ROLLBACK / 409 SEAT_TAKEN
```

Nếu hai TX lách lock (lỗi cấu hình): `UNIQUE` + `DataIntegrityViolationException` → cùng 409. **Không có trạng thái “cả hai thắng”.**

Cùng một user thêm/bớt ghế: **sửa hold hiện có** (`replaceHoldSeats`), không hủy rồi tạo lại — tránh khoảng trống unique giữa DELETE và INSERT.

---

## 5. Ánh xạ code

| Việc | Chỗ |
|---|---|
| Unique ghế | `ReservationSeat` `@UniqueConstraint(showtime_id, seat_id)` |
| Hold + lock suất + 409 | `BookingService.hold` — `em.lock(st, PESSIMISTIC_WRITE)` |
| Tạo / cập nhật hold | `createHold` / `replaceHoldSeats` / `attachSeat` |
| Kiểm tra đã giữ | `ReservationSeatRepository.existsByShowtimeIdAndSeatId` |
| Unique miss | `catch DataIntegrityViolationException` → `SEAT_TAKEN` |
| Seat-map | `BookingService.seatMap` |
| Hết hạn | `HoldExpiryScheduler` → `expireHolds` |
| Poll + hold ngay khi chọn | `web-client/src/pages/SeatMapPage.tsx` |
| Giữ selection khi poll | `mergeSelected` — không ghi đè bằng `HOLD_MINE` rỗng |
| Hàng đợi hold | `chainRef` — không song song hai POST hold của cùng tab |
| Màu / disable | `SeatGrid.tsx` |

Hợp đồng lỗi: `409` + `code: SEAT_TAKEN` (xem [docs/04](04-hop-dong-api.md), [docs/05](05-luong-nghiep-vu.md) mục 3).

---

## 6. Việc đã làm (implementation)

### Backend

1. Bảng `reservation_seats` unique `(showtime_id, seat_id)` — Hibernate/`ddl-auto=update`.
2. `POST /api/bookings/hold` trong `@Transactional`:
   - Load suất, `EntityManager.lock(..., PESSIMISTIC_WRITE)` (= `SELECT … FOR UPDATE` trên hàng `showtimes`).
   - Tìm hold `HOLD` còn hạn của đúng user + kênh; hết hạn thì `EXPIRED`.
   - Có hold: xóa ghế bỏ chọn, flush, gắn ghế mới; ghế đã có hàng (của người khác) → 409.
   - Chưa hold: tạo `reservations` + `reservation_seats`.
   - `saveAndFlush`; unique conflict → 409.
3. Confirm không insert ghế mới — chỉ đổi `HOLD` → `CONFIRMED`. Ghế đã bị unique giữ từ lúc hold.
4. Hủy / hết hạn: `seats.clear()` (orphanRemoval) rồi đổi status.

### Frontend (POS và online, cùng `SeatMapPage`)

1. Bấm ghế → `POST hold` ngay (không đợi Tiếp tục).
2. Poll 5s: cập nhật HOLD/BOOKED của người khác; **giữ** ghế local còn `AVAILABLE` / `HOLD_MINE`.
3. 409: toast “Ghế vừa bị người khác giữ trước”, reload map.
4. Tiếp tục: hold lần nữa (cùng reservation, gia hạn 8 phút) rồi sang bắp nước.
5. Bỏ hết ghế: `DELETE /api/bookings/{id}`.

---

## 7. Demo xác định chủ ghế

Hai trình duyệt, cùng suất, ghế A1:

1. Cửa sổ 1 (POS) bấm A1 → 201, vàng, đếm ngược giữ ghế.
2. Cửa sổ 2 poll ≤ 5s → A1 xám `HOLD`, không bấm được.
3. Nếu cửa sổ 2 hold A1 trước khi poll: **409 SEAT_TAKEN**.
4. Cửa sổ 1 confirm → A1 `BOOKED`.
5. Cửa sổ 1 hủy / hết 8 phút → A1 `AVAILABLE` cho cửa sổ 2.

Cùng tài khoản hai tab: một hold — tab sau **cập nhật cùng reservation** (không phải hai chủ). Demo “hai người” cần **hai user** (POS `NhanVien` + khách `customer1`).

---

## 8. Hướng mở (không làm trong BTL)

- Nhiều instance API: unique DB vẫn đủ đúng; Redis `SET NX EX` chỉ giảm contention / cache seat-map.
- WebSocket: đẩy đổi HOLD/BOOKED, **không** thay unique.
- Khóa từng hàng ghế (`SELECT FOR UPDATE` trên `seats` hoặc bảng `showtime_seats`): mịn hơn khóa cả suất; BTL khóa suất vì phòng nhỏ, hold ngắn.
- Outbox / hàng đợi: không cần khi một MySQL.

---

## 9. Câu trả lời ngắn nếu thầy hỏi

> Em không khóa trên UI. Khi chọn ghế, client gọi hold. Transaction khóa hàng suất, insert `reservation_seats`. Unique `(suất, ghế)` là trọng tài: commit trước thắng, request sau 409. Hold 8 phút rồi scheduler xóa hàng — ghế trống lại. Không Redis vì một process một DB; WebSocket chỉ là UX.
