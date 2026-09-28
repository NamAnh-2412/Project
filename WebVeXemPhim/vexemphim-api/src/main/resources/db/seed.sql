-- =====================================================================
-- Du lieu khoi tao cho vexemphim (thay cho DataSeeder.java da xoa).
-- Chay TAY trong MySQL Workbench. Spring KHONG tu chay file nay.
--
-- Thu tu: chay API mot lan de Hibernate tao bang (ddl-auto=update),
-- tat API, roi chay file nay, roi bat lai API.
--
-- Mat khau da bam BCrypt: admin 193850091011 / NhanVien NhanVien123 / customer123
-- =====================================================================

USE vexemphim;

-- ---------------------------------------------------------------------
-- 0) XOA SACH (tuy chon). Bo comment neu muon lam lai tu dau.
--    Thu tu con -> cha de khong vi pham khoa ngoai.
-- ---------------------------------------------------------------------
-- DELETE FROM payments;
-- DELETE FROM tickets;
-- DELETE FROM reservation_seats;
-- DELETE FROM reservations;
-- DELETE FROM showtimes;
-- DELETE FROM seats;
-- DELETE FROM rooms;
-- DELETE FROM movies;
-- DELETE FROM genres;
-- DELETE FROM users;

-- ---------------------------------------------------------------------
-- 1) TAI KHOAN (bat buoc)
--    ADMIN va CASHIER khong the tu dang ky tren web (register luon ra
--    CUSTOMER), nen phai chen o day.
-- ---------------------------------------------------------------------
INSERT INTO users (username, password, role, enabled, created_at) VALUES
  ('admin',     '$2a$10$mESibZuQPRGlxAm3e..pcuLcJbIh0rpyeU0wSeXJeZ4JSJH0k0NQy', 'ADMIN',    1, NOW()),
  ('NhanVien',  '$2a$10$PvvSJqAa97sWT7RiMXzodObR//DJ10S.VScsFOjcisJFRMY5WRqYy', 'CASHIER',  1, NOW()),
  ('customer1', '$2a$10$Sb3o4ZOtU0.OAD.N3DBxL..DsQ42WRjE6c0vYJeGifG8bmKabCvy6', 'CUSTOMER', 1, NOW());

-- ---------------------------------------------------------------------
-- 2) PHONG + GHE (bat buoc)
--    Seed 2 phong ban dau. ADMIN co the POST /api/rooms de them phong
--    (tu sinh ghe). Thieu phong thi khong tao duoc suat / so do ghe trong.
--    Moi phong: 8 hang A..H x 10 ghe, 2 hang cuoi (G, H) la VIP.
-- ---------------------------------------------------------------------
INSERT INTO rooms (name, row_count, seats_per_row) VALUES
  ('Phòng 1', 8, 10),
  ('Phòng 2', 8, 10);

INSERT INTO seats (room_id, row_label, number, type)
SELECT r.id,
       CHAR(64 + rw.n USING utf8mb4),
       sn.n,
       IF(rw.n > r.row_count - 2, 'VIP', 'STANDARD')
FROM rooms r
JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
      UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
      UNION SELECT 9 UNION SELECT 10) rw
JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) sn
WHERE rw.n <= r.row_count
  AND sn.n <= r.seats_per_row;

-- ---------------------------------------------------------------------
-- 3) THE LOAI + PHIM (tuy chon — co the tu them trong /admin)
-- ---------------------------------------------------------------------
INSERT INTO genres (name) VALUES
  ('Hành động'), ('Tình cảm'), ('Hoạt hình'), ('Kinh dị');

INSERT INTO movies (title, duration_minutes, rated, description, status, genre_id) VALUES
  ('Mưa Đỏ',         118, 'C16', 'Bom tấn hành động Việt — trận chiến và tình đồng đội.',
   'SHOWING', (SELECT id FROM genres WHERE name = 'Hành động')),
  ('Em Và Trịnh',    136, 'C13', 'Câu chuyện âm nhạc và những mối tình.',
   'SHOWING', (SELECT id FROM genres WHERE name = 'Tình cảm')),
  ('Robot Tí Hon',    95, 'P',   'Phiêu lưu cho cả gia đình.',
   'SHOWING', (SELECT id FROM genres WHERE name = 'Hoạt hình')),
  ('Bóng Ma Rạp Cũ', 102, 'C18', 'Sắp chiếu — chưa mở bán vé.',
   'COMING',  (SELECT id FROM genres WHERE name = 'Kinh dị'));

-- ---------------------------------------------------------------------
-- 4) SUAT CHIEU (tuy chon)
--    Gio tinh theo NOW() nen luon o tuong lai -> dat ve duoc ngay.
--    end_at = start_at + thoi luong + 15 phut don phong (giong service).
--    Phim COMING khong co suat: dung y do (hold se tra 400).
-- ---------------------------------------------------------------------
INSERT INTO showtimes (movie_id, room_id, start_at, end_at, base_price, status)
SELECT m.id,
       r.id,
       t.start_at,
       t.start_at + INTERVAL (m.duration_minutes + 15) MINUTE,
       t.price,
       'SCHEDULED'
FROM (
  SELECT 'Mưa Đỏ' AS title, 'Phòng 1' AS room,
         DATE_ADD(DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'), INTERVAL 2 HOUR) AS start_at, 75000 AS price
  UNION ALL SELECT 'Mưa Đỏ', 'Phòng 2',
         DATE_ADD(DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'), INTERVAL 6 HOUR), 90000
  UNION ALL SELECT 'Em Và Trịnh', 'Phòng 2',
         DATE_ADD(DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'), INTERVAL 2 HOUR), 80000
  UNION ALL SELECT 'Robot Tí Hon', 'Phòng 1',
         TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '10:00:00'), 65000
  UNION ALL SELECT 'Em Và Trịnh', 'Phòng 1',
         TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '14:00:00'), 85000
  UNION ALL SELECT 'Mưa Đỏ', 'Phòng 2',
         TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '20:00:00'), 90000
) t
JOIN movies m ON m.title = t.title
JOIN rooms  r ON r.name  = t.room;

-- ---------------------------------------------------------------------
-- 4b) BAP NUOC (tuy chon — API seed neu bang rong)
-- ---------------------------------------------------------------------
INSERT INTO concession_products (name, price, type, status) VALUES
  ('Bắp rang nhỏ', 45000, 'FOOD', 'ACTIVE'),
  ('Bắp rang lớn', 65000, 'FOOD', 'ACTIVE'),
  ('Pepsi', 39000, 'DRINK', 'ACTIVE'),
  ('Nước suối', 20000, 'DRINK', 'ACTIVE'),
  ('Combo bắp lớn + Pepsi', 89000, 'COMBO', 'ACTIVE'),
  ('Combo đôi', 129000, 'COMBO', 'ACTIVE');

-- ---------------------------------------------------------------------
-- 5) KIEM TRA
-- ---------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM users)     AS users,
       (SELECT COUNT(*) FROM rooms)     AS rooms,
       (SELECT COUNT(*) FROM seats)     AS seats,
       (SELECT COUNT(*) FROM genres)    AS genres,
       (SELECT COUNT(*) FROM movies)    AS movies,
       (SELECT COUNT(*) FROM showtimes) AS showtimes,
       (SELECT COUNT(*) FROM concession_products) AS concessions;
