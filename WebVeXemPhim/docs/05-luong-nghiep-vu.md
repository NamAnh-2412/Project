# 05. Luồng nghiệp vụ

Hai kênh dùng chung module booking. Khác nhau ở **ai đăng nhập**, **cách trả tiền**, **in giấy**.

Timeout hold: **8 phút**. Scheduler 30 giây.

---

## 1. Đặt vé trực tuyến (CUSTOMER)

Bốn bước cũ thành **năm**: **Phim → Suất → Ghế → Bắp nước (tuỳ chọn) → Thanh toán**.

```
Khách mở web
  → GET /api/movies?status=SHOWING&name=&page=&sort=   (public, SOS07)   [bước 1]
  → GET /api/showtimes?movieId&date                                      [bước 2]
  → Đăng nhập nếu chưa (POST /api/auth/login) → JWT
  → GET /api/bookings/showtimes/{id}/seat-map                            [bước 3]
  → Chọn tối đa 8 ghế AVAILABLE
  → POST /api/bookings/hold { channel: ONLINE }
  → GET /api/concessions ; PUT /api/bookings/{id}/concessions (có thể []) [bước 4]
  → Đếm ngược holdExpiresAt                                              [bước 5]
  → POST /api/bookings/{id}/confirm { ONLINE_MOCK }
  → Mã vé / QR
  → GET /api/bookings/my
```

Hết giờ hold: confirm 409 `HOLD_EXPIRED`; ghế AVAILABLE lại.

Huỷ: DELETE hold. CUSTOMER **không** confirm-cash.

### 1.1 Phim đang chiếu / sắp chiếu

Trang chủ có hai tab, mỗi tab một lần gọi API:

| Tab | Gọi | Hành động trên thẻ phim |
|---|---|---|
| Đang chiếu | `GET /api/movies?status=SHOWING` | **Mua vé** → bước 2 |
| Sắp chiếu | `GET /api/movies?status=COMING` | **Thông tin** → chỉ chi tiết phim |

Phim `COMING`: BTL **không seed suất**, trang chi tiết hiện “chưa mở bán vé” và không render lịch chiếu.

Chặn ở hai chỗ trong service, không dựa vào UI — cùng mã lỗi **400 `MOVIE_NOT_ON_SALE`**:

1. Tạo suất cho phim `COMING` / `HIDDEN`.
2. Hold ghế của suất mà phim đã bị chuyển sang `COMING` / `HIDDEN` sau khi suất được tạo.

Quy trình mở bán của ADMIN: sửa phim `COMING` → `SHOWING`, rồi mới tạo suất.

---

## 2. POS tại quầy (CASHIER)

```
Thu ngân /pos/login
  → GET movies SHOWING → suất theo phim+ngày
  → GET seat-map
  → Chọn ghế; guestName tuỳ chọn
  → POST hold { channel: POS }
  → PUT concessions (tuỳ chọn)
  → Thu tiền mặt
  → POST /api/pos/bookings/{id}/confirm-cash
  → In vé (window.print)
```

Khách không cần tài khoản. Thu ngân **không** sửa phim/suất (403).

POS dùng lại đúng component sơ đồ ghế và bảng màu ở mục 3.1, chỉ khác cỡ chữ/nút lớn hơn và tổng tiền in đậm.

Soát vé: `/pos/tickets` → GET `/api/tickets/{code}`.

In lại: GET booking theo id/mã.

---

## 3. Đồng bộ ghế hai kênh (tiêu chí môn)

Cùng `showtimeId` + `seatId`:

1. Online hold A5 → unique `reservation_seats`.
2. POS seat-map A5 = HOLD (disabled).
3. POS hold A5 → **409 SEAT_TAKEN**.
4. Online hết hạn / huỷ → unique nhả → POS AVAILABLE.
5. Online confirm → A5 BOOKED (BTL không hoàn).

### 3.1 Trạng thái ghế ↔ màu trên UI (chốt)

Cùng một bảng cho cả kênh khách và POS. Chốt ở đây để UI không tự đặt màu khác (chi tiết token: [docs/07](07-frontend-hai-kenh.md)).

| Trạng thái từ seat-map | Màu | Click |
|---|---|---|
| `AVAILABLE` | trắng, viền xám | được |
| `AVAILABLE` + `type = VIP` | trắng, **viền đỏ** (`--brand-red`) | được, giá × 1.2 |
| đang chọn / `HOLD_MINE` | **vàng** (`--brand-yellow`) | bấm lại để bỏ chọn |
| `HOLD` của người khác | xám (`--sold`) | chặn |
| `BOOKED` | xám đậm | chặn |

Quy ước: **đỏ chỉ dùng cho thương hiệu và viền VIP**, không dùng cho ghế đã bán (tránh lẫn với nút CTA vàng). Ghế đã bán và ghế người khác giữ cùng gam xám vì với người đang chọn thì hệ quả giống nhau: không bấm được.

Giới hạn **8 ghế / giao dịch**: client chặn trước, backend kiểm lại (400 nếu vượt).

Demo bắt buộc: hai trình duyệt cùng suất, một 201 một 409.

```
    ONLINE                     POS
       |                        |
       | POST hold A5           |
       |------ 201 -------------|
       |                        | GET seat-map HOLD
       |                        | POST hold A5
       |                        |------ 409 ------
       | confirm                |
       | A5 BOOKED              | GET seat-map BOOKED
```

---

## 4. Admin danh mục (SOS05 + SOS09)

ADMIN `/admin`:

- CRUD thể loại, phim (kèm genre), upload poster, tạo suất (chặn overlap).
- Occupancy trước khi huỷ suất.
- Mọi ghi kèm JWT + `X-API-KEY`.
- Không bán vé thay khách.

---

## 5. In vé (POS)

GET booking CONFIRMED:

- Tên rạp “Rạp BTL”
- Phim, phòng, giờ (snapshot)
- Ghế, giá, ticket_code, guest_name nếu có
- QR: text `ticket_code`

`window.print()` + `@media print`.

---

## 6. Soát vé giả

CASHIER nhập mã → GET `/api/tickets/{code}`. BTL không đổi trạng thái “đã vào phòng”.

---

## 7. Lỗi nghiệp vụ trên UI

| Tình huống | API | UI |
|---|---|---|
| Ghế người khác vừa giữ | 409 hold | Toast, reload seat-map |
| Hết hạn hold | 409 confirm | “Hết thời gian giữ ghế” |
| API tắt / mất mạng | fetch fail | Toast không kết nối (SOS08) |
| CUSTOMER vào /pos | — | ProtectedRoute |
| Suất đã chiếu | 400 hold | Ẩn suất quá khứ |
| Phim sắp chiếu | 400 `MOVIE_NOT_ON_SALE` | Tab Sắp chiếu chỉ có nút Thông tin |
| Sai API Key lúc admin lưu | 401 | Toast cấu hình |

---

## 8. Thanh toán giả lập — nói gì trong báo cáo

- Mục tiêu môn: **tích hợp dịch vụ**, không cổng NH.
- `ONLINE_MOCK` / `CASH` là adapter; sau này thay cổng thanh toán mà không đổi auth/catalog.
- “Không giao dịch tiền thật”.

---

## 9. Điểm nhấn báo cáo (luồng)

1. Client–Server: UI không ghi DB (SOS01–08).
2. Modular monolith: đổi giá ghế không sửa auth; đổi JWT không sửa entity phim.
3. Tích hợp: hai client một API hold; admin client + API Key (SOS09–10).
4. Toàn vẹn: unique + 409; overlap suất.
5. SOS07: page/search/upload trên catalog.
