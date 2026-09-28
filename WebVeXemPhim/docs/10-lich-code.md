# 10. Lịch lập trình (chưa làm trong thư mục này)

Thư mục hiện tại **chỉ tài liệu**. Mỗi pha chạy được.

Stack: Java 21, **một** Spring Boot, JPA, MySQL, Security JWT, React Vite.

## Pha 0 — Khung repo (½–1 ngày)

- `vexemphim-api` (start.spring.io: Web, JPA, MySQL, Security, Validation) + `web-client`.
- Package đúng SOS01 (`ten_lop.tenkhongdauXXX.vexemphim`).
- CORS 5173, `uploads/`, `start-all.bat`.
- README code trỏ `docs/`.
- **Mốc:** GET `/api/movies` rỗng 200 (controller tạm).

## Pha 1 — Auth + login 3 role (1–2 ngày)

- Entity User, BCrypt, seed, login/register/me, JWT.
- React: login khách + POS, AuthContext, 401.
- **Mốc:** 3 token Postman, navbar đúng role.

## Pha 2 — Catalog 3 tầng + 1-N + page + upload (2 ngày)

- Genre, Movie, Room, Seat, Showtime; seed.
- GET public page/search/sort; ADMIN CRUD + API Key + upload poster.
- Overlap suất.
- Seat-map **chưa** hold: booking trả mọi ghế AVAILABLE (layout catalog).
- **Mốc:** chọn phim → suất → lưới ghế; admin thêm phim có ảnh.

## Pha 3 — Hold / confirm + đồng bộ 2 kênh (2–3 ngày) — trọng tâm môn

- Unique `(showtime_id, seat_id)`, timeout, scheduler.
- hold, confirm mock, confirm-cash, DELETE 204.
- 409 `SEAT_TAKEN`.
- **Mốc:** hai tab trùng ghế.

## Pha 4 — UI POS + online hoàn chỉnh (2 ngày)

- Countdown, checkout, `/tickets`, in POS, `/pos/tickets`.
- ProtectedRoute.
- Poll 5 giây.
- **Mốc:** hết luồng [05](05-luong-nghiep-vu.md) không Postman.

## Pha 5 — Vé, occupancy, Postman, demo (1–2 ngày)

- ticket_code, occupancy admin.
- Collection Postman + trang “Hướng dẫn chạy”.
- Profile `demo` hold 60 giây.

## Việc không làm trừ khi thừa thời gian

- VNPay, WebSocket, hoàn vé, ghế không đều, Docker, Eureka, Gateway, app mobile, CRUD phòng, ADMIN tạo cashier.

## Thứ tự ưu tiên nếu trễ deadline

Giữ **Pha 1 + 3** (JWT, không bán trùng, hai kênh). Có thể cắt đẹp UI admin nhưng **không** cắt unique ghế. Page/upload là điểm SOS — cắt sau unique, trước “đẹp”.

## Ghi chú máy

Ổ C từng đầy. Source nhỏ; đừng cài Docker lên C. Có thể chuyển workspace sang D khi Pha 0.
