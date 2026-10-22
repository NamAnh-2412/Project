# Phương án refactor

Mục tiêu là code ngắn và dễ đọc hơn, hành vi khách và nhân viên đang dùng giữ nguyên. Không nhét tính năng VYRO vào đợt này. Danh sách việc mới nằm ở [nang-cap-vyro.md](nang-cap-vyro.md).

Ngày chốt phương án: 22/10/2026.

## Việc không được làm trong lúc refactor

- Không gộp In 3D áo, yêu cầu in trong chat và in file mô hình thành một form. Ba luồng khác nhau: áo trừ tồn, chat báo giá, file mô hình không trừ áo.
- Không mở lại trang in 2D.
- Không thêm cổng xác minh sản phẩm, ô in tên/số trên trang áo, chat AI, tìm gần đúng.
- Không đổi trạng thái đơn, cách trừ kho, hay quy tắc báo giá được phép cao hơn giá bán.
- Không xóa bảng `membership_plans` / `memberships` hay giá trị `vnpay` trong enum MySQL ở pha đầu. PHP không bán gói thành viên. `vnpay` chỉ còn nhãn “chưa gắn merchant”. Đụng enum trên MySQL đang chạy cần migration riêng.

## Cách làm mỗi pha

1. Chạy test của đúng pha đó trước khi sửa, để biết nền đang xanh.
2. Sửa một chỗ. Giữ chữ ký method mà controller đang gọi.
3. Chạy lại đúng test đó. Xanh thì sang chỗ khác.
4. Không vừa tách class vừa đổi quy tắc nghiệp vụ.

Lệnh nền, khi bắt đầu và khi kết thúc toàn bộ:

```bash
php artisan test
```

Test dùng SQLite và `RefreshDatabase`. Không cần MySQL đang chạy. Ảnh trong test phải là JPEG nhỏ dựng bằng `UploadedFile::fake()->createWithContent`, vì máy không có GD.

## Pha 1. Catalog chỉ còn một cửa “được bán”

`CatalogService` đang lặp `is_active`, `is_suspended`, `is_print_blank` ở nhiều query. Đã có helper `published()`.

Việc: mọi danh sách cửa hàng đi qua helper đó. Studio và quầy phôi không dùng helper này, vì hai chỗ đó cần hiện phôi.

Giữ nguyên: phôi không ra catalog; Nam và Nữ gồm Unisex; trang chỉ thuê đếm món thuê trống; trang chỉ bán đếm tồn bán.

Test: `StorefrontPageTest`, `ProductDetailTest`.

## Pha 2. Tách `CheckoutOrchestrator` theo bước, chưa tách class mới

File khoảng 18 KB. `checkout()` đang chuẩn bị dòng, áp mã, tính ship, ghi đơn, khóa kho và tạo khoản.

Việc: cắt thành method private trong cùng class, tên theo bước đang có trong README mục checkout. `checkout()`, `confirmByShop()`, `cancel()` giữ nguyên chữ ký.

Chỗ dễ gãy: dòng `LineType::Print` không được `lockAndBook` giữ kho lần nữa, vì In 3D đã trừ lúc khách chấp nhận. Đơn in nhận tại shop, khi thu, đi qua `PaymentOrchestrator::collectPrintPickup`.

Test: `CheckoutWebTest`, `ShippingCodTest`, `MomoPaymentTest`, `JerseyStudioTest`, `PosTest`.

## Pha 3. Một chỗ tạo đơn in

`JerseyDesignService`, `PrintRequestService` và `ModelPrintService` đều tạo đơn dòng `print`. Mỗi service vẫn quyết định giá, kho và trạng thái của riêng nó.

Việc: method dùng chung chỉ nhận dữ liệu đã chốt (khách, dòng, tiền, ghi chú) rồi gọi `OrderWriter`. Không kéo báo giá, file mô hình hay trừ kho vào method đó.

Giữ nguyên:

- In 3D: chấp nhận thì đơn chờ chốt, tồn đã trừ, tiền đơn bằng báo giá.
- Quầy phôi: thu giá bán, không trừ lần hai, chat xin nội dung in.
- In mô hình: không trừ tồn áo.

Test: `JerseyStudioTest`, `PrintRequestTest`, `ModelPrintTest`, `PosTest`.

## Pha 4. Form sản phẩm

`ProductController` vừa validate, vừa upload ảnh đại diện, vừa ảnh theo màu. `ProductWriter` đã ghi sản phẩm.

Việc: controller chỉ còn nhận request và gọi writer. Upload file có thể là một method private cuối controller, chưa cần class mới.

Giữ nguyên: ảnh màu không bắt buộc; tên màu lấy từ ô Màu của phân loại; cùng màu thì một ảnh; sửa sản phẩm mà không gửi `size_guide_id` thì không xóa bảng size đang gắn; sửa không được ghi đè tồn kho.

Test: `AdminCrudTest` phần sửa sản phẩm, `ProductDetailTest`.

## Pha 5. Controller admin dày

Các file lớn nhưng đang đúng việc: `PosController`, `RentalController`, `InventoryController`, `OrderController`. Chỉ tách khi một action dài quá một màn hình và đã có test.

Thứ tự nếu làm: POS trước, vì quầy phôi và giỏ thường nằm cạnh nhau. Tách “danh sách phôi” và “thu tiền” thành method private. Không đổi session phiếu.

Test: `PosTest`, `RentalLifecycleTest`, `StockReceiptTest`, `OrderHandoverTest`, `CloseoutFlowsTest`.

## Pha 6. Xóa code chết, sau khi pha 1–4 xanh

Chỉ xóa khi grep không còn người gọi và test vẫn xanh.

- Nhãn `PaymentMethod::Vnpay` giữ đến khi có migration bỏ giá trị khỏi enum MySQL.
- Bảng membership giữ trong schema cũ. Không viết model mới cho chúng.
- View và JS không còn route thì xóa cùng route. Không xóa `public/models/custom-studio` và `public/vendor/three` vì trang In 3D đang dùng.

## Việc để ngoài các pha

- Đổi CSS hàng loạt, đổi tên route, đổi tên bảng.
- Gộp `NotificationWriter` email và thông báo trong web thành một kênh. Hai kênh khác nhau: email có hàng đợi, thông báo trong web chỉ cần `read_at`.
- Cache catalog: `CatalogCache` đã bump khi ghi sản phẩm. Không thêm tầng cache mới trong refactor.

## Định nghĩa xong

Một pha xong khi test của pha đó xanh, trang liên quan trên `http://localhost/WebTheThao/public` vẫn làm được việc cũ, và không có route mới. Cả phương án xong khi `php artisan test` xanh và không có mục nào trong “Việc không được làm” bị đụng.
