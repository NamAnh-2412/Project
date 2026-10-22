# Chỗ còn khác với VYRO, để nâng cấp sau

Đối chiếu mã nguồn `WebTheThao` và `C:\xampp\htdocs\VYRO_SPORT_TEST` ngày 22/10/2026. File này chỉ ghi việc chưa làm. Không phải danh sách phải làm ngay.

Phần sản phẩm đã kéo gần nhau: bộ lọc giới tính, thương hiệu, size, màu, giá, còn hàng và sắp xếp; ảnh đổi theo màu; bảng size và gợi ý theo chiều cao, cân nặng. Cửa hàng không dùng cổng “phải xác minh mới được bán” của VYRO.

## Đã có ở cả hai, không nâng cấp lại

- Catalog bán và thuê, giỏ hỗn hợp, checkout, MoMo, QR, COD, GHN tỉnh/quận/phường cũ.
- POS, kho, phiếu nhập, lịch thuê, gia hạn, trả, cọc, tài chính, bill, bàn giao.
- Chat một hội thoại với cửa hàng, thông báo, banner trang chủ.
- Đánh giá chữ sau khi mua hoặc thuê.

## VYRO còn, cửa hàng chưa có

Làm từng mục một. Mỗi mục cần test riêng trước khi sang mục sau.

| Việc | VYRO đang làm | Cửa hàng hiện tại | Khi nào nên làm |
| --- | --- | --- | --- |
| Tìm gần đúng | Gõ lệch vẫn gợi ý một cụm gần nghĩa, bộ lọc khác giữ nguyên | Ô tìm kiếm khớp tên thường | Để sau. Chủ shop muốn suy nghĩ thêm trước khi làm |
| Đánh giá có ảnh | Khách gửi ảnh, shop trả lời, ghim một đánh giá | Đánh giá chữ. Admin chỉ xóa | Khi cần bằng chứng ảnh trên trang áo |
| Chọn dòng trong giỏ | Khách tick dòng nào thì chỉ những dòng đó sang thanh toán | Cả giỏ đi thanh toán | Khi giỏ hay bị trộn món chưa muốn mua |
| Mua ngay | Một nút tạo ý định thanh toán riêng, không phụ thuộc cả giỏ | Không có | Cùng đợt với chọn dòng, vì cả hai đụng `CartService` |
| Chặn đơn trùng | `CheckoutIdempotency` bỏ lần bấm thanh toán thứ hai | Bấm hai lần có thể tạo hai đơn | Trước khi mở traffic thật |
| Khách báo đã chuyển khoản | Khách gửi thông báo, admin xác nhận thông báo đó | Admin tự đối chiếu và bấm xác nhận khoản | Khi khách chuyển khoản online nhiều |
| Địa giới mới | API tỉnh và phường sau sáp nhập, trang admin thử kết nối GHN | Tỉnh, quận, phường cũ và tính phí | Khi GHN bắt buộc địa chỉ mới |
| Kiện hàng | Gram và kích thước trên form sản phẩm, hiện ở trang chi tiết | Chưa lưu | Khi phí ship cần đúng khối lượng từng áo |
| MoMo mô phỏng | Trang giả lập kết quả khi bật môi trường thử | Chỉ cổng MoMo thật | Chỉ để demo, không thay cổng thật |
| Chat AI | Hỏi sản phẩm, đơn, size, chính sách. Admin cấu hình nhà cung cấp | Chỉ chat với nhân viên | Không ưu tiên. In 3D và kho đã là luồng tư vấn chính |

## Cố ý không mang sang

| Việc của VYRO | Lý do bỏ |
| --- | --- |
| `is_verified` mới được hiện catalog | Một cửa hàng. Sản phẩm đang bán và không bị khóa là đủ |
| Ô in tên và số ngay trên trang sản phẩm, cộng phụ phí vào giỏ | `/in-3d` đã nhận nội dung in, báo giá và trừ tồn khi khách chấp nhận |
| Studio 2D `/in-theo-yeu-cau` | URL này đã chuyển sang `/in-3d`. Không dựng lại trang 2D |
| Tìm sản phẩm bằng AI | Trùng việc của bộ lọc và của chat nhân viên |

## Cửa hàng đang hơn VYRO

Giữ các luồng này khi nâng cấp. Không đổi chúng chỉ để giống VYRO.

- **In 3D áo.** Chọn phôi hoặc áo có sẵn, màu và size còn hàng. Nháp, gửi và báo giá không trừ kho. Khách chấp nhận thì đơn chờ chốt và tồn bán bị trừ ngay. Nhân viên vẫn bấm chốt. Giá online là báo giá, có thể cao hơn giá bán.
- **Quầy phôi.** Nhân viên chọn phôi, màu, size, số lượng. Thu giá bán của biến thể. Sau khi thu, chat nhắc khách gửi nội dung in. Không trừ kho lần thứ hai.
- **In file mô hình.** Khách gửi file, shop báo giá. Không trừ tồn áo.
- Phôi `is_print_blank` không xuất hiện trong catalog cửa hàng.

## Thứ tự gợi ý khi bắt đầu nâng cấp

1. Chặn đơn trùng lúc checkout.
2. Chọn dòng trong giỏ và Mua ngay, làm cùng một đợt vì cùng một giỏ.
3. Ảnh đánh giá, trả lời và ghim.
4. Khách báo đã chuyển khoản.
5. Địa giới mới, chỉ khi GHN yêu cầu.
6. Tìm gần đúng, khi chủ shop chốt cách gợi ý.
7. Kiện hàng, MoMo mô phỏng, chat AI để cuối.

Mỗi mục xong thì chạy `php artisan test` lọc đúng test của luồng đó, rồi mở trang liên quan trên `http://localhost/WebTheThao/public`.
