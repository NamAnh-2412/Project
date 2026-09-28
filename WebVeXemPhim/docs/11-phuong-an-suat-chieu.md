# 11. Phương án suất chiếu

Một rạp, hai phòng. Không đa cụm, không 2D/3D/IMAX, không bảng giá cuối tuần. Bắp nước là bước tùy chọn sau ghế (không thuộc lịch suất).

**Thứ tự:** xong admin trước. Trang khách chỉ làm sau khi admin ổn.

---

## A. Trang khách — để sau (không làm lúc này)

Hiện tại lịch chiếu nằm trên chi tiết phim (`/movies/:id`, alias `/showtimes/:id`): date native + chip `giờ · phòng · giá`. Phim `COMING` không có lịch. Admin xem được nhưng không đặt.

Khi quay lại, giữ luồng **Phim → ngày → giờ → ghế**. Không thêm bước chọn rạp/thành phố.

Nâng vừa đủ so với Galaxy/CGV (một rạp):

- Dải 7 ngày (Hôm nay / Thứ …) thay ô `date` native.
- Chip giờ nổi, phòng + giá là phụ. Nhóm theo phòng nếu một ngày nhiều suất.
- Suất đã qua ẩn (API public đã lọc). Empty state rõ.
- Chưa làm: hết vé trên chip (occupancy đang ADMIN-only), format phụ đề, chọn rạp.

POS `/pos` giữ list suất theo ngày; không gộp vào trang khách.

---

## B. Trang admin — làm trước (chốt)

Mô hình: **lịch phòng theo một ngày**, không form tạo nhanh, không canvas Vista.

Vì sao: 2 phòng nên lưới ngày đọc được; overlap/cleanup/`HAS_TICKETS`/occupancy API đã có; cùng kiểu phân cấp trang Thể loại (phòng chứa suất).

### Bề mặt

1. Đầu trang: tiêu đề, chọn ngày (mặc định hôm nay), thống kê `X suất · Y đã bán ghế`.
2. Form tạo có nhãn: Phim (`SHOWING`), Phòng (`GET /api/rooms`), giờ bắt đầu, giá VNĐ. Hiện **giờ kết thúc dự kiến** = thời lượng phim + 15 phút. Không hardcode Phòng 1/2.
3. Hai cột/khối **Phòng 1** / **Phòng 2**. Mỗi suất là thẻ: khoảng giờ, tên phim, giá, `hold / booked / sức chứa`, Sửa / Hủy.
4. Suất đã qua trong ngày: xám, không sửa/hủy.
5. Sửa: đổ form, `PUT`. Đã bán vé → khóa `HAS_TICKETS`. Đang giữ ghế → khóa `HAS_HOLDS`. Phòng `INACTIVE` → khóa Sửa (vẫn Hủy nếu trống).
6. Hủy: xác nhận, `DELETE` soft-cancel. Đã bán hoặc đang hold → không cho hủy.
7. Tạo/sửa: `startAt` phải tương lai (`SHOWTIME_PAST`); phim phải `SHOWING` cả lúc sửa; phòng phải `ACTIVE`.
8. Dừng phòng: khách không thấy / không hold suất còn lại (`ROOM_INACTIVE`).
9. Overlap: banner đỏ như hiện tại (`SHOWTIME_OVERLAP`).

Không làm ở pha này: copy tuần, tạo hàng loạt, kéo thả canvas, CRUD phòng, suất đã `CANCELLED` trên list.

### API nhỏ để admin thấy đúng ngày

`GET /api/showtimes` public vẫn ẩn suất đã qua (trang khách).

Thêm cho ADMIN (JWT), không đổi contract khách:

- `includePast=true` — suất `SCHEDULED` cả ngày, kể cả đã bắt đầu.
- Response thêm `status`.
- Occupancy trên từng suất (`hold`, `booked`, `capacity`) để khỏi N lần `GET /occupancy`.

`capacity` = `rowCount * seatsPerRow` của phòng.

PUT/DELETE/POST giữ nguyên. Form gọi `GET /api/rooms`.
