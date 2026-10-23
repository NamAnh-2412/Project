# Quy tắc suất chiếu

Một rạp, nhiều phòng lấy từ `GET /api/rooms`. Không có chọn cụm rạp, không có 2D/3D, không có copy lịch cả tuần.

Giờ kết thúc không nhập tay. `ShowtimeService` tính `endAt = startAt + durationMinutes + 15`. Mười lăm phút là thời gian dọn phòng, và cũng là mốc thu ngân còn bán được (xem `docs/02-hai-kenh.md`).

## Chồng giờ

`ShowtimeRepository.findOverlaps` tìm suất `SCHEDULED` cùng `room_id`, khác id đang sửa, giao khoảng thời gian: `startAt < endAt mới` và `endAt > startAt mới`.

Tạo hoặc sửa mà dính suất khác trong cùng phòng trả `409 SHOWTIME_OVERLAP`. Hai phòng khác nhau được trùng giờ.

## Tạo, sửa, hủy

Tạo và sửa (`POST` / `PUT`, chỉ `ADMIN`):

- Phim phải đang bán. `COMING` và `HIDDEN` trả `400 MOVIE_NOT_ON_SALE`.
- Phòng phải `ACTIVE`. Phòng dừng trả `400 ROOM_INACTIVE`.
- `startAt` phải sau thời điểm hiện tại. Suất quá khứ trả `400 SHOWTIME_PAST`.
- Không chồng giờ, như trên.

Sửa và hủy còn chặn khi suất đã có người:

- Đã có vé `CONFIRMED`: `409 HAS_TICKETS`.
- Đang có ghế `HOLD`: `409 HAS_HOLDS`.

Hủy không xóa hàng. `cancel` đặt `status = CANCELLED`. List chỉ đọc suất `SCHEDULED`, nên suất đã hủy biến khỏi lịch khách, quầy và admin.

## Ai thấy gì trên list

`GET /api/showtimes` là public. Có JWT thì lọc thêm theo vai.

| Người gọi | Suất còn hiện |
|---|---|
| Khách hoặc chưa đăng nhập | `startAt` còn ở tương lai, phòng `ACTIVE` |
| `CASHIER` | chưa tới `endAt`, phòng `ACTIVE`, kèm số ghế hold / booked / sức chứa |
| `ADMIN` | như khách, trừ khi `includePast=true`: mọi suất `SCHEDULED` trong ngày đã chọn, kể cả đã bắt đầu, kèm occupancy |

`capacity` = `rowCount * seatsPerRow` của phòng. Khách không nhận số ghế đã bán trên list.

## Trang đang có

Admin `/admin/showtimes` là lịch một ngày theo phòng: chọn ngày, tạo, sửa, hủy, thấy hold / booked / sức chứa. Form lấy phòng từ API.

Trang khách `/movies/:id` vẫn là ô chọn ngày và chip `giờ · phòng · giá`. Dải 7 ngày chưa làm. Suất đã qua không hiện vì API đã lọc.
