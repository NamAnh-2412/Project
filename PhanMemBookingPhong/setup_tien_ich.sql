-- Script thêm dữ liệu mẫu cho bảng tiện ích
-- Chạy script này trong phpMyAdmin để thêm các tiện ích cơ bản

-- Xóa dữ liệu cũ nếu có (tùy chọn)
-- DELETE FROM phong_tien_ich;
-- DELETE FROM tien_ich;

-- Thêm các tiện ích phổ biến
INSERT INTO `tien_ich` (`id`, `ten`, `icon`) VALUES
(1, 'WiFi miễn phí', '📶'),
(2, 'Điều hòa', '❄️'),
(3, 'Máy nước nóng', '🔥'),
(4, 'Tủ lạnh', '🧊'),
(5, 'Máy giặt', '🌀'),
(6, 'Tủ quần áo', '👔'),
(7, 'Giường', '🛏️'),
(8, 'Bàn làm việc', '🪑'),
(9, 'Ban công', '🌳'),
(10, 'Nhà vệ sinh riêng', '🚿'),
(11, 'Bếp', '🍳'),
(12, 'Chỗ đậu xe', '🚗'),
(13, 'Thang máy', '🛗'),
(14, 'Bảo vệ 24/7', '🛡️'),
(15, 'Camera an ninh', '📹'),
(16, 'Gần chợ', '🛒'),
(17, 'Gần trường học', '🏫'),
(18, 'Gần bệnh viện', '🏥'),
(19, 'Gần bến xe', '🚌'),
(20, 'Cửa sổ', '🪟');

-- Reset AUTO_INCREMENT
ALTER TABLE `tien_ich` AUTO_INCREMENT = 21;

