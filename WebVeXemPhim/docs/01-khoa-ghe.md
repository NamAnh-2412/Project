# Khóa ghế khi hai người cùng đặt

Đây là phần khó nhất của hệ thống. Hai kênh (khách và quầy) nhìn cùng một sơ đồ ghế. Nếu cả hai thấy ghế trống và cùng bấm, chỉ một người được ghế.

Quyền sở hữu không nằm trên bảng `seats`. Bảng đó chỉ là sơ đồ phòng (hàng, số, loại STANDARD/VIP). Ghế của một suất là một hàng trong `reservation_seats`.

## Trọng tài

Hai lớp, cả hai đều trong `BookingService.hold`:

1. Khóa hàng suất `showtimes` bằng `PESSIMISTIC_WRITE` ngay khi vào transaction. Hai request cùng suất phải xếp hàng. Request sau chỉ kiểm tra ghế khi request trước đã commit hoặc rollback.
2. Ràng buộc `UNIQUE (showtime_id, seat_id)` trên `reservation_seats`. Nếu khóa bị lỡ, request insert sau vẫn fail. Code bắt `DataIntegrityViolationException` và trả `409 SEAT_TAKEN`.

`attachSeat` còn kiểm tra `existsByShowtimeIdAndSeatId` trước khi insert. Kiểm tra này chỉ đúng vì hàng ghế được xóa khi nhả chỗ. Unique là lớp chốt.

Không dùng Redis và không dùng WebSocket. Sơ đồ ghế trên web làm mới bằng poll. Poll chỉ để nhìn. Ai thắng vẫn do database.

## Trạng thái trên sơ đồ

`BookingService.seatMap` suy trạng thái từ hàng `reservation_seats` và `reservations.status`:

| Hàng `reservation_seats` | Reservation | Ô ghế |
|---|---|---|
| không có | — | `AVAILABLE` |
| có | `HOLD` và đúng user đang đăng nhập | `HOLD_MINE` |
| có | `HOLD` của user khác hoặc kênh khác | `HOLD` |
| có | `CONFIRMED` | `BOOKED` |

`HOLD_MINE` so đúng người: kênh `ONLINE` so `customerUserId`, kênh `POS` so `cashierUserId`. Cùng một tài khoản ở kênh kia không được tính là ghế của mình.

## Vòng đời giữ chỗ

`app.hold-minutes=8` trong `application.properties`. Lúc tạo hold, `holdExpiresAt = now + 8 phút`.

Đổi ghế trên cùng một hold (`replaceHoldSeats`) không gia hạn đồng hồ nếu `holdExpiresAt` đã có. Chỉ gán hạn khi hạn đang null.

Tối đa 8 ghế mỗi lần giữ. Quá 8 trả `400 VALIDATION`.

Cùng một hàng, ghế đã thanh toán (`CONFIRMED` / `BOOKED`) không cho giữ ghế cách đúng một số. Ghế kề bên, hoặc cách từ hai ghế trống trở lên, vẫn được. Ghế chỉ đang `HOLD` chưa thanh toán không kích hoạt luật này. Ghế chọn cùng một lần giữ tính là đã lấp chỗ trống ở giữa. Vi phạm trả `400 SEAT_GAP`. Bỏ bớt ghế trên hold đang mở vẫn được, để gỡ ghế đã chọn.

Nhả ghế là xóa hàng `reservation_seats` (`expire` gọi `seats.clear()`), rồi đổi status reservation:

- Người hủy: `CANCELLED`. Chỉ hủy khi status còn `HOLD`.
- Hết giờ: `EXPIRED`.

`HoldExpiryScheduler` chạy mỗi 30 giây, tìm `HOLD` có `holdExpiresAt` đã qua và gọi cùng hàm xóa ghế. Thanh toán cũng tự hết hạn nếu bấm sau `holdExpiresAt`, không cần đợi scheduler: trả `409 HOLD_EXPIRED`.

Một user trên một suất chỉ giữ một hold đang mở. `findMyOpenHold` lấy hold còn hạn của đúng user và đúng kênh. Hold hết hạn bị đánh `EXPIRED`. Nếu lỡ có hai hold mở, hold thứ hai bị `CANCELLED`.

## Khi thanh toán

`finalizePayment` nếu reservation đã `CONFIRMED` thì trả lại vé cũ, không tạo vé lần hai. Nếu không còn `HOLD`, hoặc đã quá `holdExpiresAt`, trả `409 HOLD_EXPIRED` và xóa ghế.

Giá ghế chốt lúc giữ: VIP nhân `app.vip-multiplier` (mặc định 1,2), làm tròn đơn vị. Bắp nước chụp đơn giá lúc thêm vào đơn, không đọc lại giá mới khi thanh toán.
