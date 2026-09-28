# 03. Mô hình dữ liệu

**Một** schema MySQL `vexemphim`. Foreign key JPA **được phép** trong cùng DB (SOS04–SOS05). Không còn ba database tách.

`spring.jpa.hibernate.ddl-auto=update` lúc dev (SOS04). Khi ổn định có thể `validate`.

Múi giờ: `Asia/Ho_Chi_Minh` (`serverTimezone=Asia/Ho_Chi_Minh` trên JDBC URL). Không copy mật khẩu máy thầy vào repo.

---

## 1. users (module auth)

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK AI | `userId` trong JWT |
| username | VARCHAR(64) UNIQUE NOT NULL | |
| password | VARCHAR(255) NOT NULL | BCrypt — không plaintext SOS06 |
| role | VARCHAR(32) NOT NULL | `ADMIN` \| `CASHIER` \| `CUSTOMER` |
| enabled | BOOLEAN DEFAULT TRUE | |
| created_at | DATETIME | |

Không bảng role riêng (BTL). Seed 3 user demo.

---

## 2. genres (thể loại — tương đương categories SOS05)

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(100) NOT NULL UNIQUE | Hành động, Tình cảm, Hoạt hình, … |

`Genre` `@OneToMany(mappedBy = "genre")` → `Movie`.  
`Movie` `@ManyToOne` `@JoinColumn(name = "genre_id")`.  
JSON: `@JsonManagedReference` / `@JsonBackReference` như SOS05 (tránh vòng lặp).

---

## 3. movies

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| title | VARCHAR(255) NOT NULL | |
| duration_minutes | INT NOT NULL | Dùng để tính `showtimes.end_at` |
| rated | VARCHAR(16) | P, C13, C16, C18 |
| description | TEXT | |
| poster_url | VARCHAR(512) | `uploads/uuid_ten.jpg` hoặc null |
| status | VARCHAR(16) | `SHOWING` \| `COMING` \| `HIDDEN` |
| genre_id | BIGINT | FK genres, nullable nếu chưa gán |

`status`: `SHOWING` bán vé bình thường; `COMING` hiện ở tab Sắp chiếu nhưng hold → 400 `MOVIE_NOT_ON_SALE` (seed BTL không tạo suất cho phim COMING); `HIDDEN` là xoá mềm, chỉ ADMIN thấy.

Repository: `findByTitleContainingIgnoreCase(String, Pageable)` (SOS07).

---

## 4. rooms và seats

### rooms

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(64) | “Phòng 1” |
| row_count | INT | ví dụ 8 |
| seats_per_row | INT | ví dụ 12 |

BTL: ghế đều. Seed, không CRUD phòng trên UI.

### seats (định danh ghế vật lý, không phải trạng thái bán)

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | **seatId** dùng xuyên booking |
| room_id | BIGINT NOT NULL | FK rooms |
| row_label | VARCHAR(8) | A, B, C |
| number | INT | 1, 2, 3 |
| type | VARCHAR(16) | `STANDARD` \| `VIP` |

Unique `(room_id, row_label, number)`.  
Room `@OneToMany` seats.

---

## 5. showtimes

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | **showtimeId** |
| movie_id | BIGINT NOT NULL | FK movies |
| room_id | BIGINT NOT NULL | FK rooms |
| start_at | DATETIME NOT NULL | |
| end_at | DATETIME NOT NULL | `start_at` + duration phim (+ buffer 15 phút, tuỳ) |
| base_price | DECIMAL(12,2) NOT NULL | giá STANDARD; VIP = × 1.2 (hằng số service) |
| status | VARCHAR(16) | `SCHEDULED` \| `CANCELLED` |

Không chỉ unique `(room_id, start_at)`. **Service phải từ chối** suất mới nếu khoảng `[start_at, end_at)` giao với suất `SCHEDULED` cùng phòng.

Movie `@OneToMany` showtimes (1-N, SOS05).

---

## 6. reservations

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| showtime_id | BIGINT NOT NULL | FK showtimes |
| movie_id | BIGINT | snapshot id |
| movie_title | VARCHAR(255) | snapshot lúc confirm |
| start_at | DATETIME | snapshot |
| room_name | VARCHAR(64) | snapshot |
| channel | VARCHAR(16) NOT NULL | `ONLINE` \| `POS` |
| customer_user_id | BIGINT NULL | JWT userId; POS vãng lai = null |
| cashier_user_id | BIGINT NULL | bắt buộc nếu POS |
| guest_name | VARCHAR(128) NULL | tên khách quầy (tuỳ chọn, in vé) |
| status | VARCHAR(16) NOT NULL | `HOLD` \| `CONFIRMED` \| `EXPIRED` \| `CANCELLED` |
| hold_expires_at | DATETIME NULL | chỉ khi HOLD |
| total_amount | DECIMAL(12,2) | |
| created_at | DATETIME | |

FK `showtime_id` được phép vì cùng DB. Snapshot vẫn giữ để in khi admin sửa tên phim.

### reservation_seats

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| reservation_id | BIGINT NOT NULL | FK reservations |
| showtime_id | BIGINT NOT NULL | denormalize unique ghế |
| seat_id | BIGINT NOT NULL | FK seats |
| seat_label | VARCHAR(16) | “A5” snapshot |
| price | DECIMAL(12,2) | |

**Chống bán trùng**

```
UNIQUE (showtime_id, seat_id)
```

Chỉ tồn tại hàng khi ghế HOLD hoặc reservation CONFIRMED. EXPIRED/CANCELLED: **xoá** `reservation_seats` rồi cập nhật reservation.

Luồng nhả ghế:

1. Scheduler 30 giây: `HOLD` và `hold_expires_at < now` → xoá seats → `EXPIRED`.
2. Huỷ hold: tương tự, `CANCELLED`.

### tickets

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| reservation_id | BIGINT NOT NULL | |
| ticket_code | VARCHAR(32) UNIQUE | `VX-2026-000123` |
| qr_payload | VARCHAR(128) | cùng ticket_code |
| issued_at | DATETIME | |

BTL: **một vé / một ghế**.

### payments

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| reservation_id | BIGINT NOT NULL | |
| method | VARCHAR(16) | `CASH` \| `ONLINE_MOCK` |
| status | VARCHAR(16) | `SUCCESS` |
| amount | DECIMAL(12,2) | |
| paid_at | DATETIME | |

Không lưu số thẻ.

### concession_products

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(128) UNIQUE | |
| price | DECIMAL(12,2) | |
| type | VARCHAR(16) | `FOOD` \| `DRINK` \| `COMBO` |
| status | VARCHAR(16) | `ACTIVE` \| `HIDDEN` |

### reservation_concessions

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK | |
| reservation_id | BIGINT NOT NULL | |
| product_id | BIGINT | id món lúc thêm |
| name | VARCHAR(128) | snapshot |
| unit_price | DECIMAL(12,2) | snapshot |
| qty | INT | 1–8 |

Hủy/hết hạn hold: xóa dòng món (orphan). Confirm: giữ snapshot. `payments.amount` = ghế + món.

---

## 7. Trạng thái ghế (logic)

| Hiển thị | Điều kiện |
|---|---|
| AVAILABLE | không có reservation_seats (showtime, seat) |
| HOLD | reservation HOLD chưa hết hạn, không phải phiên hiện tại |
| HOLD_MINE | HOLD thuộc user/cashier hiện tại |
| BOOKED | reservation CONFIRMED |

Client **chỉ** tin `GET seat-map`.

## 8. Máy trạng thái reservation

```
HOLD --(confirm + payment SUCCESS)--> CONFIRMED
HOLD --(timeout scheduler)----------> EXPIRED   (+ xóa reservation_seats)
HOLD --(user/cashier hủy)-----------> CANCELLED (+ xóa reservation_seats)
CONFIRMED: BTL không hoàn vé
```

## 9. Seed tối thiểu

- 4 thể loại, 3 phim (mỗi phim một genre), 2 phòng, ghế ~8×10.
- 6–8 suất / 2 ngày, không chồng giờ cùng phòng.
- User demo như README.
