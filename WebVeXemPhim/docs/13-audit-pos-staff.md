# 13. Audit cổng Staff / POS — ba việc còn lại

**Đã triển khai** (thứ tự ưu tiên): vòng đời hold → suất/ngày + occupancy → check-in một lần → tìm/in lại + print CSS → thu hẹp `getOne` + `@PreAuthorize` DELETE. Chi tiết code ở các file POS / `BookingService`.

Gộp kết quả audit cổng thu ngân (`/pos`) thành **ba việc**: sửa logic/cạnh, dọn mùi code, và so gap với POS rạp (CGV / Lotte / AMC).

Không làm lại khóa ghế: đã chốt ở [docs/12-khoa-ghe-dong-thoi.md](12-khoa-ghe-dong-thoi.md). Không nâng lịch chiếu trang khách (`MovieDetailPage`) — xem [docs/11-phuong-an-suat-chieu.md](11-phuong-an-suat-chieu.md).

**Đã ổn, không nằm trong ba việc này:** unique `(showtime_id, seat_id)` + `PESSIMISTIC_WRITE` trên suất + hold 8 phút; CASHIER thấy suất đến `endAt`; hold-on-click + `mergeSelected`; confirm cash idempotent; `HAS_TICKETS` / `HAS_HOLDS`; snapshot giá bắp nước; tách kênh `ONLINE` / `POS`.

---

## Việc 1 — Logic bugs và edge case

Sửa hành vi sai trên luồng đang có. Không thêm sản phẩm mới.

### P1 — bán sai ngày, khóa ghế oan, cửa rạp không kiểm được

| # | Hiện tượng | Chỗ | Hệ quả |
|---|---|---|---|
| 1.1 | Không suất ngày đang chọn → UI **tự `setDate`** sang ngày có suất trong 7 ngày | `PosShowtimesPage` | Thu ngân tưởng bán hôm nay |
| 1.2 | Màn bắp nước **không countdown** hold | `ConcessionPage` | Đứng chọn combo > 8 phút → `HOLD_EXPIRED`, ghế trả |
| 1.3 | Về `/pos`, đổi suất, logout **không** `cancelHold` | `SeatMapPage`, `Navbar`, `AuthContext` | Ghế khóa đến hết hạn; mỗi click ghế **reset** thêm 8 phút |
| 1.4 | Một cashier + một suất = **một** hold (hai tab ghi đè) | `findMyOpenHold` | Đúng model hiện tại; quầy 2 cửa sổ sẽ mất ghế tab kia |
| 1.5 | Soát vé chỉ `GET` mã; `status` = reservation `CONFIRMED` | `Ticket`, `lookupTicket`, `PosScanPage` | Quét 2 lần vẫn “hợp lệ”; không `CHECKED_IN` |
| 1.6 | `finalizePayment` không kiểm lại `SCHEDULED` / `endAt` | `BookingService` | Hold phút cuối → vẫn thu tiền sau khi phim hết |
| 1.7 | Mọi `CASHIER` `GET /api/bookings/{id}` | `getOne` | Xem hold/vé khách và thu ngân khác nếu đoán id |

### P2 — thao tác dễ sai

| # | Hiện tượng |
|---|---|
| 1.8 | 401 / `Protected` đẩy về `/login` khách, không `/pos/login`; logout cashier về `/` |
| 1.9 | `guestName` chỉ gửi lúc hold; không SĐT; không ô tìm đơn / in lại |
| 1.10 | `PosPrintPage` không `.catch` (kẹt “Đang tải”); checkout hết hạn vẫn hiện “Đã thu tiền”; hủy hold không confirm |
| 1.11 | `/pos/book/:id` không cảnh báo suất đang chiếu / hủy / hết ghế; occupancy chỉ ADMIN |
| 1.12 | `load(true)` sau 409 reset về `HOLD_MINE`; poll 5s (đã chấp nhận, không WS) |
| 1.13 | POS home chỉ `SHOWING`, `size: 50`, không tìm; URL `?movie=` phim `COMING` vào được rồi hold 400 |
| 1.14 | Date picker không `min=hôm nay` → ngày quá khứ + look-ahead nhảy tiếp |
| 1.15 | Mã vé `VX-{year}-{id*10+i}`; `lookupTicket` có stream ghế chết; label ghế có `\|` làm vỡ `seatFromTicket` |
| 1.16 | `/pos/revenue` bỏ `from/to/grain`, `series`, `topConcessions`; thu ngân thấy DT cả rạp (kể cả online) |

---

## Việc 2 — Code smell

Dọn hợp đồng và vệ sinh. Không đổi nghiệp vụ bán vé.

| # | Smell | Hướng dọn |
|---|---|---|
| 2.1 | `/pos/login` prefill `NhanVien` / `NhanVien123` | Bỏ mật khẩu prefill trên máy lab |
| 2.2 | `DELETE /api/bookings/{id}` không `@PreAuthorize` | `hasAnyRole('CUSTOMER','CASHIER','ADMIN')` — rule chủ vẫn ở service |
| 2.3 | `CashConfirmRequest` nhận body rồi bỏ | Dùng cho tiền khách đưa / tiền thối (Việc 1.6 UI) hoặc xóa DTO |
| 2.4 | `confirmCash` không assert `channel == POS` | Thêm check kênh |
| 2.5 | `getOne` mọi cashier | Thu hẹp: chủ hold, hoặc tra theo mã vé (phụ thuộc 1.7 / 1.9) |
| 2.6 | `existsByShowtimeIdAndSeatId` không lọc status | Sống nhờ `expire()` xóa ghế; nếu `clear()` lỗi → ghế ma AVAILABLE |
| 2.7 | Lock cả hàng suất | Đủ BTL; ghi chú cổ chai — không Redis |
| 2.8 | Reset `holdExpiresAt` mỗi click ghế | Một lần gia hạn hoặc không gia hạn (phụ thuộc 1.3) |
| 2.9 | `BookingStepper` không bấm được | Optional; checkout đã có “Sửa đồ ăn vặt” |
| 2.10 | [docs/07](07-frontend-hai-kenh.md) lệch: `/pos` không còn “suất hôm nay”; mermaid thiếu F&B | Cập nhật docs khi sửa UI |
| 2.11 | `window.print()` in GalaxySky + navbar | `@media print` chỉ `#print-area` |
| 2.12 | Soát vé không Enter, không QR camera | UX nhỏ sau Việc 1.5 |
| 2.13 | Không `beforeunload` hủy hold | Gộp với 1.3 |
| 2.14 | `tsc -b` đỏ sẵn ở `Navbar` / `MovieAdminPage` | Không phải file POS; sửa riêng nếu chặn CI |

---

## Việc 3 — Gap so với POS rạp (CGV / Lotte / AMC)

So với quầy thật. **Không** biến BTL thành Vista. Cột “BTL” = có nên làm trong phạm vi môn không.

### Cốt lõi quầy (rạp không chạy thiếu cái này)

| Năng lực rạp | Hệ thống mình | BTL? |
|---|---|---|
| Lưới suất theo phòng + % ghế / sold-out | List giờ theo phim+ngày; occupancy ADMIN-only | Occupancy + sold-out trên POS: **nên** (1.11). Không 2D/phụ đề |
| Hold theo **máy / ca**, không theo user | Hold theo `cashierUserId` | Ngoài — một account demo |
| Tiền khách đưa, thối, split tender | Nút “Đã thu tiền” | Tiền đưa/thối mock: **nên**. Split/thẻ: không |
| Void / hoàn / đổi suất trong ngày | Chỉ hủy `HOLD` | Đổi suất = cancel + hold mới: **nên** (1.3). Void sau `CONFIRMED` + PIN: ngoài |
| Tìm đơn + in lại | In ngay sau bán | Ô mã vé: **nên** (1.9). CRM SĐT: ngoài |
| Check-in cửa 1 lần, cửa sổ giờ | `GET` mã | `CHECKED_IN`: **nên** nếu demo cửa (1.5). Offline scanner: ngoài |
| F&B bán không cần vé, kho, in bếp | F&B theo reservation | Ngoài (docs/07: combo một SKU) |
| Mở ca / đóng ca / quỹ | Không | Ngoài |
| Ghế đặc biệt, house seat | STANDARD / VIP ×1.2 | Ngoài |
| Cảnh báo rated C18 | Badge | Cảnh báo POS: optional. Chặn tuổi: ngoài |
| Cắt bán sau giờ bắt đầu | Bán đến `endAt` | Re-check `endAt` lúc confirm: **nên** (1.6) |
| VAT / HĐĐT | Một `totalAmount` | Ngoài |

### UI/UX POS (rạp tối ưu thu ngân)

Nút lớn, ít scroll, không galaxy khi in; tìm phim; mặc định hôm nay (không look-ahead ngầm); stepper + “Hủy giao dịch”; sau in → “Giao dịch mới”; soát vé full-màn xanh/đỏ; DT theo ca vs cả rạp.

### Ngoài phạm vi đã chốt (đừng hứa trên slide)

Membership / điểm, nhiều cụm rạp, VNPay/Momo thật, 2D/3D/IMAX, copy lịch tuần, canvas Vista, Redis/WebSocket, công thức combo, U22 / Happy Day, kiosk riêng.

---

## Phụ thuộc và thứ tự

```
Việc 1 (đúng nghiệp vụ bán)
    │
    ├─ 1.2 + 1.3 + 2.8     hold còn sống đúng vòng đời     ← làm trước
    │         │
    │         └─ 1.6 + 2.4  confirm không bán suất chết
    │
    ├─ 1.1 + 1.11 + 1.14   chọn đúng suất / ngày            ← song song được
    │
    ├─ 1.5                 check-in (schema vé mới)         ← sau hold ổn
    │         └─ 1.9 + 2.11 tìm đơn / in lại / print CSS
    │
    └─ 1.7 + 2.2 + 2.5     thu hẹp getOne / DELETE
              │
Việc 2 (mùi còn lại) ──── 2.1, 2.10, 2.14, docs
              │
Việc 3 (gap rạp) ──────── chỉ lấy mục cột “BTL = nên”
                          phần còn lại: ngoài scope
```

Ba việc **không ngang nhau**:

| Việc | Phụ thuộc | Rủi ro nếu làm trước | Giá trị demo / bảo vệ |
|---|---|---|---|
| **1. Bugs** | Không cần Việc 2 hay 3. Dựa trên hold đã có (docs/12) | Thấp — sửa luồng cũ | Cao — thầy bấm POS sẽ thấy |
| **2. Smell** | Một phần **trùng file** với Việc 1 (`BookingService`, checkout, print) | Làm trước → conflict / làm lại khi sửa bug | Trung — sạch hợp đồng, ít “wow” |
| **3. Gap rạp** | Check-in / in lại / occupancy **cần** model ổn từ Việc 1 | Làm trước = đắp feature lên hold/soát đang sai | Cao nếu chọn đúng mục “nên”; rất cao rủi ro scope |

**Không** implement Việc 3 như một epic “làm giống CGV”. Mọi mục gap có cột “nên” đã map về số Việc 1.

---

## Nên làm trước: Việc 1 — vòng đời hold trên POS

**Chọn:** 1.2 (countdown F&B + checkout) + 1.3 (hủy hold khi rời giao dịch) + 2.8 (không gia hạn hold mỗi click).

**Vì sao kỹ thuật — không phải vì “trông quan trọng hơn”:**

1. **Đây là invariant đã hứa với môn.** Docs/05 và docs/12 nói ghế chỉ thuộc một reservation `HOLD` trong 8 phút, scheduler 30s, confirm 409 `HOLD_EXPIRED`. UI bắp nước **không hiện** invariant đó; rời trang **không giải** nó. Demo “không bán trùng” vẫn đúng, nhưng demo “quầy bán liên tục” sai: ghế ma, hold chồng suất, đồng hồ chỉ có ở 2/3 bước.

2. **Không phụ thuộc schema mới.** Countdown đọc `holdExpiresAt` đã có trên `Reservation`. `cancelHold` + `DELETE /api/bookings/{id}` đã có. Chỉ nối UI/logout vào API. Check-in (1.5) cần cột/status vé mới — làm trước sẽ lệch với “vé = CONFIRMED mãi”.

3. **Các bug khác ngồi trên hold.** Look-ahead sai ngày (1.1) độc lập, nhưng nếu hold cũ không hủy, thu ngân chọn đúng suất vẫn đụng ghế của giao dịch trước. Confirm `endAt` (1.6) chỉ an toàn khi hold không bị gia hạn vô hạn bằng click. `getOne` (1.7) là IDOR; không làm mất ghế trên sàn demo.

4. **Việc 2 đụng cùng class.** `@PreAuthorize` trên DELETE, assert `channel == POS`, thu hẹp `getOne` nên **dọn chung PR** với 1.3/1.6/1.7 — không tách PR “smell” trước rồi revert.

5. **Việc 3 chờ cửa sổ giờ vào rạp.** Check-in “T−30 … T+15” giả định bán/hold đã cắt đúng `endAt` và vé có trạng thái. Làm cửa trước = soát mã `CONFIRMED` giả.

**Không làm trước:** look-ahead (1.1) — độc lập nhưng chỉ sai **chọn ngày**, không phá kho ghế. Occupancy POS — API đã có cho ADMIN, mở cho CASHIER là việc nhỏ **sau** khi ngày/suất ổn. Check-in, tiền thối, ca quỹ, F&B standalone — sau hoặc ngoài BTL.

### Gói triển khai đề xuất (một PR)

1. Countdown `holdExpiresAt` trên F&B + checkout; hết hạn → disable CTA, về sơ đồ ghế.  
2. “Hủy giao dịch” + về `/pos` / đổi suất / logout cashier → `cancelHold` (best-effort).  
3. Không reset 8 phút mỗi click ghế (giữ hạn gốc, hoặc gia hạn tối đa một lần).  
4. Đi kèm trong cùng PR nếu chạm file: 401/logout → `/pos/login`; `confirmCash` re-check `endAt` + `channel`; `@PreAuthorize` DELETE.

Xong gói này mới: look-ahead/occupancy → check-in một lần → tìm đơn/in lại.

---

## Ngoài ba việc (nhắc để không trôi)

- Trang khách `MovieDetailPage` / chip giờ: **không** nâng cho đến khi admin suất xong và có yêu cầu ([docs/11](11-phuong-an-suat-chieu.md)).  
- Redis / WebSocket: không thay unique DB ([docs/12](12-khoa-ghe-dong-thoi.md)).  
- Không commit secret (`.env`, mật khẩu JDBC).
