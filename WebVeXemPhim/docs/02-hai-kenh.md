# Hai kênh dùng chung một kho ghế

Khách (`ONLINE`) và thu ngân (`POS`) gọi cùng `POST /api/bookings/hold` và cùng sơ đồ `seatMap`. Khác nhau ở vai được gọi, cửa sổ giờ còn bán, và cách thu tiền.

## Ai được giữ ghế

| Kênh trong body | Vai JWT | Sai vai |
|---|---|---|
| `ONLINE` | `CUSTOMER` | `403 FORBIDDEN` |
| `POS` | `CASHIER` | `403 FORBIDDEN` |

Hold online ghi `customerUserId`. Hold quầy ghi `cashierUserId`.

## Cửa sổ còn bán

Lúc giữ ghế (`hold`):

- Khách: `startAt` phải còn ở tương lai. Đúng giờ bắt đầu trở đi trả "Suất không còn mở bán".
- Quầy: bán được sau giờ bắt đầu, chặn khi `endAt` đã tới. `endAt` = thời lượng phim + 15 phút.

Danh sách suất (`ShowtimeService.list`) lọc cùng hướng đó. Khách không thấy suất đã tới giờ bắt đầu. Thu ngân còn thấy suất cho đến `endAt`. Admin gửi `includePast=true` thì thấy mọi suất `SCHEDULED` trong ngày, kể cả đã bắt đầu. Người không phải admin gửi cờ này không có tác dụng.

Phòng `INACTIVE` biến khỏi list của khách và thu ngân, và hold trả `400 ROOM_INACTIVE`. Admin vẫn thấy suất của phòng đã dừng.

Phim `COMING` hoặc `HIDDEN` không hold được: `400 MOVIE_NOT_ON_SALE`.

## Thu tiền

- Khách: `confirmOnline`. Chỉ chủ hold (`customerUserId`) và chỉ đơn `ONLINE`. Phương thức mặc định `ONLINE_MOCK`.
- Quầy: `confirmCash`. Chỉ thu ngân tạo hold đó và chỉ đơn `POS`. Phương thức `CASH`.

Cả hai đi vào `finalizePayment`.

Lúc thu tiền, quầy bị kiểm lại `endAt`. Nếu suất đã kết thúc, hold bị `EXPIRED` và trả `409 SHOWTIME_ENDED`. Suất không còn `SCHEDULED` thì cả hai kênh nhận `409 SHOWTIME_CLOSED`.

Xác nhận online không kiểm lại `startAt`. Khách đã giữ ghế trước giờ chiếu vẫn thanh toán được sau giờ bắt đầu, miễn hold chưa hết 8 phút và suất vẫn `SCHEDULED`. Chặn sau giờ bắt đầu chỉ áp lên lần giữ ghế mới.

## Soát vé

Mỗi ghế đã thanh toán có một `tickets.ticket_code` duy nhất, dạng `VX-{năm}-{id}`. Status lúc in là `ISSUED`.

`POST /api/tickets/{code}/check-in` chỉ `CASHIER` hoặc `ADMIN`. Vé chưa `CONFIRMED` trả `400 NOT_PAID`. Vé đã `CHECKED_IN` trả `409 ALREADY_CHECKED_IN`. Lần soát thành công ghi `checkedInAt`.

## Ai được xem đơn

`GET /api/bookings/my` chỉ trả đơn `CONFIRMED` của `customerUserId` trong token. Không nhận `userId` trên query.

`GET /api/bookings/{id}` cho phép admin, chủ đơn (khách hoặc thu ngân ghi trên đơn), hoặc bất kỳ thu ngân nào khi đơn đã `CONFIRMED` (để in lại). Người còn lại nhận `403`.
