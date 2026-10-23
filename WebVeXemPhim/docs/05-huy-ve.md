# Hủy vé đã mua

Đã làm theo quyết định nhóm, ngày 10/10/2026.

## Luật

Chỉ vé **online** đã thanh toán (`CONFIRMED`). Vé quầy thu tiền mặt không hủy.

Khách hủy trên **Vé của tôi**, trước giờ chiếu **30 phút** (`app.cancel-before-minutes`). Vé đã soát (`CHECKED_IN`) thì không hủy. Hủy cả đơn, không hủy từng ghế.

Số hoàn bằng `totalAmount` — số tiền khách đã trả sau giảm giá. Đơn 100.000đ còn 90.000đ thì hoàn 90.000đ. Ghi một dòng `payments` với `method=REFUND`, `status=REFUNDED`. Không gọi hoàn MoMo ở bước này.

Mã giảm giá không trả lại lượt (`usedCount` giữ nguyên).

Đơn chuyển `REFUNDED`, không xóa. Vé chuyển `CANCELLED` nên quầy không soát được. Ghế xóa khỏi `reservation_seats` và phát trạng thái `AVAILABLE`, nên người khác đặt lại được. Suất không bị xóa.

## Thông báo

Cùng một nội dung ghi vào `staff_notices`:

- Admin xem tại `/admin/notices` và nhận email nếu tài khoản admin có email và SMTP đã cấu hình.
- Nhân viên xem tại `/pos/notices`. Không gửi email cho quầy.

## API

`POST /api/bookings/{id}/cancel-tickets` — chỉ CUSTOMER, chủ vé.

`DELETE /api/bookings/{id}` vẫn chỉ hủy giữ chỗ `HOLD`, chưa thu tiền.
