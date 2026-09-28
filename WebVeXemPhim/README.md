# Thiết kế và phát triển hệ thống bán vé xem phim theo kiến trúc hướng dịch vụ

Tài liệu kế hoạch bài tập lớn môn **Phát triển phần mềm hướng dịch vụ**.

Thư mục có **kế hoạch (`docs/`)** và **mã nguồn**: backend SOA `vexemphim-api` (REST) + client React `web-client`.

Chạy:

- Có MySQL: bật service MySQL 3306 (schema `vexemphim` tự tạo) rồi chạy `start-all.bat`
- Chưa có MySQL: `start-all.bat h2` — dùng H2 in-memory, dữ liệu reset mỗi lần chạy

Script mở API `:8080`, client `:5173` và tự mở trình duyệt. Client **chỉ** gọi http://localhost:8080.

Chạy tay: `cd vexemphim-api && mvn spring-boot:run` (thêm `-Dspring-boot.run.profiles=h2` nếu không có MySQL) và `cd web-client && npm run dev`.

## Tài khoản (đã tạo trong MySQL)

Form **Đăng ký** trên web chỉ tạo khách (`CUSTOMER`). Admin và nhân viên không tự đăng ký được.

| Vai trò | Tên đăng nhập | Mật khẩu | Vào trang |
|---|---|---|---|
| Quản trị | `admin` | `193850091011` | Đăng nhập → `/admin/movies` |
| Nhân viên (thu ngân) | `NhanVien` | `NhanVien123` | `/pos/login` → `/pos` |
| Khách demo | `customer1` | `customer123` | Đăng nhập thường, hoặc **Đăng ký** tài khoản mới |

## Tên đề tài

**Thiết kế và phát triển hệ thống bán vé xem phim theo kiến trúc hướng dịch vụ**

Tên phản ánh:

- **Thiết kế và phát triển hệ thống** — không chỉ một website độc lập.
- **Bán vé xem phim** — bài toán nghiệp vụ.
- **Kiến trúc hướng dịch vụ** — backend cung cấp RESTful API; client chỉ tiêu thụ API.

Phương án dự phòng (nếu cần chữ API / Client trên bìa):

- Xây dựng dịch vụ RESTful API và ứng dụng client cho hệ thống bán vé xem phim (đặt vé trực tuyến và POS tại rạp)
- Phát triển hệ thống bán vé xem phim theo mô hình Client-Server dựa trên RESTful API
- Xây dựng và tích hợp dịch vụ API với ứng dụng client cho hệ thống bán vé xem phim

Không dùng: “Xây dựng website bán vé xem phim”.

## Quyết định kiến trúc (cập nhật theo hướng dẫn thầy)

Backend là **Modular Monolith** (một process Spring Boot, một MySQL), **không** tách 4 microservice + API Gateway như bản kế hoạch cũ (bám lab CRS).

Lý do:

- Khớp bộ **SOS01–SOS10** (Hướng dẫn Modular Monolith): 3 tầng Controller–Service–Repository, JPA, một CSDL, CORS, client gọi API, phân quyền, tìm kiếm/phân trang, upload ảnh, API Key.
- Vẫn đúng môn: Client–Server, hợp đồng REST, module tách theo nghiệp vụ, hai kênh dùng chung API đặt ghế.
- Dễ chạy demo (một backend `:8080` + một frontend `:5173`). Hướng mở: tách module thành process riêng.

Chi tiết: [docs/01-kien-truc-soa.md](docs/01-kien-truc-soa.md).

## Mục tiêu sản phẩm

Hệ thống bán vé **một rạp**, hai kênh dùng **cùng một bộ API**:

1. **Đặt vé trực tuyến** — khách xem phim, suất, chọn ghế, thanh toán giả lập, nhận mã vé.
2. **POS tại quầy** — thu ngân chọn suất, sơ đồ ghế, thu tiền mặt, in vé.

Ghế, suất, trạng thái giữ/bán nằm ở **module booking**. Hai kênh không được có hai kho ghế riêng.

## Thành phần theo yêu cầu môn

| Thành phần | Trong đề tài này |
|---|---|
| Dịch vụ phía máy chủ (Backend Service/API) | Một app Spring Boot `vexemphim-api` (`:8080`): module `auth`, `catalog`, `booking` + `common` — RESTful JSON |
| Ứng dụng khách (Client Application) | Một app React: cổng khách `/` và cổng POS `/pos` (+ `/admin`) |
| Tích hợp | Client chỉ gọi `http://localhost:8080`. Nội bộ module gọi Service (bean), không RestTemplate |

## Cách đọc tài liệu

Đọc theo thứ tự số:

| File | Nội dung |
|---|---|
| [docs/01-kien-truc-soa.md](docs/01-kien-truc-soa.md) | Modular monolith, so SOS / CRS, sơ đồ, 3 tầng |
| [docs/02-danh-muc-service.md](docs/02-danh-muc-service.md) | Module, package, ranh giới, 3 tầng |
| [docs/03-mo-hinh-du-lieu.md](docs/03-mo-hinh-du-lieu.md) | Một DB, quan hệ 1-N, unique ghế, hold |
| [docs/04-hop-dong-api.md](docs/04-hop-dong-api.md) | Path, Pageable, upload, API Key, mã lỗi |
| [docs/05-luong-nghiep-vu.md](docs/05-luong-nghiep-vu.md) | POS, online, đồng bộ ghế, in vé |
| [docs/06-bao-mat-va-rbac.md](docs/06-bao-mat-va-rbac.md) | JWT (nâng SOS06) + API Key (SOS10), IDOR |
| [docs/07-frontend-hai-kenh.md](docs/07-frontend-hai-kenh.md) | Một React, hai kênh, search/page, upload |
| [docs/08-trien-khai-va-bat.md](docs/08-trien-khai-va-bat.md) | Cổng, `.bat`, JDK, MySQL |
| [docs/09-kiem-thu-demo-baocao.md](docs/09-kiem-thu-demo-baocao.md) | Postman (đăng nhập tài khoản), demo, báo cáo |
| [docs/10-lich-code.md](docs/10-lich-code.md) | Các pha khi bắt đầu lập trình |
| [docs/11-phuong-an-suat-chieu.md](docs/11-phuong-an-suat-chieu.md) | Admin lịch suất theo phòng |
| [docs/12-khoa-ghe-dong-thoi.md](docs/12-khoa-ghe-dong-thoi.md) | Hold, unique ghế, lock suất, 409 |
| [docs/13-audit-pos-staff.md](docs/13-audit-pos-staff.md) | Audit POS: bug, smell, gap rạp, thứ tự làm |

## Stack (bám SOS + nâng chỗ lab ghi “tạm thời”)

- Java 21, Spring Boot, Spring Web, Spring Data JPA, MySQL, Spring Security + JWT (JJWT 0.12.x)
- React + TypeScript + Vite + Axios + React Router (thay HTML thuần SOS08/09 vì cần hai kênh + sơ đồ ghế)
- Giao diện **CineVe** nền tối, CTA vàng, bố cục kiểu web rạp (chi tiết: [docs/07](docs/07-frontend-hai-kenh.md))
- Khởi động local bằng **file `.bat`**, không Docker Compose
- Thanh toán **giả lập** (tiền mặt POS / `ONLINE_MOCK`)
- Upload poster phim vào thư mục `uploads/` (SOS07)

## Phạm vi / ngoài phạm vi

**Trong BTL:** một rạp, vài phòng, vài phim + thể loại (1-N), tab Đang chiếu / Sắp chiếu (`movies.status` = `SHOWING` | `COMING` | `HIDDEN`), sơ đồ ghế đơn giản, 3 role, giữ ghế có hạn, không bán trùng, JWT, API Key khi ghi catalog, tìm kiếm/phân trang phim, upload ảnh.

**Ngoài BTL:** VNPay/Momo thật, nhiều cụm rạp, app mobile, Docker, Eureka/Kafka, Gateway, khuyến mãi phức tạp, hoàn vé.

## Tài khoản

Xem bảng ở đầu README. Tóm tắt: `admin` / `193850091011`, `NhanVien` / `NhanVien123`, `customer1` / `customer123`.

## Việc sinh viên phải điền khi tạo project

Theo SOS01: package / groupId theo `ten_lop.tenkhongdauXXX.ten_bai` (XXX = 3 số cuối MSSV). Trong kế hoạch dùng placeholder `btl.vexemphim` — **đổi đúng định dạng thầy bắt khi Pha 0**.
