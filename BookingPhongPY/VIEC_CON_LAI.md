# Việc còn lại so với sàn phòng trọ

Phần chạy nhiều người cùng lúc đã làm xong: Gunicorn nhiều worker, Redis cho giới hạn request, lịch nhắc việc tách riêng, Nginx phục vụ file tĩnh, chặn mật khẩu `admin123` khi `FLASK_ENV=production`, chat giảm tần suất hỏi server. Những mục dưới đây chưa làm.

## Tìm phòng theo vị trí

Người thuê thường lọc theo quận, rồi muốn biết phòng gần trường, gần bến xe, trong bán kính bao nhiêu cây số. Web đã lọc được giá, diện tích, quận, loại phòng và tiện ích. Tọa độ vẫn lưu trong bảng `phong`, nhưng trang không còn bản đồ và không xếp theo khoảng cách. Làm lại phần này khi có nguồn bản đồ dùng được trên máy chủ, và chỉ bật lại nếu chủ dự án muốn.

## Tin cậy người đăng

- `cccd_so` chỉ là ô chữ trên hồ sơ. Cột `cccd_mat_truoc` và `cccd_mat_sau` đã có trong database, chưa có trang tải ảnh và chưa có bước admin đối chiếu.
- Chưa xác thực số điện thoại bằng OTP. Sàn lớn chặn tin ảo chủ yếu bằng số điện thoại thật và duyệt tay.
- Cờ `is_verified` là admin bật tay, chưa gắn với giấy tờ.

## Tiền vào và tiền ra

- VietQR và webhook ghi nhận cọc là đủ cho demo. Chưa đối soát sao kê tự động.
- Hủy đơn đã chuyển giao dịch cọc sang `can_xem_lai`. Chưa có bước hoàn tiền, số tiền hoàn, người xác nhận và biên lai.
- Mã giảm không trừ tiền cọc. Giữ nguyên quy tắc đó khi làm hoàn tiền.

## Sau khi người thuê đã ở

- Đã có hợp đồng và hóa đơn điện nước, kèm PDF.
- Chưa có biên bản bàn giao phòng, ảnh chỉ số công tơ lúc vào và lúc ra.
- Nhắc hóa đơn quá hạn đã có trong lịch buổi sáng, nhưng mới là thông báo trong web và email nếu SMTP gửi được. Chưa có SMS.
- Chưa có luồng trả phòng: chốt chỉ số, trừ hỏng hóc, hoàn cọc, đóng hợp đồng.

## Chưa cần cho bản này

App điện thoại, video 360, gợi ý phòng bằng mô hình, ký hợp đồng điện tử, ví điện tử. Đó là việc của sàn lớn khi đã có lượng tin và người vận hành.
